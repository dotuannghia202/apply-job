package com.dtn.apply_job.security;

import com.dtn.apply_job.domain.User;
import com.dtn.apply_job.service.UserService;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserService userService;

    public CustomUserDetailsService(UserService userService) {
        this.userService = userService;
    }


    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = this.userService.handleGetUserByUsername(username);

        if (user == null) {
            throw new UsernameNotFoundException("User not found with email: " + username);
        }

        // 🚨 CHỐT CHẶN BẢO MẬT: NẾU TÀI KHOẢN BỊ KHÓA -> NÉM LỖI DisabledException NGAY LẬP TỨC
        if (user.getIsActive() != null && !user.getIsActive()) {
            throw new DisabledException("Your account has been locked by the Administrator!");
        }

        List<SimpleGrantedAuthority> authorities = user.getRoles().stream()
                .map(role -> new SimpleGrantedAuthority(role.getName().toString()))
                .toList();

        return new CustomUserDetails(
                user.getId(),
                user.getEmail(),
                user.getName(),
                user.getPassword(),
                user.getAvatarUrl(),
                authorities
        );
    }
}