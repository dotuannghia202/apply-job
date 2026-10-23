package com.dtn.apply_job.controller;

import com.dtn.apply_job.common.annotation.ApiMessage;
import com.dtn.apply_job.domain.User;
import com.dtn.apply_job.domain.request.auth.ReqForgotPasswordDTO;
import com.dtn.apply_job.domain.request.auth.ReqGoogleLoginDTO;
import com.dtn.apply_job.domain.request.auth.ReqRegisterDTO;
import com.dtn.apply_job.domain.request.user.ReqLoginDTO;
import com.dtn.apply_job.domain.response.user.ResLoginDTO;
import com.dtn.apply_job.exception.EmailExistedException;
import com.dtn.apply_job.exception.IdInvalidException;
import com.dtn.apply_job.mapper.UserMapper;
import com.dtn.apply_job.security.CustomUserDetails;
import com.dtn.apply_job.security.SecurityUtil;
import com.dtn.apply_job.service.AuthService;
import com.dtn.apply_job.service.UserService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuthController {

    AuthenticationManager authenticationManager;
    SecurityUtil securityUtil;
    UserService userService;
    AuthService authService;
    UserMapper userMapper;

    @NonFinal
    @Value("${jwt.refresh-token.expiration}")
    long refreshTokenExpiration;

    @NonFinal
    @Value("${jwt.access-token.expiration}")
    long accessTokenExpiration;

    @PostMapping("/login")
    @ApiMessage("Login successful")
    public ResponseEntity<ResLoginDTO> login(@Valid @RequestBody ReqLoginDTO reqLoginDTO) {
        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(
                        reqLoginDTO.getUsername(),
                        reqLoginDTO.getPassword()
                );

        Authentication authentication = authenticationManager.authenticate(authenticationToken);
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        User currentUser = userService.handleGetUserByUsername(userDetails.getUsername());

        return issueAuthResponse(currentUser);
    }

    @PostMapping("/google")
    @ApiMessage("Google login successful")
    public ResponseEntity<ResLoginDTO> loginWithGoogle(@Valid @RequestBody ReqGoogleLoginDTO reqDTO) throws Exception {
        User currentUser = authService.authenticateGoogleUser(reqDTO.getCode(), reqDTO.getRedirectUri());
        return issueAuthResponse(currentUser);
    }

    private ResponseEntity<ResLoginDTO> issueAuthResponse(User user) {
        CustomUserDetails userDetails = new CustomUserDetails(
                user.getId(),
                user.getEmail(),
                user.getName(),
                user.getPassword(),
                user.getAvatarUrl(),
                securityUtil.buildAuthorities(user)
        );

        Authentication authentication = new UsernamePasswordAuthenticationToken(
                userDetails,
                null,
                userDetails.getAuthorities()
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        String accessToken = securityUtil.createAccessToken(authentication);
        String refreshToken = securityUtil.createRefreshToken(authentication);

        userService.handleUpdateUserToken(refreshToken, user.getEmail());

        ResLoginDTO response = ResLoginDTO.builder()
                .userLogin(userMapper.toUserLogin(user))
                .build();

        ResponseCookie refreshTokenCookie = buildRefreshTokenCookie(refreshToken);
        ResponseCookie accessTokenCookie = buildAccessTokenCookie(accessToken);

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, accessTokenCookie.toString())
                .header(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString())
                .body(response);
    }

    @GetMapping("/account")
    @ApiMessage("Account retrieved successfully")
    public ResponseEntity<ResLoginDTO.UserGetAccount> getAccount() {
        String email = SecurityUtil.getCurrentUser().orElse("");
        User currentUser = userService.handleGetUserByUsername(email);

        ResLoginDTO.UserGetAccount response = ResLoginDTO.UserGetAccount.builder()
                .user(currentUser != null ? userMapper.toUserLogin(currentUser) : null)
                .build();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/refresh")
    public ResponseEntity<Void> refreshToken(
            @CookieValue(name = "refresh_token", defaultValue = "") String refreshToken
    ) throws Exception {
        if (refreshToken == null || refreshToken.isBlank()) {
            throw new BadCredentialsException("Refresh token is missing!");
        }

        Jwt decodedToken = securityUtil.checkValidRefreshToken(refreshToken);
        String email = decodedToken.getSubject();

        User currentUser = userService.handleGetUserByRefreshTokenAndEmail(refreshToken, email);
        if (currentUser == null) {
            throw new IdInvalidException("Refresh token invalid!");
        }

        CustomUserDetails userDetails = new CustomUserDetails(
                currentUser.getId(),
                currentUser.getEmail(),
                currentUser.getName(),
                currentUser.getPassword(),
                currentUser.getAvatarUrl(),
                securityUtil.buildAuthorities(currentUser)
        );

        Authentication authentication = new UsernamePasswordAuthenticationToken(
                userDetails,
                null,
                userDetails.getAuthorities()
        );

        String newAccessToken = securityUtil.createAccessToken(authentication);
        String newRefreshToken = securityUtil.createRefreshToken(authentication);

        userService.handleUpdateUserToken(newRefreshToken, email);

        ResponseCookie accessCookie = buildAccessTokenCookie(newAccessToken);
        ResponseCookie refreshCookie = buildRefreshTokenCookie(newRefreshToken);

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, accessCookie.toString())
                .header(HttpHeaders.SET_COOKIE, refreshCookie.toString())
                .body(null);
    }

    @PostMapping("/logout")
    @ApiMessage("Logout successful")
    public ResponseEntity<Void> logout() throws IdInvalidException {
        String email = SecurityUtil.getCurrentUser().orElse("");

        if (email.isBlank()) {
            throw new IdInvalidException("Access token invalid!");
        }

        userService.handleUpdateUserToken(null, email);

        ResponseCookie deleteRefreshCookie = ResponseCookie.from("refresh_token", "")
                .httpOnly(true)
                .secure(true)
                .path("/")
                .sameSite("None")
                .maxAge(0)
                .build();

        ResponseCookie deleteAccessCookie = ResponseCookie.from("access_token", "")
                .httpOnly(true)
                .secure(true)
                .path("/")
                .sameSite("None")
                .maxAge(0)
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, deleteAccessCookie.toString())
                .header(HttpHeaders.SET_COOKIE, deleteRefreshCookie.toString())
                .build();
    }

    @PostMapping("/register")
    public ResponseEntity<Void> register(@Valid @RequestBody ReqRegisterDTO registerPayload) throws EmailExistedException, HttpMessageNotReadableException {
        authService.registerUser(registerPayload);
        return ResponseEntity.status(HttpStatus.CREATED).body(null);
    }

    @PostMapping("/forgot-password")
    @ApiMessage("A new password has been sent to your email address!")
    public ResponseEntity<Void> forgotPassword(@Valid @RequestBody ReqForgotPasswordDTO reqDTO) throws Exception {
        authService.handleForgotPassword(reqDTO.getEmail());
        return ResponseEntity.ok().build();
    }

    private ResponseCookie buildRefreshTokenCookie(String refreshToken) {
        return ResponseCookie.from("refresh_token", refreshToken)
                .httpOnly(true)
                .secure(true)
                .path("/")
                .sameSite("None")
                .maxAge(refreshTokenExpiration / 1000)
                .build();
    }

    private ResponseCookie buildAccessTokenCookie(String accessToken) {
        return ResponseCookie.from("access_token", accessToken)
                .httpOnly(true)
                .secure(true)
                .path("/")
                .sameSite("None")
                .maxAge(accessTokenExpiration / 1000)
                .build();
    }
}