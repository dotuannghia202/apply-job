package com.dtn.apply_job.controller;

import com.dtn.apply_job.domain.User;
import com.dtn.apply_job.domain.request.ReqLoginDTO;
import com.dtn.apply_job.domain.response.user.ResLoginDTO;
import com.dtn.apply_job.exception.IdInvalidException;
import com.dtn.apply_job.service.UserService;
import com.dtn.apply_job.util.SecurityUtil;
import com.dtn.apply_job.util.annotation.ApiMessage;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
public class AuthController {

    private final AuthenticationManagerBuilder authenticationManagerBuidlder;
    private final SecurityUtil securityUtil;
    private final UserService userService;

    @Value("${jwt.refresh-token.expiration}")
    private long refreshTokenExpiration;

    public AuthController(AuthenticationManagerBuilder authenticationManagerBuilder, SecurityUtil securityUtil, UserService userService) {
        this.authenticationManagerBuidlder = authenticationManagerBuilder;
        this.securityUtil = securityUtil;
        this.userService = userService;
    }

    @PostMapping("/auth/login")
    @ApiMessage("User login")
    public ResponseEntity<ResLoginDTO> login(@Valid @RequestBody ReqLoginDTO reqLoginDTO) {
        //Nạp input username và password
        UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(reqLoginDTO.getUsername(), reqLoginDTO.getPassword());

        //Xác thực, so sánh thông tin người dùng trong database, ghi đè loadUserByUsername
        Authentication authentication = authenticationManagerBuidlder.getObject().authenticate(token);

        //Lưu thông tin tài khoản đang đăng nhập vào context để có thể sử dụng sau này
        SecurityContextHolder.getContext().setAuthentication(authentication);

        ResLoginDTO res = new ResLoginDTO();
        User currentUserDB = this.userService.handleGetUserByUsername(reqLoginDTO.getUsername());
        if (currentUserDB != null) {
            ResLoginDTO.UserLogin resLoginDTO = new ResLoginDTO.UserLogin(
                    currentUserDB.getId(),
                    currentUserDB.getEmail(),
                    currentUserDB.getName());
            res.setUserLogin(resLoginDTO);
        }

        String access_token = this.securityUtil.createAccessToken(authentication.getName(), res.getUserLogin());

        res.setAccessToken(access_token);

        //Create refresh-token
        String refresh_token = this.securityUtil.createRefreshToken(reqLoginDTO.getUsername(), res);

        //Update user
        this.userService.handleUpdateUserToken(refresh_token, reqLoginDTO.getUsername());

        //Set cookie
        ResponseCookie resCookies = ResponseCookie.from("refresh_token", refresh_token)
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(refreshTokenExpiration / 1000)
                .build();

        return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, resCookies.toString()).body(res);
    }

    @GetMapping("/auth/account")
    @ApiMessage("Fetch info account")
    public ResponseEntity<ResLoginDTO.UserGetAccount> getAccount() {
        String email = SecurityUtil.getCurrentUser().isPresent() ?
                SecurityUtil.getCurrentUser().get() : "";

        User currentUserDB = this.userService.handleGetUserByUsername(email);

        ResLoginDTO.UserLogin userLogin = new ResLoginDTO.UserLogin();
        ResLoginDTO.UserGetAccount userGetAccount = new ResLoginDTO.UserGetAccount();
        if (currentUserDB != null) {
            userLogin.setId(currentUserDB.getId());
            userLogin.setEmail(currentUserDB.getEmail());
            userLogin.setName(currentUserDB.getName());
            userGetAccount.setUser(userLogin);
        }
        return ResponseEntity.ok().body(userGetAccount);
    }

    @GetMapping("/auth/refresh")
    @ApiMessage("Refresh token")
    public ResponseEntity<ResLoginDTO> getRefreshToken(@CookieValue(name = "refresh_token") String refresh_token) throws Exception {
        //Check valid token
        Jwt jwtDecodedToken = this.securityUtil.checkValidRefreshToken(refresh_token);
        String email = jwtDecodedToken.getSubject();

        //Check user by token + email
        User currentUser = this.userService.handleGetUserByRefreshTokenAndEmail(refresh_token, email);
        System.out.println("CurrentUser: " + currentUser);
        if (currentUser == null) {
            throw new IdInvalidException("Refresh token invalid!");
        }


        //Issue new token/set refresh token as cookie
        ResLoginDTO userRefreshToken = new ResLoginDTO();
        ResLoginDTO.UserLogin userLogin = new ResLoginDTO.UserLogin();
        userLogin.setId(currentUser.getId());
        userLogin.setEmail(currentUser.getEmail());
        userLogin.setName(currentUser.getName());

        userRefreshToken.setUserLogin(userLogin);

        String access_token = this.securityUtil.createAccessToken(email, userRefreshToken.getUserLogin());

        userRefreshToken.setAccessToken(access_token);

        //Create refresh-token
        String new_refresh_token = this.securityUtil.createRefreshToken(email, userRefreshToken);

        //Update user
        this.userService.handleUpdateUserToken(new_refresh_token, email);

        //Set cookie
        ResponseCookie resCookies = ResponseCookie.from("refresh_token", new_refresh_token)
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(refreshTokenExpiration / 1000)
                .build();

        return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, resCookies.toString()).body(userRefreshToken);
    }

    @PostMapping("/api/v1/auth/logout")
    @ApiMessage("User logout")
    public ResponseEntity<Void> logout() throws IdInvalidException {
        String email = SecurityUtil.getCurrentUser().isPresent() ?
                SecurityUtil.getCurrentUser().get() : "";

        if (email.equals("")) {
            throw new IdInvalidException("Access token invalid!");
        }

        //update refresh token = null
        this.userService.handleUpdateUserToken(null, email);

        //remove refresh token in cookie
        ResponseCookie deleteCookie = ResponseCookie
                .from("refresh_token", null)
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(0)
                .build();

        return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, deleteCookie.toString()).body(null);
    }
}
