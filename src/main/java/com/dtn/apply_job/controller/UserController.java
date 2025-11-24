package com.dtn.apply_job.controller;

import com.dtn.apply_job.domain.User;
import com.dtn.apply_job.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class UserController {

    private final UserService userService;


    public UserController(UserService userService) {
        this.userService = userService;
    }
//    @GetMapping("/users")
//    public List<User> getAllUsers() {
//        return this.userService.getAllUsers();
//    }

    @GetMapping ("/users")
    public User createUser() {
        User user = new User();
        user.setName("Đỗ Tuấn Nghĩa");
        user.setPassword("123456");
        user.setEmail("dotuannghia07@gmail.com");
        return this.userService.createUser(user);
    }
}
