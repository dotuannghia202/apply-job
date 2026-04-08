package com.dtn.apply_job.controller;

import com.dtn.apply_job.common.annotation.ApiMessage;
import com.dtn.apply_job.domain.User;
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
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;
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

        List<String> roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        // 7. Build response
        ResLoginDTO response = buildLoginResponse(userDetails, accessToken, refreshToken, roles);

        // 8. Set refresh token vào httpOnly cookie
        ResponseCookie responseCookie = buildRefreshTokenCookie(refreshToken);

        // 9. Trả response + cookie
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, responseCookie.toString())
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
            throw new IdInvalidException("Refresh token is missing!");
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
                securityUtil.buildAuthorities(currentUser) // nếu bạn chưa có hàm này thì xem ghi chú bên dưới
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

        // 7. Set cookie mới
        ResponseCookie responseCookie = buildRefreshTokenCookie(newRefreshToken);

        ResRefreshTokenDTO res = new ResRefreshTokenDTO(newAccessToken);

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, responseCookie.toString())
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

        // 2. Xóa cookie
        ResponseCookie deleteCookie = ResponseCookie.from("refresh_token", "")
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(0)
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, deleteCookie.toString())
                .build();
    }

    @PostMapping("/register")
    public ResponseEntity<Void> register(@Valid @RequestBody ReqRegisterDTO registerPayload) throws EmailExistedException, HttpMessageNotReadableException {
        authService.registerUser(registerPayload);
        return ResponseEntity.status(HttpStatus.CREATED).body(null);
    }

    private ResLoginDTO buildLoginResponse(
            CustomUserDetails userDetails,
            String accessToken,
            String refreshToken,
            List<String> roles) {
        ResLoginDTO response = new ResLoginDTO();
        response.setAccessToken(accessToken);

        response.setUserLogin(buildUserLogin(userDetails, roles));
        return response;
    }

    private ResLoginDTO.UserLogin buildUserLogin(CustomUserDetails userDetails, List<String> roles) {
        ResLoginDTO.UserLogin userLogin = new ResLoginDTO.UserLogin();
        userLogin.setId(userDetails.getId());
        userLogin.setEmail(userDetails.getUsername());
        userLogin.setName(userDetails.getFullName());
        userLogin.setRoles(roles);
        return userLogin;
    }

    private ResLoginDTO.UserLogin buildUserLogin(User user) {
        ResLoginDTO.UserLogin userLogin = new ResLoginDTO.UserLogin();
        userLogin.setId(user.getId());
        userLogin.setEmail(user.getEmail());
        userLogin.setName(user.getName());
        userLogin.setRoles(user.getRoles().stream()
                .map(role -> role.getName().name())
                .collect(Collectors.toList()));
        return userLogin;
    }

    private ResponseCookie buildRefreshTokenCookie(String refreshToken) {
        return ResponseCookie.from("refresh_token", refreshToken)
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(refreshTokenExpiration / 1000)
                .build();
    }
}