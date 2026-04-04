package com.dtn.apply_job.service;

import com.dtn.apply_job.domain.Role;
import com.dtn.apply_job.domain.User;
import com.dtn.apply_job.domain.request.auth.ReqRegisterDTO;
import com.dtn.apply_job.repository.RoleRepository;
import com.dtn.apply_job.repository.UserRepository;
import com.dtn.apply_job.util.constant.enums.ERole;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;

@Service
public class AuthService {

    private final UserRepository userRepository;

    private final RoleRepository roleRepository;

    private final PasswordEncoder passwordEncoder;

    private final EmailService emailService;

    // Tiêm EmailService vào đây
    public AuthService(UserRepository userRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder, EmailService emailService) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
    }

    // Hàm tạo mật khẩu ngẫu nhiên 8 ký tự
    private String generateRandomPassword() {
        final String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%";
        SecureRandom random = new SecureRandom();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 8; i++) {
            int randomIndex = random.nextInt(chars.length());
            sb.append(chars.charAt(randomIndex));
        }
        return sb.toString();
    }

    public void registerUser(ReqRegisterDTO payload) throws Exception {
        // 1. Kiểm tra Email đã tồn tại chưa
        if (userRepository.findByEmail(payload.getEmail()) != null) {
            throw new Exception("Email đã được sử dụng!");
        }

        // 2. Tạo mật khẩu ngẫu nhiên
        String rawPassword = generateRandomPassword();

        // 3. Khởi tạo User mới
        User newUser = new User();
        newUser.setEmail(payload.getEmail());
        newUser.setName(payload.getName());

        // Mã hóa mật khẩu trước khi lưu vào Database
        newUser.setPassword(passwordEncoder.encode(rawPassword));

        // Mặc định cho quyền ỨNG VIÊN
        Role userRole = roleRepository.findByName(ERole.CANDIDATE)
                .orElseThrow(() -> new RuntimeException("Lỗi: Không tìm thấy Role."));
        newUser.getRoles().add(userRole);

        // 4. Lưu vào Database
        userRepository.save(newUser);

        // 5. Gửi mật khẩu dạng thô (chưa mã hóa) qua Gmail cho người dùng
        // Chạy cái này ở luồng (thread) riêng để web không bị đơ chờ gửi mail nếu cần thiết
        emailService.sendPasswordEmail(payload.getEmail(), rawPassword);
    }
}