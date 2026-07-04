package com.dtn.apply_job.service;

import com.dtn.apply_job.domain.Role;
import com.dtn.apply_job.domain.User;
import com.dtn.apply_job.domain.request.auth.ReqRegisterDTO;
import com.dtn.apply_job.exception.EmailExistedException;
import com.dtn.apply_job.exception.IdInvalidException;
import com.dtn.apply_job.repository.RoleRepository;
import com.dtn.apply_job.repository.UserRepository;
import com.dtn.apply_job.util.constant.enums.ERole;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;

@Service
public class AuthService {

    private final UserRepository userRepository;

    private final RoleRepository roleRepository;

    private final PasswordEncoder passwordEncoder;

    private final EmailService emailService;

    
    public AuthService(UserRepository userRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder, EmailService emailService) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
    }

    
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

    public void registerUser(ReqRegisterDTO payload) throws EmailExistedException, HttpMessageNotReadableException {
        
        if (userRepository.findByEmail(payload.getEmail()) != null) {
            throw new EmailExistedException("Email " + payload.getEmail() + " đã tồn tại, vui lòng sử dụng địa chỉ email khác.");
        }

        
        String rawPassword = generateRandomPassword();

        
        User newUser = new User();
        newUser.setEmail(payload.getEmail());
        newUser.setName(payload.getName());

        
        newUser.setPassword(passwordEncoder.encode(rawPassword));

        
        Role userRole = roleRepository.findByName(ERole.CANDIDATE)
                .orElseThrow(() -> new RuntimeException("Error: Role not found!."));
        newUser.getRoles().add(userRole);

        
        userRepository.save(newUser);

        
        
        emailService.sendPasswordEmail(payload.getEmail(), rawPassword);
    }

    @Transactional
    public void handleForgotPassword(String email) throws Exception {
        
        User user = userRepository.findByEmail(email);
        if (user == null) {
            
            
            
            throw new IdInvalidException("Tài khoản không tồn tại!");
        }

        
        String newRandomPassword = generateRandomPassword();

        
        user.setPassword(passwordEncoder.encode(newRandomPassword));
        userRepository.save(user);

        emailService.sendResetPasswordEmail(email, newRandomPassword);
    }
}