package com.dtn.apply_job.config;

import com.dtn.apply_job.domain.Role;
import com.dtn.apply_job.domain.User;
import com.dtn.apply_job.repository.RoleRepository;
import com.dtn.apply_job.repository.UserRepository;
import com.dtn.apply_job.util.constant.enums.ERole;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;

@Slf4j
@Configuration
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ApplicationInitConfig {

    PasswordEncoder passwordEncoder;

    @NonFinal
    @Value("${app.default-admin.email:admin@gmail.com}")
    String defaultAdminEmail;

    @NonFinal
    @Value("${app.default-admin.password:admin123}")
    String defaultAdminPassword;

    @NonFinal
    @Value("${app.default-admin.name:Administrator}")
    String defaultAdminName;

    @Bean
    @Transactional
    public ApplicationRunner applicationRunner(UserRepository userRepository, RoleRepository roleRepository) {
        return args -> {
            // 1. Initialize all roles if missing
            for (ERole roleName : ERole.values()) {
                if (!roleRepository.existsByName(roleName)) {
                    roleRepository.save(Role.builder().name(roleName).build());
                    log.info("Initialized role: {}", roleName);
                }
            }

            // 2. Initialize default ADMIN account if not exists
            if (!userRepository.existsByEmail(defaultAdminEmail)) {
                Role adminRole = roleRepository.findByName(ERole.ADMIN)
                        .orElseThrow(() -> new IllegalStateException("ADMIN role not found in database"));

                User adminUser = User.builder()
                        .email(defaultAdminEmail)
                        .name(defaultAdminName)
                        .password(passwordEncoder.encode(defaultAdminPassword))
                        .isActive(true)
                        .roles(new HashSet<>(Set.of(adminRole)))
                        .build();

                userRepository.save(adminUser);
                log.warn(">>> Default admin user created: [email: {}, password: {}]. Please change the password in production! <<<",
                        defaultAdminEmail, defaultAdminPassword);
            }
        };
    }
}
