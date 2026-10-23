package com.dtn.apply_job.config;

import com.dtn.apply_job.domain.Industry;
import com.dtn.apply_job.domain.Role;
import com.dtn.apply_job.domain.Specialization;
import com.dtn.apply_job.domain.User;
import com.dtn.apply_job.repository.IndustryRepository;
import com.dtn.apply_job.repository.RoleRepository;
import com.dtn.apply_job.repository.SpecializationRepository;
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
import java.util.List;
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
    public ApplicationRunner applicationRunner(UserRepository userRepository,
                                               RoleRepository roleRepository,
                                               IndustryRepository industryRepository,
                                               SpecializationRepository specializationRepository) {
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

            // 3. Check and initialize default Industries & Specializations if DB has no industries yet
            if (industryRepository.count() == 0) {
                initDefaultIndustriesAndSpecializations(industryRepository, specializationRepository);
            } else {
                log.info("Database already contains {} industries. Skipping default industry seeding.", industryRepository.count());
            }
        };
    }

    private void initDefaultIndustriesAndSpecializations(IndustryRepository industryRepository,
                                                         SpecializationRepository specializationRepository) {
        log.info(">>> No industries found in database. Initializing default Industries & Specializations...");

        record SpecSeed(String nameVi, String nameEn) {}
        record IndustrySeed(String nameVi, String nameEn, List<SpecSeed> specs) {}

        List<IndustrySeed> seedList = List.of(
                new IndustrySeed(
                        "Công nghệ thông tin", "Information Technology",
                        List.of(
                                new SpecSeed("Kỹ thuật phần mềm", "Software Engineering"),
                                new SpecSeed("Trí tuệ nhân tạo & Khoa học dữ liệu", "AI & Data Science"),
                                new SpecSeed("An toàn thông tin", "Cybersecurity"),
                                new SpecSeed("Quản trị hệ thống & DevOps", "System Administration & DevOps"),
                                new SpecSeed("Thiết kế UI/UX", "UI/UX Design")
                        )
                ),
                new IndustrySeed(
                        "Kinh doanh & Bán hàng", "Business & Sales",
                        List.of(
                                new SpecSeed("Phát triển kinh doanh", "Business Development"),
                                new SpecSeed("Quản lý bán hàng", "Sales Management"),
                                new SpecSeed("Tư vấn & Chăm sóc khách hàng", "Customer Success & Consulting"),
                                new SpecSeed("Thương mại điện tử", "E-Commerce")
                        )
                ),
                new IndustrySeed(
                        "Marketing & Truyền thông", "Marketing & Communications",
                        List.of(
                                new SpecSeed("Tiếp thị số", "Digital Marketing"),
                                new SpecSeed("Quan hệ công chúng", "Public Relations - PR"),
                                new SpecSeed("Sáng tạo nội dung", "Content Creation"),
                                new SpecSeed("Nghiên cứu thị trường", "Market Research")
                        )
                ),
                new IndustrySeed(
                        "Xây dựng & Kiến trúc", "Construction & Architecture",
                        List.of(
                                new SpecSeed("Kỹ thuật xây dựng dân dụng", "Civil Engineering"),
                                new SpecSeed("Thiết kế kiến trúc", "Architectural Design"),
                                new SpecSeed("Quản lý dự án xây dựng", "Construction Project Management"),
                                new SpecSeed("Giám sát & Quản lý chất lượng", "Site Supervision & QA/QC")
                        )
                ),
                new IndustrySeed(
                        "Tài chính & Ngân hàng", "Finance & Banking",
                        List.of(
                                new SpecSeed("Ngân hàng & Tín dụng", "Banking & Credit"),
                                new SpecSeed("Kế toán & Kiểm toán", "Accounting & Auditing"),
                                new SpecSeed("Phân tích tài chính & Đầu tư", "Financial Analysis & Investment"),
                                new SpecSeed("Quản trị rủi ro tài chính", "Financial Risk Management")
                        )
                )
        );

        for (IndustrySeed seed : seedList) {
            Industry industry = industryRepository.save(Industry.builder()
                    .nameVi(seed.nameVi())
                    .nameEn(seed.nameEn())
                    .build());

            List<Specialization> specs = seed.specs().stream()
                    .map(s -> Specialization.builder()
                            .nameVi(s.nameVi())
                            .nameEn(s.nameEn())
                            .industry(industry)
                            .build())
                    .toList();

            specializationRepository.saveAll(specs);
            log.info("Initialized industry '{}' ('{}') with {} specializations.",
                    seed.nameVi(), seed.nameEn(), specs.size());
        }

        log.info(">>> Initialized default Industries and Specializations successfully!");
    }
}
