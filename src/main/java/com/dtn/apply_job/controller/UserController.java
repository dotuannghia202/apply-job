package com.dtn.apply_job.controller;

import com.dtn.apply_job.domain.User;
import com.dtn.apply_job.exception.IdInvalidException;
import com.dtn.apply_job.service.UserService;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;


import java.util.List;
import java.util.Optional;

@RestController
public class UserController {

    private final UserService userService;

    private final PasswordEncoder passwordEncoder;


    public UserController(UserService userService, PasswordEncoder passwordEncoder) {

        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("/users")
    public ResponseEntity<List<User>> getAllUsers(@RequestParam("current")Optional<String> currentOptional, @RequestParam("pageSize") Optional<String> pageSizeOptional) {
        String sCurrent = currentOptional.isPresent() ? currentOptional.get() : "";
        String sPageSize = pageSizeOptional.isPresent() ? pageSizeOptional.get() : "";

        int current =  Integer.parseInt(sCurrent);
        int pageSize = Integer.parseInt(sPageSize);
        Pageable pageable = PageRequest.of(current - 1, pageSize);
        List<User> result = this.userService.getAllUsers(pageable);
        return ResponseEntity.ok().body(result);
    }

    @PostMapping ("/users")
    public ResponseEntity<User> createUser(@RequestBody User user) {
        String hashedPassword = passwordEncoder.encode(user.getPassword());
        user.setPassword(hashedPassword);
        User result = this.userService.createUser(user);
        return ResponseEntity.status(HttpStatus.CREATED).body(null);
    }

    @GetMapping("/users/{id}")
    public ResponseEntity<User> getUserById(@PathVariable long id) throws IdInvalidException {
        if(id >= 1500){
            throw new IdInvalidException("Id invalid!");
        }
        User result = this.userService.getUserById(id);
        return ResponseEntity.ok().body(result);
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<Void> deleteUserById(@PathVariable long id) throws IdInvalidException {
        if(id >= 1500){
            throw new IdInvalidException("Id invalid!");
        }
        this.userService.deleteUserById(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @PutMapping("/users/{id}")
    public ResponseEntity<User> updateUser(@PathVariable long id , @RequestBody User user) {
        User result = this.userService.updateUser(id, user);
        return ResponseEntity.ok().body(result);
    }
}
