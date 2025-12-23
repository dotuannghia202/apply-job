package com.dtn.apply_job.service;

import com.dtn.apply_job.domain.User;
import com.dtn.apply_job.domain.dto.Meta;
import com.dtn.apply_job.domain.dto.ResultPaginationDTO;
import com.dtn.apply_job.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public ResultPaginationDTO getAllUsers(Specification<User> spec, Pageable pageable) {
        Page<User> pageUser =  this.userRepository.findAll(spec, pageable);

        ResultPaginationDTO resultPaginationDTO = new ResultPaginationDTO();
        Meta meta = new Meta();

        meta.setPage(pageUser.getNumber() + 1);
        meta.setPageSize(pageUser.getSize());
        meta.setPages(pageUser.getTotalPages());
        meta.setTotal(pageUser.getTotalElements());

        resultPaginationDTO.setMeta(meta);
        resultPaginationDTO.setResult(pageUser.getContent());

        return resultPaginationDTO;
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
