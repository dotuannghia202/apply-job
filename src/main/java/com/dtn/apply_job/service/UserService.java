package com.dtn.apply_job.service;

import com.dtn.apply_job.domain.User;
import com.dtn.apply_job.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public List<User> getAllUsers() {
        return this.userRepository.findAll();
    }

    public User createUser(User user) {
        return this.userRepository.save(user);
    }

    public User getUserById(long id) {
        return this.userRepository.findById(id).get();
    }

    public void deleteUserById(long id) {
        this.userRepository.deleteById(id);
        return;
    }

    public User updateUser(long id, User newUser) {
        User userUpdate = getUserById(id);
        userUpdate.setName(newUser.getName());
        userUpdate.setPassword(newUser.getPassword());
        return this.userRepository.save(userUpdate);
    }

    public User getUserByUsername(String email) {
        return this.userRepository.findByEmail(email);
    }
}
