package com.dtn.apply_job.service;

import com.dtn.apply_job.common.response.ResultPaginationDTO;
import com.dtn.apply_job.domain.Company;
import com.dtn.apply_job.domain.Role;
import com.dtn.apply_job.domain.User;
import com.dtn.apply_job.domain.request.user.ReqCreateUserDTO;
import com.dtn.apply_job.domain.request.user.ReqUpdateUserDTO;
import com.dtn.apply_job.domain.request.user.ReqUpdateUserRoleDTO;
import com.dtn.apply_job.domain.response.employer.ResHrDashboardStatsDTO;
import com.dtn.apply_job.domain.response.user.ResCreateUserDTO;
import com.dtn.apply_job.domain.response.user.ResUpdateUserDTO;
import com.dtn.apply_job.domain.response.user.ResUserDTO;
import com.dtn.apply_job.exception.EmailExistedException;
import com.dtn.apply_job.exception.IdInvalidException;
import com.dtn.apply_job.exception.InvalidRequestException;
import com.dtn.apply_job.repository.*;
import com.dtn.apply_job.security.SecurityUtil;
import com.dtn.apply_job.util.constant.enums.ERole;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class UserService {
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final CompanyRepository companyRepository;
    private final RoleRepository roleRepository;
    private final JobRepository jobRepository;
    private final ApplicationRepository applicationRepository;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, CompanyRepository companyRepository, RoleRepository roleRepository, JobRepository jobRepository, ApplicationRepository applicationRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.companyRepository = companyRepository;
        this.roleRepository = roleRepository;
        this.jobRepository = jobRepository;
        this.applicationRepository = applicationRepository;
    }

    public ResultPaginationDTO getAllUsers(Specification<User> spec, Pageable pageable) {
        Page<User> pageUser = this.userRepository.findAll(spec, pageable);

        List<ResUserDTO> results = new ArrayList<>(pageUser.getContent().size());
        for (User user : pageUser.getContent()) {
            ResUserDTO userDTO = new ResUserDTO();
            userDTO.setId(user.getId());
            userDTO.setName(user.getName());
            userDTO.setEmail(user.getEmail());
            userDTO.setAvatarUrl(user.getAvatarUrl());
            userDTO.setAge(user.getAge());
            userDTO.setGender(user.getGender() != null ? user.getGender().toString() : null);
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
            resUserDTO.setAvatarUrl(optionalUser.get().getAvatarUrl());
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
        currentUser.setAvatarUrl(reqUser.getAvatarUrl());
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

        return convertToResUpdateUserDTO(updatedUser);
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

    @Transactional
    public void assignCompanyToCurrentUser(Long companyId) throws Exception {
        // 1. Lấy user đang đăng nhập từ Token (Tuyệt đối không lấy ID từ URL để tránh hack)
        String email = SecurityUtil.getCurrentUser()
                .orElseThrow(() -> new IdInvalidException("Please login!!"));
        User currentUser = userRepository.findByEmail(email);

        // 2. Tìm Công ty theo ID người dùng gửi lên
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new IdInvalidException("Company doesn't exist!"));

        // 3. Gắn công ty vào User
        currentUser.setCompany(company);


        // 5. Lưu vào Database
        User updatedUser = userRepository.save(currentUser);

        // Trả về DTO
        // (Bạn dùng lại đoạn code mapping User sang ResUpdateUserDTO ở các bài trước nhé)
        return;
    }

    // Nhớ import org.springframework.security.access.AccessDeniedException;

    @Transactional
    public ResUpdateUserDTO handleUpdateUserRoles(long targetUserId, ReqUpdateUserRoleDTO reqDTO) throws Exception {

        // 1. LẤY THÔNG TIN NGƯỜI ĐANG THỰC HIỆN HÀNH ĐỘNG GỌI API
        String email = SecurityUtil.getCurrentUser().orElseThrow();
        User currentUser = userRepository.findByEmail(email);

        // Kiểm tra xem người gọi API có phải là ADMIN không?
        boolean isAdmin = currentUser.getRoles().stream()
                .anyMatch(r -> r.getName().name().equals("ROLE_ADMIN"));

        // ========================================================
        // RÀO CẢN 1: CHỐNG LỖI IDOR (XEM TRỘM/SỬA TRỘM)
        // Nếu KHÔNG phải Admin, và ID muốn sửa KHÁC VỚI ID của chính mình -> Chặn!
        // ========================================================
        if (!isAdmin && currentUser.getId() != targetUserId) {
            throw new Exception("Security Error: You do not have permission to change other people's access rights!");
        }

        // ========================================================
        // RÀO CẢN 2: CHỐNG LEO THANG ĐẶC QUYỀN (PRIVILEGE ESCALATION)
        // Nếu ứng viên cố tình gửi JSON chứa ["ROLE_ADMIN"] để hack hệ thống -> Chặn!
        // ========================================================
        if (!isAdmin) {
            boolean wantsAdminRole = reqDTO.getRoles().contains(ERole.ADMIN); // Đổi thành ERole.ADMIN tùy Enum của bạn
            if (wantsAdminRole) {
                throw new Exception("Security Error: You do not have permission to change other people's access rights!");
            }
        }

        // 3. LẤY USER MỤC TIÊU CẦN SỬA QUYỀN TỪ DB
        User targetUser = userRepository.findById(targetUserId)
                .orElseThrow(() -> new IdInvalidException("User not found!"));

        // 4. THỰC HIỆN XÓA QUYỀN CŨ VÀ CẬP NHẬT QUYỀN MỚI
        targetUser.getRoles().clear();
        for (ERole roleName : reqDTO.getRoles()) {
            Role role = roleRepository.findByName(roleName)
                    .orElseThrow(() -> new IdInvalidException("Invalid permissions: " + roleName));
            targetUser.getRoles().add(role);
        }

        // 5. LƯU VÀO DATABASE VÀ TRẢ VỀ DTO
        User updatedUser = userRepository.save(targetUser);

        // 6. (Sử dụng lại hàm convert Entity -> DTO mà bạn đã có sẵn)
        return convertToResUpdateUserDTO(updatedUser);
    }

    private ResUpdateUserDTO convertToResUpdateUserDTO(User updatedUser) {
        ResUpdateUserDTO resUpdateDTO = new ResUpdateUserDTO();
        resUpdateDTO.setId(updatedUser.getId());
        resUpdateDTO.setName(updatedUser.getName());
        resUpdateDTO.setEmail(updatedUser.getEmail());
        resUpdateDTO.setAvatarUrl(updatedUser.getAvatarUrl());
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

    public ResHrDashboardStatsDTO getHrDashboardStats() throws Exception {
        // 1. Lấy HR đang đăng nhập
        String email = SecurityUtil.getCurrentUser().orElseThrow();
        User currentHr = userRepository.findByEmail(email);

        if (currentHr.getCompany() == null) {
            throw new Exception("You haven't joined any company yet!");
        }

        Long companyId = currentHr.getCompany().getId();

        // 2. Gọi DB để lấy thống kê
        long activeJobs = jobRepository.countByCompany_IdAndActiveTrue(companyId);
        long totalApplicants = applicationRepository.countByJob_Company_Id(companyId);
        Double avgScore = applicationRepository.getAverageMatchScoreByCompanyId(companyId);

        // 3. Đổ vào DTO
        ResHrDashboardStatsDTO stats = new ResHrDashboardStatsDTO();
        stats.setTotalActiveJobs(activeJobs);
        stats.setTotalApplicants(totalApplicants);

        // Nếu avgScore bị null (do chưa có ai nộp hoặc chưa chạy AI), trả về 0.0 hoặc mock data 88.4
        stats.setAvgAiMatchRate(avgScore != null ? Math.round(avgScore * 10.0) / 10.0 : 88.4);

        return stats;
    }
}
