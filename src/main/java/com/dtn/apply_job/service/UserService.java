package com.dtn.apply_job.service;

import com.dtn.apply_job.domain.Company;
import com.dtn.apply_job.domain.Role;
import com.dtn.apply_job.domain.User;
import com.dtn.apply_job.domain.request.user.ReqCreateUserDTO;
import com.dtn.apply_job.domain.request.user.ReqUpdateUserDTO;
import com.dtn.apply_job.domain.response.user.ResCreateUserDTO;
import com.dtn.apply_job.domain.response.user.ResUpdateUserDTO;
import com.dtn.apply_job.domain.response.user.ResUserDTO;
import com.dtn.apply_job.domain.response.user.ResultPaginationDTO;
import com.dtn.apply_job.exception.EmailExistedException;
import com.dtn.apply_job.exception.IdInvalidException;
import com.dtn.apply_job.exception.InvalidRequestException;
import com.dtn.apply_job.repository.CompanyRepository;
import com.dtn.apply_job.repository.RoleRepository;
import com.dtn.apply_job.repository.UserRepository;
import com.dtn.apply_job.util.constant.enums.ERole;
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
    private final CompanyRepository companyRepository;
    private final RoleRepository roleRepository;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, CompanyRepository companyRepository, RoleRepository roleRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.companyRepository = companyRepository;
        this.roleRepository = roleRepository;
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
            userDTO.setActive(user.getIsActive());
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

    public ResCreateUserDTO handleCreateUser(ReqCreateUserDTO reqDTO) throws EmailExistedException, IdInvalidException {
        // 1. Check Email
        if (this.userRepository.existsByEmail(reqDTO.getEmail())) {
            throw new EmailExistedException("The email address already exists in the system.");
        }

        User newUser = new User();
        newUser.setName(reqDTO.getName());
        newUser.setEmail(reqDTO.getEmail());
        newUser.setAge(reqDTO.getAge());
        newUser.setGender(reqDTO.getGender());
        newUser.setAddress(reqDTO.getAddress());

        newUser.setPassword(passwordEncoder.encode(reqDTO.getPassword()));

        if (reqDTO.getCompanyId() != null) {
            Company company = this.companyRepository.findById(reqDTO.getCompanyId())
                    .orElseThrow(() -> new IdInvalidException("Company doesn't exist!"));
            newUser.setCompany(company);
        }

        // 4. Cấp quyền (Role)
        if (reqDTO.getRoleName() != null) {
            Role role = this.roleRepository.findByName(reqDTO.getRoleName())
                    .orElseThrow(() -> new IdInvalidException("Invalid role!"));
            newUser.getRoles().add(role);
        } else {
            // Nếu KHÔNG truyền lên thì lấy quyền CANDIDATE làm mặc định
            Role defaultRole = this.roleRepository.findByName(ERole.CANDIDATE)
                    .orElseThrow(() -> new IdInvalidException("Default role 'CANDIDATE' not found in system!"));
            newUser.getRoles().add(defaultRole);
        }

        // 5. Lưu vào Database
        User savedUser = this.userRepository.save(newUser);

        // 6. Trả về Response DTO
        ResCreateUserDTO resCreatedDTO = new ResCreateUserDTO();
        resCreatedDTO.setId(savedUser.getId());
        resCreatedDTO.setName(savedUser.getName());
        resCreatedDTO.setEmail(savedUser.getEmail());
        resCreatedDTO.setAge(savedUser.getAge());
        resCreatedDTO.setGender(savedUser.getGender() != null ? savedUser.getGender().toString() : null);
        resCreatedDTO.setAddress(savedUser.getAddress());
        resCreatedDTO.setRoles(savedUser.getRoles().stream()
                .map(role -> role.getName().name())
                .toList());
        resCreatedDTO.setCreatedAt(savedUser.getCreatedAt());
        resCreatedDTO.setCreatedBy(savedUser.getCreatedBy());

        if (savedUser.getCompany() != null) {
            ResCreateUserDTO.CompanyUser companyUser = new ResCreateUserDTO.CompanyUser();
            companyUser.setId(savedUser.getCompany().getId());
            companyUser.setName(savedUser.getCompany().getName());
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

    public ResUpdateUserDTO handleUpdateUser(long id, ReqUpdateUserDTO reqUser) throws IdInvalidException, InvalidRequestException {

        Optional<User> optionalUser = this.userRepository.findById(id);
        if (!optionalUser.isPresent()) {
            throw new IdInvalidException("User with id " + id + " not found!");
        }

        User currentUser = (User) optionalUser.get();

        currentUser.setName(reqUser.getName());
        currentUser.setAge(reqUser.getAge());
        currentUser.setGender(reqUser.getGender());
        currentUser.setAddress(reqUser.getAddress());
        currentUser.setIsActive(reqUser.getIsActive());

        if (reqUser.getCompanyId() != null) {
            Company company = this.companyRepository.findById(reqUser.getCompanyId())
                    .orElseThrow(() -> new IdInvalidException("Company doesn't exist!"));
            currentUser.setCompany(company);
        }


        if (reqUser.getRoles() != null && !reqUser.getRoles().isEmpty()) {


            currentUser.getRoles().clear();


            for (ERole rName : reqUser.getRoles()) {
                Role role = this.roleRepository.findByName(rName)
                        .orElseThrow(() -> new IdInvalidException("Invalid role: " + rName));


                currentUser.getRoles().add(role);
            }
        } else {
            throw new InvalidRequestException("Role is required!");
        }

        User updatedUser = this.userRepository.save(currentUser);

        ResUpdateUserDTO resUpdateDTO = new ResUpdateUserDTO();
        resUpdateDTO.setId(updatedUser.getId());
        resUpdateDTO.setName(updatedUser.getName());
        resUpdateDTO.setEmail(updatedUser.getEmail());
        resUpdateDTO.setAge(updatedUser.getAge());
        resUpdateDTO.setGender(updatedUser.getGender() != null ? updatedUser.getGender().toString() : null);
        resUpdateDTO.setAddress(updatedUser.getAddress());
        resUpdateDTO.setRoles(updatedUser.getRoles().stream()
                .map(role -> role.getName().name())
                .toList());
        resUpdateDTO.setIsActive(updatedUser.getIsActive());
        resUpdateDTO.setUpdatedAt(updatedUser.getUpdatedAt());
        resUpdateDTO.setUpdatedBy(updatedUser.getUpdatedBy());

        if (updatedUser.getCompany() != null) {
            ResUpdateUserDTO.CompanyUser companyUser = new ResUpdateUserDTO.CompanyUser();
            companyUser.setId(updatedUser.getCompany().getId());
            companyUser.setName(updatedUser.getCompany().getName());
            resUpdateDTO.setCompany(companyUser);
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
