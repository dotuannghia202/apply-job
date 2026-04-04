package com.dtn.apply_job.security;

import com.dtn.apply_job.domain.User;
import com.dtn.apply_job.service.UserService;
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

        List<SimpleGrantedAuthority> authorities = user.getRoles().stream()
                .map(role -> new SimpleGrantedAuthority(role.getName().toString()))
                .toList();

        return new CustomUserDetails(
                user.getId(),
                user.getEmail(),
                user.getName(),
                user.getPassword(),
                authorities
        );
    }
}