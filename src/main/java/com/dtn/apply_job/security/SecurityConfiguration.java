package com.dtn.apply_job.security;

import com.nimbusds.jose.jwk.source.ImmutableSecret;
import com.nimbusds.jose.util.Base64;
import jakarta.servlet.http.Cookie;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
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

    // =========================================================================
    // DẠY SPRING SECURITY CÁCH LẤY TOKEN TỪ COOKIE "access_token"
    // =========================================================================
    @Bean
    public BearerTokenResolver bearerTokenResolver() {
        DefaultBearerTokenResolver defaultResolver = new DefaultBearerTokenResolver();

        return request -> {
            // 1. Ưu tiên tìm trong Cookie trước
            if (request.getCookies() != null) {
                for (Cookie cookie : request.getCookies()) {
                    if ("access_token".equals(cookie.getName())) {
                        return cookie.getValue();
                    }
                }
            }
            // 2. Nếu không có Cookie thì tìm trong Header (Authorization: Bearer ...)
            return defaultResolver.resolve(request);
        };
    }
    // =========================================================================

    @Bean
    public SecurityFilterChain filterChain(
            HttpSecurity http,
            DaoAuthenticationProvider authenticationProvider,
            CustomAuthenticationEntryPoint customAuthenticationEntryPoint,
            CustomAccessDeniedHandler customAccessDeniedHandler
    ) throws Exception {

        http
                .csrf(csrf -> csrf.disable())
                .cors(Customizer.withDefaults())
                .authenticationProvider(authenticationProvider)
                .authorizeHttpRequests(authz -> authz
                        .requestMatchers(
                                "/",
                                "/api/v1/auth/login",
                                "/api/v1/auth/register",
                                "/api/v1/auth/refresh",
                                "/storage/**",
                                "/api/v1/auth/forgot-password",
                                "/ws/**"
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

                        // 🟢 Gắn cái BearerTokenResolver vừa tạo ở trên vào chuỗi bảo mật
                        .bearerTokenResolver(bearerTokenResolver())
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