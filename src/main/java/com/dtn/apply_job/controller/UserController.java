package com.dtn.apply_job.controller;

import com.dtn.apply_job.common.annotation.ApiMessage;
import com.dtn.apply_job.common.response.ResultPaginationDTO;
import com.dtn.apply_job.domain.User;
import com.dtn.apply_job.domain.request.user.ReqCreateUserDTO;
import com.dtn.apply_job.domain.request.user.ReqUpdateUserDTO;
import com.dtn.apply_job.domain.response.user.ResCreateUserDTO;
import com.dtn.apply_job.domain.response.user.ResUpdateUserDTO;
import com.dtn.apply_job.domain.response.user.ResUserDTO;
import com.dtn.apply_job.exception.EmailExistedException;
import com.dtn.apply_job.exception.IdInvalidException;
import com.dtn.apply_job.service.UserService;
import com.turkraft.springfilter.boot.Filter;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
public class UserController {

    private final UserService userService;


    public UserController(UserService userService, PasswordEncoder passwordEncoder) {

        this.userService = userService;

    }

    @GetMapping("/users")
    @ApiMessage("Fetch all users")
    public ResponseEntity<ResultPaginationDTO> getAllUsers(
            @Filter Specification<User> spec,
            Pageable pageable
    ) {

        ResultPaginationDTO result = this.userService.getAllUsers(spec, pageable);
        return ResponseEntity.status(HttpStatus.OK).body(result);
    }


    @PostMapping("/users")
    @ApiMessage("Create a new user")
    public ResponseEntity<ResCreateUserDTO> createUser(@RequestBody ReqCreateUserDTO user) throws EmailExistedException, IdInvalidException {

        ResCreateUserDTO result = this.userService.handleCreateUser(user);
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }


    @GetMapping("/users/{id}")
    @ApiMessage("Fetch user by id")
    public ResponseEntity<ResUserDTO> getUserById(@PathVariable long id) throws IdInvalidException {
        ResUserDTO result = this.userService.getUserById(id);
        return ResponseEntity.ok().body(result);
    }

    @DeleteMapping("/users/{id}")
    @ApiMessage("Delete user")
    public ResponseEntity<Void> deleteUserById(@PathVariable long id) throws IdInvalidException {
        this.userService.deleteUserById(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }


    @PutMapping("/users/{id}")
    @ApiMessage("Update user")
    public ResponseEntity<ResUpdateUserDTO> updateUser(@PathVariable long id, @RequestBody ReqUpdateUserDTO user) throws IdInvalidException {
        ResUpdateUserDTO result = this.userService.handleUpdateUser(id, user);
        return ResponseEntity.ok().body(result);
    }
}
