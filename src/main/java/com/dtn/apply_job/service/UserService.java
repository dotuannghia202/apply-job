package com.dtn.apply_job.service;

import com.dtn.apply_job.common.response.ResultPaginationDTO;
import com.dtn.apply_job.domain.Company;
import com.dtn.apply_job.domain.Role;
import com.dtn.apply_job.domain.User;
import com.dtn.apply_job.domain.request.user.*;
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
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;
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
    private final EmailService emailService;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, CompanyRepository companyRepository, RoleRepository roleRepository, JobRepository jobRepository, ApplicationRepository applicationRepository, EmailService emailService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.companyRepository = companyRepository;
        this.roleRepository = roleRepository;
        this.jobRepository = jobRepository;
        this.applicationRepository = applicationRepository;
        this.emailService = emailService;
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
            userDTO.setRoles(user.getRoles().stream()
                    .map(role -> role.getName().name())
                    .toList());

            if (user.getCompany() != null) {
                ResUserDTO.CompanyUser companyUserDTO = new ResUserDTO.CompanyUser();
                companyUserDTO.setId(user.getCompany().getId());
                companyUserDTO.setName(user.getCompany().getName());
                companyUserDTO.setLogo(user.getCompany().getLogo());
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

    // Nhớ import cái này ở đầu file nhé:
    // import org.springframework.security.access.AccessDeniedException;

    public ResUserDTO getUserById(long id) throws IdInvalidException {
        // 1. TÌM USER TRONG DATABASE (Gộp existsById và findById thành 1 câu cho tối ưu)
        User targetUser = this.userRepository.findById(id)
                .orElseThrow(() -> new IdInvalidException("User with id " + id + " not found!"));

        // =================================================================
        // 2. RÀO CẢN BẢO MẬT (CHỐNG LỖI IDOR - XEM TRỘM)
        // =================================================================
        String email = SecurityUtil.getCurrentUser()
                .orElseThrow(() -> new AccessDeniedException("Please login!"));
        User currentUser = userRepository.findByEmail(email);

        boolean isAdmin = currentUser.getRoles().stream()
                .anyMatch(r -> r.getName().name().equals("ROLE_ADMIN") || r.getName().name().equals("ADMIN"));

        // Nếu KHÔNG phải Admin VÀ ID muốn xem KHÁC VỚI ID của chính mình -> CHẶN NGAY LẬP TỨC!
        if (!isAdmin && currentUser.getId() != id) {
            throw new AccessDeniedException("Security Error: You do not have permission to view other people's personal information!");
        }
        // =================================================================

        // 3. ĐỔ DỮ LIỆU SANG DTO VÀ TRẢ VỀ
        ResUserDTO resUserDTO = new ResUserDTO();
        resUserDTO.setId(targetUser.getId());
        resUserDTO.setName(targetUser.getName());
        resUserDTO.setAvatarUrl(targetUser.getAvatarUrl());
        resUserDTO.setEmail(targetUser.getEmail());
        resUserDTO.setAge(targetUser.getAge());

        // Fix lỗi sập Server (NPE) nếu gender đang bị null
        resUserDTO.setGender(targetUser.getGender() != null ? targetUser.getGender().toString() : null);

        resUserDTO.setAddress(targetUser.getAddress());
        resUserDTO.setCreatedAt(targetUser.getCreatedAt());
        resUserDTO.setRoles(targetUser.getRoles().stream()
                .map(role -> role.getName().name())
                .toList());

        if (targetUser.getCompany() != null) {
            ResUserDTO.CompanyUser companyUser = new ResUserDTO.CompanyUser();
            companyUser.setId(targetUser.getCompany().getId());
            companyUser.setName(targetUser.getCompany().getName());
            companyUser.setLogo(targetUser.getCompany().getLogo());
            resUserDTO.setCompany(companyUser);
        } else {
            resUserDTO.setCompany(null);
        }

        return resUserDTO;
    }

    @Transactional
    public void handleUpdateUserStatus(long targetUserId, ReqUpdateUserStatusDTO reqDTO) throws IdInvalidException {
        User targetUser = this.userRepository.findById(targetUserId)
                .orElseThrow(() -> new IdInvalidException("The user does not exist!"));

        // 1. Lưu lại trạng thái cũ để so sánh
        boolean wasActive = targetUser.getIsActive();
        boolean willBeActive = reqDTO.getIsActive();

        // 2. Cập nhật trạng thái mới
        targetUser.setIsActive(willBeActive);
        this.userRepository.save(targetUser);

        // 3. LOGIC GỬI EMAIL: Chỉ gửi khi trạng thái chuyển từ Mở (true) sang Khóa (false)
        // Chạy một luồng ngầm (Thread) để không làm chậm thao tác của Admin
        if (wasActive == true && willBeActive == false) {
            new Thread(() -> {
                emailService.sendAccountLockedEmail(targetUser.getEmail(), targetUser.getName());
            }).start();
        }
    }

    public ResUpdateUserDTO handleUpdateUser(long id, ReqUpdateUserDTO reqUser) throws IdInvalidException, InvalidRequestException {

        // --- 1. RÀO CẢN BẢO MẬT (CHỐNG LỖI IDOR) ---
        String email = SecurityUtil.getCurrentUser()
                .orElseThrow(() -> new IdInvalidException("Please login!"));
        User loggedInUser = userRepository.findByEmail(email);

        boolean isAdmin = loggedInUser.getRoles().stream()
                .anyMatch(r -> r.getName().name().equals("ROLE_ADMIN") || r.getName().name().equals("ADMIN"));

        // Nếu KHÔNG phải Admin VÀ đang cố sửa ID của người khác -> CHẶN!
        if (!isAdmin && loggedInUser.getId() != id) {
            throw new AccessDeniedException("Security Error: You do not have permission to update other people's information!");
        }

        User currentUser = this.userRepository.findById(id)
                .orElseThrow(() -> new IdInvalidException("User with id " + id + " not found!"));

        // Cập nhật các trường cơ bản
        if (reqUser.getName() != null) currentUser.setName(reqUser.getName());
        if (reqUser.getAvatarUrl() != null) currentUser.setAvatarUrl(reqUser.getAvatarUrl());
        if (reqUser.getAge() > 0) currentUser.setAge(reqUser.getAge());
        if (reqUser.getGender() != null) currentUser.setGender(reqUser.getGender());
        if (reqUser.getAddress() != null) currentUser.setAddress(reqUser.getAddress());

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
    public ResUpdateUserDTO handleUpdateUserRoles(long targetUserId, ReqUpdateUserRoleDTO reqDTO) throws Exception, AccessDeniedException {

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
                throw new AccessDeniedException("Security Error: You do not have permission to change other people's access rights!");
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

    @Transactional
    public void handleChangePassword(ReqChangePasswordDTO reqDTO) throws Exception {
        // 1. Verify if the new password and confirm password match
        if (!reqDTO.getNewPassword().equals(reqDTO.getConfirmPassword())) {
            throw new Exception("New password and confirm password do not match!");
        }

        // 2. Get the currently logged-in user
        String email = SecurityUtil.getCurrentUser().orElseThrow(() -> new IdInvalidException("Please log in!"));
        User currentUser = userRepository.findByEmail(email);

        // 3. Verify if the current password is correct
        boolean isOldPasswordCorrect = passwordEncoder.matches(reqDTO.getOldPassword(), currentUser.getPassword());
        if (!isOldPasswordCorrect) {
            throw new Exception("Current password is incorrect!");
        }

        // 4. Encode the new password and save to the database
        currentUser.setPassword(passwordEncoder.encode(reqDTO.getNewPassword()));
        userRepository.save(currentUser);
    }

    private Specification<User> buildUserFilterSpec(String keyword, Boolean isActive, ERole role) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            boolean needsDistinct = false;

            // 1. LỌC THEO TỪ KHÓA (TÌM TÊN HOẶC EMAIL) -> Dùng OR
            if (keyword != null && !keyword.trim().isEmpty()) {
                String pattern = "%" + keyword.trim().toLowerCase() + "%";
                Predicate byName = cb.like(cb.lower(root.get("name")), pattern);
                Predicate byEmail = cb.like(cb.lower(root.get("email")), pattern);
                predicates.add(cb.or(byName, byEmail));
            }

            // 2. LỌC THEO TRẠNG THÁI ACTIVE
            if (isActive != null) {
                predicates.add(cb.equal(root.get("isActive"), isActive));
            }

            // 3. LỌC THEO QUYỀN (ROLE) -> Phải JOIN vào bảng Roles
            if (role != null) {
                Join<User, Role> roleJoin = root.join("roles", JoinType.INNER);
                predicates.add(cb.equal(roleJoin.get("name"), role));
                needsDistinct = true; // Bật cờ Distinct để tránh bị lặp data do JOIN N-N
            }

            // Bật DISTINCT nếu có join bảng
            if (needsDistinct) {
                query.distinct(true);
            }

            return predicates.isEmpty() ? cb.conjunction() : cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    // 2. HÀM GỌI API CHO CONTROLLER SỬ DỤNG
    // =========================================================
    public ResultPaginationDTO getAllUsersWithFilters(
            Specification<User> spec,
            Pageable pageable,
            String keyword,
            Boolean isActive,
            ERole role
    ) {
        // Build lớp giáp bộ lọc tùy chỉnh
        Specification<User> filterSpec = buildUserFilterSpec(keyword, isActive, role);

        // Nối lớp giáp với bộ lọc của Frontend (Nếu có)
        Specification<User> combinedSpec = spec == null ? filterSpec : spec.and(filterSpec);

        // Gọi hàm getAllUsers CŨ của bạn truyền Specification vào
        return getAllUsers(combinedSpec, pageable);
    }
}
