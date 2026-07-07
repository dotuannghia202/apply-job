package com.dtn.apply_job.controller;

import com.dtn.apply_job.common.annotation.ApiMessage;
import com.dtn.apply_job.domain.User;
import com.dtn.apply_job.domain.request.auth.ReqForgotPasswordDTO;
import com.dtn.apply_job.domain.request.auth.ReqRegisterDTO;
import com.dtn.apply_job.domain.request.user.ReqLoginDTO;
import com.dtn.apply_job.domain.response.user.ResLoginDTO;
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
    @ApiMessage("Đăng nhập thành công")
    public ResponseEntity<ResLoginDTO> login(@Valid @RequestBody ReqLoginDTO reqLoginDTO) {
        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(
                        reqLoginDTO.getUsername(),
                        reqLoginDTO.getPassword()
                );

        Authentication authentication = authenticationManager.authenticate(authenticationToken);

        SecurityContextHolder.getContext().setAuthentication(authentication);

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

        String accessToken = securityUtil.createAccessToken(authentication);
        String refreshToken = securityUtil.createRefreshToken(authentication);

        userService.handleUpdateUserToken(refreshToken, userDetails.getUsername());

        User currentUser = userService.handleGetUserByUsername(userDetails.getUsername());

        ResLoginDTO response = new ResLoginDTO();
        response.setUserLogin(buildUserLogin(currentUser));

        ResponseCookie refreshTokenCookie = buildRefreshTokenCookie(refreshToken);
        ResponseCookie accessTokenCookie = buildAccessTokenCookie(accessToken);


        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, accessTokenCookie.toString())
                .header(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString())
                .body(response);
    }

    @GetMapping("/account")
    @ApiMessage("Lấy thông tin tài khoản thành công")
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
    @ApiMessage("Đăng xuất thành công")
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
    @ApiMessage("Mật khẩu mới đã được gửi tới địa chỉ email của bạn!")
    public ResponseEntity<Void> forgotPassword(@Valid @RequestBody ReqForgotPasswordDTO reqDTO) throws Exception {
        authService.handleForgotPassword(reqDTO.getEmail());
        return ResponseEntity.ok().build();
    }
}