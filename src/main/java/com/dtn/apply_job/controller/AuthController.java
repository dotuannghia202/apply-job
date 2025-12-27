package com.dtn.apply_job.controller;

import com.dtn.apply_job.domain.User;
import com.dtn.apply_job.domain.dto.LoginDTO;
import com.dtn.apply_job.domain.dto.ResLoginDTO;
import com.dtn.apply_job.service.UserService;
import com.dtn.apply_job.util.SecurityUtil;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
public class AuthController {

    private final AuthenticationManagerBuilder authenticationManagerBuidlder;
    private final SecurityUtil securityUtil;
    private final UserService userService;

    public AuthController(AuthenticationManagerBuilder authenticationManagerBuilder, SecurityUtil securityUtil, UserService userService) {
        this.authenticationManagerBuidlder = authenticationManagerBuilder;
        this.securityUtil = securityUtil;
        this.userService = userService;
    }

    @PostMapping("/login")
    public ResponseEntity<ResLoginDTO> login(@Valid @RequestBody LoginDTO loginDTO) {
        //Nạp input username và password
        UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(loginDTO.getUsername(), loginDTO.getPassword());

        //Xác thực, so sánh thông tin người dùng trong database, ghi đè loadUserByUsername
        Authentication authentication = authenticationManagerBuidlder.getObject().authenticate(token);

        String access_token = this.securityUtil.createAccessGToken(authentication);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        ResLoginDTO res = new ResLoginDTO();
        User currentUserDB = this.userService.getUserByUsername(loginDTO.getUsername());
        if (currentUserDB != null) {
            ResLoginDTO.UserLogin resLoginDTO = new ResLoginDTO.UserLogin(
                    currentUserDB.getId(),
                    currentUserDB.getEmail(),
                    currentUserDB.getName());
            res.setUserLogin(resLoginDTO);
        }

        res.setAccessToken(access_token);

        //Create refresh-token
        String refresh_token = this.securityUtil.createRefreshToken(loginDTO.getUsername(), res);
        return ResponseEntity.ok().body(res);
    }
}
