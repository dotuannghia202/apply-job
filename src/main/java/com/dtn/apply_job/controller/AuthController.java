package com.dtn.apply_job.controller;

import com.dtn.apply_job.common.annotation.ApiMessage;
import com.dtn.apply_job.domain.User;
import com.dtn.apply_job.domain.request.auth.ReqForgotPasswordDTO;
import com.dtn.apply_job.domain.request.auth.ReqRegisterDTO;
import com.dtn.apply_job.domain.request.user.ReqLoginDTO;
import com.dtn.apply_job.domain.response.user.ResLoginDTO;
import com.dtn.apply_job.domain.response.user.ResRefreshTokenDTO;
import com.dtn.apply_job.exception.EmailExistedException;
import com.dtn.apply_job.exception.IdInvalidException;
import com.dtn.apply_job.security.CustomUserDetails;
import com.dtn.apply_job.security.SecurityUtil;
import com.dtn.apply_job.service.AuthService;
import com.dtn.apply_job.service.UserService;
import jakarta.validation.Valid;
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

import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final SecurityUtil securityUtil;
    private final UserService userService;
    private final AuthService authService;

    @Value("${jwt.refresh-token.expiration}")
    private long refreshTokenExpiration;

    @Value("${jwt.access-token.expiration}")
    private long accessTokenExpiration;

    public AuthController(
            AuthenticationManager authenticationManager,
            SecurityUtil securityUtil,
            UserService userService,
            AuthService authService
    ) {
        this.authenticationManager = authenticationManager;
        this.securityUtil = securityUtil;
        this.userService = userService;
        this.authService = authService;
    }

    @PostMapping("/login")
    @ApiMessage("User login")
    public ResponseEntity<ResLoginDTO> login(@Valid @RequestBody ReqLoginDTO reqLoginDTO) {
        // 1. Tạo request xác thực từ username + password
        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(
                        reqLoginDTO.getUsername(),
                        reqLoginDTO.getPassword()
                );

        // 2. Gọi Spring Security xác thực
        Authentication authentication = authenticationManager.authenticate(authenticationToken);

        // 3. Lưu vào security context cho request hiện tại
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // 4. Lấy principal đã xác thực
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

        // 5. Tạo token
        String accessToken = securityUtil.createAccessToken(authentication);
        String refreshToken = securityUtil.createRefreshToken(authentication);

        // 6. Lưu refresh token vào DB
        userService.handleUpdateUserToken(refreshToken, userDetails.getUsername());

        // 7. LẤY FULL THÔNG TIN USER TỪ DB ĐỂ LẤY ĐƯỢC COMPANY
        User currentUser = userService.handleGetUserByUsername(userDetails.getUsername());

        ResLoginDTO response = new ResLoginDTO();
        response.setUserLogin(buildUserLogin(currentUser));

        // 6. Set cookie
        ResponseCookie refreshTokenCookie = buildRefreshTokenCookie(refreshToken);
        ResponseCookie accessTokenCookie = buildAccessTokenCookie(accessToken);


        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, accessTokenCookie.toString())
                .header(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString())
                .body(response);
    }

    @GetMapping("/account")
    @ApiMessage("Fetch info account")
    public ResponseEntity<ResLoginDTO.UserGetAccount> getAccount() {
        String email = SecurityUtil.getCurrentUser().orElse("");

        User currentUser = userService.handleGetUserByUsername(email);

        ResLoginDTO.UserGetAccount response = new ResLoginDTO.UserGetAccount();

        if (currentUser != null) {
            response.setUser(buildUserLogin(currentUser));
        }

        return ResponseEntity.ok(response);
    }

    @GetMapping("/refresh")
    public ResponseEntity<ResRefreshTokenDTO> refreshToken(
            @CookieValue(name = "refresh_token", defaultValue = "") String refreshToken
    ) throws Exception {

        if (refreshToken == null || refreshToken.isBlank()) {
            throw new BadCredentialsException("Refresh token is missing!");
        }

        // 1. Verify refresh token
        Jwt decodedToken = securityUtil.checkValidRefreshToken(refreshToken);
        String email = decodedToken.getSubject();

        // 2. Check token trong DB có khớp user không
        User currentUser = userService.handleGetUserByRefreshTokenAndEmail(refreshToken, email);
        if (currentUser == null) {
            throw new IdInvalidException("Refresh token invalid!");
        }

        // 3. Dựng lại CustomUserDetails từ user DB
        CustomUserDetails userDetails = new CustomUserDetails(
                currentUser.getId(),
                currentUser.getEmail(),
                currentUser.getName(),
                currentUser.getPassword(),
                currentUser.getAvatarUrl(),
                securityUtil.buildAuthorities(currentUser)
        );

        // 4. Dựng lại Authentication
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                userDetails,
                null,
                userDetails.getAuthorities()
        );

        // 5. Tạo access token mới + refresh token mới
        String newAccessToken = securityUtil.createAccessToken(authentication);
        String newRefreshToken = securityUtil.createRefreshToken(authentication);

        // 6. Update refresh token mới vào DB
        userService.handleUpdateUserToken(newRefreshToken, email);

        // ==========================================
        // 7. SET LẠI COOKIE CHO CẢ 2 TOKEN MỚI
        // ==========================================
        ResponseCookie accessCookie = buildAccessTokenCookie(newAccessToken);
        ResponseCookie refreshCookie = buildRefreshTokenCookie(newRefreshToken);

        // Tạo một DTO rỗng (Không chứa access token nữa)
        // Lưu ý: Bạn có thể vào file ResRefreshTokenDTO đổi nó thành class rỗng,
        // Hoặc truyền null vào constructor: new ResRefreshTokenDTO(null)
        ResRefreshTokenDTO res = new ResRefreshTokenDTO(null);

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, accessCookie.toString())
                .header(HttpHeaders.SET_COOKIE, refreshCookie.toString())
                .body(res);
    }

    @PostMapping("/logout")
    @ApiMessage("User logout")
    public ResponseEntity<Void> logout() throws IdInvalidException {
        String email = SecurityUtil.getCurrentUser().orElse("");

        if (email.isBlank()) {
            throw new IdInvalidException("Access token invalid!");
        }

        // 1. Xóa refresh token trong DB
        userService.handleUpdateUserToken(null, email);

        // ==========================================
        // 2. TẠO 2 COOKIE TRỐNG ĐỂ HỦY TOKEN (maxAge = 0)
        // ==========================================
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


    private ResLoginDTO.UserLogin buildUserLogin(User user) {
        ResLoginDTO.UserLogin userLogin = new ResLoginDTO.UserLogin();
        userLogin.setId(user.getId());
        userLogin.setEmail(user.getEmail());
        userLogin.setName(user.getName());
        userLogin.setAvatarUrl(user.getAvatarUrl());
        userLogin.setRoles(user.getRoles().stream()
                .map(role -> role.getName().name())
                .collect(Collectors.toList()));

        if (user.getCompany() != null) {
            ResLoginDTO.UserLogin.CompanyInfo companyInfo = new ResLoginDTO.UserLogin.CompanyInfo();
            companyInfo.setId(user.getCompany().getId());
            companyInfo.setName(user.getCompany().getName());
            userLogin.setCompany(companyInfo);
        }
        return userLogin;
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


    @PostMapping("/forgot-password")
    @ApiMessage("Your new password has been sent to your email address!")
    public ResponseEntity<Void> forgotPassword(@Valid @RequestBody ReqForgotPasswordDTO reqDTO) throws Exception {
        authService.handleForgotPassword(reqDTO.getEmail());
        return ResponseEntity.ok().build();
    }
}