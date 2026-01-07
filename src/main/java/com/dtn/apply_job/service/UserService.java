package com.dtn.apply_job.service;

import com.dtn.apply_job.domain.Company;
import com.dtn.apply_job.domain.User;
import com.dtn.apply_job.domain.response.ResCreatedDTO;
import com.dtn.apply_job.domain.response.ResUpdateDTO;
import com.dtn.apply_job.domain.response.ResUserDTO;
import com.dtn.apply_job.domain.response.ResultPaginationDTO;
import com.dtn.apply_job.exception.EmailExistedException;
import com.dtn.apply_job.exception.IdInvalidException;
import com.dtn.apply_job.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class UserService {
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final CompanyService companyService;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, CompanyService companyService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.companyService = companyService;
    }

    public ResultPaginationDTO getAllUsers(Specification<User> spec, Pageable pageable) {
        Page<User> pageUser = this.userRepository.findAll(spec, pageable);

        List<ResUserDTO> results = new ArrayList<>(pageUser.getContent().size());
        for (User user : pageUser.getContent()) {
            ResUserDTO userDTO = new ResUserDTO();
            userDTO.setId(user.getId());
            userDTO.setName(user.getName());
            userDTO.setEmail(user.getEmail());
            userDTO.setAge(user.getAge());
            userDTO.setAddress(user.getAddress());
            userDTO.setCreatedAt(user.getCreatedAt());
            userDTO.setUpdatedAt(user.getUpdatedAt());
            userDTO.setCreatedBy(user.getCreatedBy());
            userDTO.setUpdatedBy(user.getUpdatedBy());

            if (user.getCompany() != null) {
                ResUserDTO.CompanyUser companyUserDTO = new ResUserDTO.CompanyUser();
                companyUserDTO.setId(user.getCompany().getId());
                companyUserDTO.setName(user.getCompany().getName());
                userDTO.setCompany(companyUserDTO);
            }

            results.add(userDTO);

        }

        ResultPaginationDTO resultPaginationDTO = new ResultPaginationDTO();
        ResultPaginationDTO.Meta meta = new ResultPaginationDTO.Meta();

        meta.setPage(pageUser.getNumber() + 1);
        meta.setPageSize(pageUser.getSize());
        meta.setPages(pageUser.getTotalPages());
        meta.setTotal(pageUser.getTotalElements());

        resultPaginationDTO.setMeta(meta);
        resultPaginationDTO.setResult(results);

        return resultPaginationDTO;
    }

    public ResCreatedDTO handleCreateUser(User user) throws EmailExistedException {
        if (this.userRepository.existsByEmail(user.getEmail())) {
            throw new EmailExistedException("Email " + user.getEmail() + " already exists, please use a different email address.");
        }

        if (user.getCompany() != null) {
            Optional<Company> optionalCompany = this.companyService.getCompanyRepository().findById(user.getCompany().getId());
            user.setCompany(optionalCompany.isPresent() ? optionalCompany.get() : null);
        }

        String hashedPassword = passwordEncoder.encode(user.getPassword());
        user.setPassword(hashedPassword);

        User newUser = this.userRepository.save(user);

        ResCreatedDTO resCreatedDTO = new ResCreatedDTO();
        resCreatedDTO.setId(newUser.getId());
        resCreatedDTO.setName(newUser.getName());
        resCreatedDTO.setEmail(newUser.getEmail());
        resCreatedDTO.setAge(newUser.getAge());
        resCreatedDTO.setGender(newUser.getGender().toString());
        resCreatedDTO.setAddress(newUser.getAddress());
        resCreatedDTO.setCreatedAt(newUser.getCreatedAt());
        resCreatedDTO.setCreatedBy(newUser.getCreatedBy());

        if (user.getCompany() != null) {
            ResCreatedDTO.CompanyUser companyUser = new ResCreatedDTO.CompanyUser();
            companyUser.setId(user.getCompany().getId());
            companyUser.setName(user.getCompany().getName());
            resCreatedDTO.setCompany(companyUser);
        }

        return resCreatedDTO;
    }

    public ResUserDTO getUserById(long id) throws IdInvalidException {
        if (!this.userRepository.existsById(id)) {
            throw new IdInvalidException("User with id " + id + " not found!");
        }
        Optional<User> optionalUser = this.userRepository.findById(id);
        if (optionalUser.isPresent()) {
            ResUserDTO resUserDTO = new ResUserDTO();
            resUserDTO.setId(optionalUser.get().getId());
            resUserDTO.setName(optionalUser.get().getName());
            resUserDTO.setEmail(optionalUser.get().getEmail());
            resUserDTO.setAge(optionalUser.get().getAge());
            resUserDTO.setGender(optionalUser.get().getGender().toString());
            resUserDTO.setAddress(optionalUser.get().getAddress());
            resUserDTO.setCreatedAt(optionalUser.get().getCreatedAt());

            if (optionalUser.get().getCompany() != null) {
                ResUserDTO.CompanyUser companyUser = new ResUserDTO.CompanyUser();
                companyUser.setId(optionalUser.get().getCompany().getId());
                companyUser.setName(optionalUser.get().getCompany().getName());
                resUserDTO.setCompany(companyUser);
            } else {
                resUserDTO.setCompany(null);
            }

            return resUserDTO;
        }
        return null;
    }

    public void deleteUserById(long id) throws IdInvalidException {
        if (!this.userRepository.existsById(id)) {
            throw new IdInvalidException("User with id " + id + " not found!");
        }
        this.userRepository.deleteById(id);
        return;
    }

    public ResUpdateDTO handleUpdateUser(long id, User user) throws IdInvalidException {

        Optional<User> optionalUser = this.userRepository.findById(id);
        if (!optionalUser.isPresent()) {
            throw new IdInvalidException("User with id " + id + " not found!");
        }

        User currentUser = (User) optionalUser.get();

        currentUser.setName(user.getName());
        currentUser.setAge(user.getAge());
        currentUser.setGender(user.getGender());
        currentUser.setAddress(user.getAddress());

        if (user.getCompany() != null) {

            currentUser.setCompany(user.getCompany());
        }

        User updatedUser = this.userRepository.save(currentUser);

        ResUpdateDTO resUpdateDTO = new ResUpdateDTO();
        resUpdateDTO.setId(updatedUser.getId());
        resUpdateDTO.setName(updatedUser.getName());
        resUpdateDTO.setEmail(updatedUser.getEmail());
        resUpdateDTO.setAge(updatedUser.getAge());
        resUpdateDTO.setGender(updatedUser.getGender().toString());
        resUpdateDTO.setAddress(updatedUser.getAddress());
        resUpdateDTO.setUpdatedAt(updatedUser.getUpdatedAt());
        resUpdateDTO.setUpdatedBy(updatedUser.getUpdatedBy());

        ResUpdateDTO.CompanyUser companyUser = new ResUpdateDTO.CompanyUser();
        if (user.getCompany() != null) {
            Optional<Company> optionalCompany = this.companyService.handleGetCompanyById(user.getCompany().getId());
            if (optionalCompany.isPresent()) {
                companyUser.setId(optionalCompany.get().getId());
                companyUser.setName(optionalCompany.get().getName());
                resUpdateDTO.setCompany(companyUser);
            }
        } else if (currentUser.getCompany() != null) {
            companyUser.setId(currentUser.getCompany().getId());
            companyUser.setName(currentUser.getCompany().getName());
            resUpdateDTO.setCompany(companyUser);
        } else {
            resUpdateDTO.setCompany(null);
        }
        return resUpdateDTO;
    }

    public User handleGetUserByUsername(String email) {
        return this.userRepository.findByEmail(email);
    }

    public boolean existsByEmail(String email) {
        return this.userRepository.existsByEmail(email);
    }

    public void handleUpdateUserToken(String token, String email) {
        User currentUser = this.handleGetUserByUsername(email);
        if (currentUser != null) {
            currentUser.setRefreshToken(token);
            this.userRepository.save(currentUser);
        }
    }

    public User handleGetUserByRefreshTokenAndEmail(String refresh_token, String email) {
        return this.userRepository.findByRefreshTokenAndEmail(refresh_token, email);
    }

    public Optional<List<User>> handleGetUserByCompany(Company company) {
        return this.userRepository.findByCompany(company);
    }
}
