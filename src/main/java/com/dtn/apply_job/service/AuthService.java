package com.dtn.apply_job.service;

import com.dtn.apply_job.domain.Role;
import com.dtn.apply_job.domain.User;
import com.dtn.apply_job.domain.request.auth.ReqRegisterDTO;
import com.dtn.apply_job.exception.EmailExistedException;
import com.dtn.apply_job.exception.IdInvalidException;
import com.dtn.apply_job.repository.RoleRepository;
import com.dtn.apply_job.repository.UserRepository;
import com.dtn.apply_job.util.constant.enums.ERole;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeTokenRequest;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;

@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuthService {

    UserRepository userRepository;
    RoleRepository roleRepository;
    PasswordEncoder passwordEncoder;
    EmailService emailService;

    @NonFinal
    @Value("${google.client-id}")
    String clientId;

    @NonFinal
    @Value("${google.client-secret}")
    String clientSecret;

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
            throw new EmailExistedException("Email already exists: " + payload.getEmail() + ". Please use a different email address.");
        }

        String rawPassword = generateRandomPassword();

        Role userRole = roleRepository.findByName(ERole.CANDIDATE)
                .orElseThrow(() -> new RuntimeException("Role CANDIDATE not found."));

        User newUser = User.builder()
                .email(payload.getEmail())
                .name(payload.getName())
                .password(passwordEncoder.encode(rawPassword))
                .isActive(true)
                .build();
        newUser.getRoles().add(userRole);

        userRepository.save(newUser);

        emailService.sendPasswordEmail(payload.getEmail(), rawPassword);
    }

    @Transactional
    public void handleForgotPassword(String email) throws Exception {
        User user = userRepository.findByEmail(email);
        if (user == null) {
            throw new IdInvalidException("Account does not exist!");
        }

        String newRandomPassword = generateRandomPassword();
        user.setPassword(passwordEncoder.encode(newRandomPassword));
        userRepository.save(user);

        emailService.sendResetPasswordEmail(email, newRandomPassword);
    }

    @Transactional
    public User authenticateGoogleUser(String authCode, String redirectUri) throws Exception {
        String effectiveRedirectUri = (redirectUri != null && !redirectUri.isBlank()) ? redirectUri : "postmessage";

        GoogleTokenResponse tokenResponse;
        try {
            tokenResponse = new GoogleAuthorizationCodeTokenRequest(
                    GoogleNetHttpTransport.newTrustedTransport(),
                    GsonFactory.getDefaultInstance(),
                    clientId,
                    clientSecret,
                    authCode,
                    effectiveRedirectUri
            ).execute();
        } catch (Exception e) {
            log.error("Failed to verify Google authorization code: {}", e.getMessage());
            throw new IdInvalidException("Failed to verify Google authorization code: " + e.getMessage());
        }

        GoogleIdToken idToken = tokenResponse.parseIdToken();
        if (idToken == null) {
            throw new IdInvalidException("Failed to parse ID token from Google.");
        }

        GoogleIdToken.Payload payload = idToken.getPayload();
        String email = payload.getEmail();
        String name = (String) payload.get("name");
        String picture = (String) payload.get("picture");

        if (email == null || email.isBlank()) {
            throw new IdInvalidException("Email address not found in Google account.");
        }

        User user = userRepository.findByEmail(email);
        if (user == null) {
            Role userRole = roleRepository.findByName(ERole.CANDIDATE)
                    .orElseThrow(() -> new RuntimeException("Role CANDIDATE not found."));

            user = User.builder()
                    .email(email)
                    .name(name != null && !name.isBlank() ? name : email.split("@")[0])
                    .avatarUrl(picture)
                    .password(passwordEncoder.encode(generateRandomPassword()))
                    .isActive(true)
                    .build();
            user.getRoles().add(userRole);
        } else {
            if (Boolean.FALSE.equals(user.getIsActive())) {
                throw new IdInvalidException("Account is locked. Please contact the administrator.");
            }
            if ((user.getAvatarUrl() == null || user.getAvatarUrl().isBlank()) && picture != null) {
                user.setAvatarUrl(picture);
            }
        }

        return userRepository.save(user);
    }
}