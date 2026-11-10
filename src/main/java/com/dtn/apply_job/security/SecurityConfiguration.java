package com.dtn.apply_job.security;

import com.nimbusds.jose.jwk.source.ImmutableSecret;
import com.nimbusds.jose.util.Base64;
import jakarta.servlet.http.Cookie;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.oauth2.server.resource.web.BearerTokenResolver;
import org.springframework.security.oauth2.server.resource.web.DefaultBearerTokenResolver;
import org.springframework.security.web.SecurityFilterChain;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

@Configuration
@EnableMethodSecurity(securedEnabled = true)
public class SecurityConfiguration {

    @Value("${jwt.secret}")
    private String jwtKey;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider(
            CustomUserDetailsService customUserDetailsService
    ) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(customUserDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration authenticationConfiguration
    ) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }


    private boolean isPublicGetRequest(jakarta.servlet.http.HttpServletRequest request) {
        if (!HttpMethod.GET.matches(request.getMethod())) {
            return false;
        }
        String path = request.getRequestURI();
        if (path.startsWith("/api/v1/jobs/hr")
                || path.startsWith("/api/v1/companies/dashboard-stats")
                || path.startsWith("/api/v1/companies/my-company")) {
            return false;
        }
        if (path.equals("/api/v1/companies") || path.equals("/api/v1/companies/")) {
            return false;
        }

        return path.startsWith("/api/v1/industries")
                || path.startsWith("/api/v1/specializations")
                || path.startsWith("/api/v1/jobs")
                || path.startsWith("/api/v1/skills")
                || path.startsWith("/storage/")
                || path.startsWith("/api/v1/companies/");
    }

    // cấu hình để đọc cookie access_token trên mọi request ngoại trừ auth
    @Bean
    public BearerTokenResolver bearerTokenResolver(JwtDecoder jwtDecoder) {
        DefaultBearerTokenResolver defaultResolver = new DefaultBearerTokenResolver();

        return request -> {
            String path = request.getRequestURI();
            if (path.startsWith("/api/v1/auth/login")
                    || path.startsWith("/api/v1/auth/google")
                    || path.startsWith("/api/v1/auth/register")
                    || path.startsWith("/api/v1/auth/forgot-password")
                    || path.startsWith("/api/v1/auth/refresh")) {
                return null;
            }

            String token = null;
            if (request.getCookies() != null) {
                for (Cookie cookie : request.getCookies()) {
                    if ("access_token".equals(cookie.getName())) {
                        token = cookie.getValue();
                        break;
                    }
                }
            }

            if (token == null) {
                token = defaultResolver.resolve(request);
            }

            // Với request GET public (trang chủ, tìm việc): nếu token hết hạn/lỗi, coi như khách vãng lai (bỏ qua token để không bị chặn 401)
            if (token != null && isPublicGetRequest(request)) {
                try {
                    jwtDecoder.decode(token);
                } catch (Exception e) {
                    return null;
                }
            }

            return token;
        };
    }

    @Bean
    public SecurityFilterChain filterChain(
            HttpSecurity http,
            DaoAuthenticationProvider authenticationProvider,
            CustomAuthenticationEntryPoint customAuthenticationEntryPoint,
            CustomAccessDeniedHandler customAccessDeniedHandler,
            BearerTokenResolver bearerTokenResolver
    ) throws Exception {

        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(Customizer.withDefaults())
                .authenticationProvider(authenticationProvider)
                .authorizeHttpRequests(authz -> authz
                        .requestMatchers(
                                "/",
                                "/api/v1/auth/login",
                                "/api/v1/auth/register",
                                "/api/v1/auth/refresh",
                                "/api/v1/auth/google",
                                "/storage/**",
                                "/api/v1/auth/forgot-password",
                                "/ws/**"
                        ).permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/jobs/hr").authenticated()
                        .requestMatchers(HttpMethod.GET,
                                "/api/v1/industries",
                                "/api/v1/industries/**",
                                "/api/v1/specializations",
                                "/api/v1/specializations/**",
                                "/api/v1/jobs",
                                "/api/v1/jobs/**",
                                "/api/v1/skills",
                                "/api/v1/skills/**",
                                "/api/v1/companies/*"
                        ).permitAll()
                        .anyRequest().authenticated()
                )
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(customAuthenticationEntryPoint)
                        .accessDeniedHandler(customAccessDeniedHandler)
                )

                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter()))
                        .authenticationEntryPoint(customAuthenticationEntryPoint)
                        .bearerTokenResolver(bearerTokenResolver)
                )
                .formLogin(form -> form.disable())
                .httpBasic(httpBasic -> httpBasic.disable())
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                );

        return http.build();
    }

    private SecretKey getSecretKey() {
        byte[] keyBytes = Base64.from(jwtKey).decode();
        return new SecretKeySpec(
                keyBytes,
                0,
                keyBytes.length,
                SecurityUtil.JWT_ALGORITHM.getName()
        );
    }

    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtGrantedAuthoritiesConverter grantedAuthoritiesConverter =
                new JwtGrantedAuthoritiesConverter();

        grantedAuthoritiesConverter.setAuthorityPrefix("");
        grantedAuthoritiesConverter.setAuthoritiesClaimName("roles");
        grantedAuthoritiesConverter.setAuthorityPrefix("ROLE_");

        JwtAuthenticationConverter jwtAuthenticationConverter =
                new JwtAuthenticationConverter();
        jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(grantedAuthoritiesConverter);

        return jwtAuthenticationConverter;
    }

    @Bean
    public JwtDecoder jwtDecoder() {
        NimbusJwtDecoder jwtDecoder = NimbusJwtDecoder
                .withSecretKey(getSecretKey())
                .macAlgorithm(SecurityUtil.JWT_ALGORITHM)
                .build();

        return token -> {
            try {
                return jwtDecoder.decode(token);
            } catch (Exception e) {
                System.out.println(">>> JWT error: " + e.getMessage());
                throw e;
            }
        };
    }

    @Bean
    public JwtEncoder jwtEncoder() {
        return new NimbusJwtEncoder(new ImmutableSecret<>(getSecretKey()));
    }
}