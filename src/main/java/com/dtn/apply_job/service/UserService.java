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
import jakarta.persistence.criteria.*;
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
        if (this.userRepository.existsByEmail(reqDTO.getEmail())) {
            throw new EmailExistedException("Địa chỉ email đã tồn tại trong hệ thống.");
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
                    .orElseThrow(() -> new IdInvalidException("Công ty không tồn tại!"));
            newUser.setCompany(company);
        }

        if (reqDTO.getRoleName() != null) {
            Role role = this.roleRepository.findByName(reqDTO.getRoleName())
                    .orElseThrow(() -> new IdInvalidException("Vai trò không hợp lệ!"));
            newUser.getRoles().add(role);
        } else {
            Role defaultRole = this.roleRepository.findByName(ERole.CANDIDATE)
                    .orElseThrow(() -> new IdInvalidException("Vai trò mặc định 'CANDIDATE' không tìm thấy trong hệ thống!"));
            newUser.getRoles().add(defaultRole);
        }

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
        User targetUser = this.userRepository.findById(id)
                .orElseThrow(() -> new IdInvalidException("Không tìm thấy người dùng với id " + id + "!"));

        String email = SecurityUtil.getCurrentUser()
                .orElseThrow(() -> new AccessDeniedException("Vui lòng đăng nhập!"));
        User currentUser = userRepository.findByEmail(email);

        boolean isAdmin = currentUser.getRoles().stream()
                .anyMatch(r -> r.getName().name().equals("ROLE_ADMIN") || r.getName().name().equals("ADMIN"));

        if (!isAdmin && currentUser.getId() != id) {
            throw new AccessDeniedException("Lỗi bảo mật: Bạn không có quyền xem thông tin cá nhân của người khác!");
        }

        ResUserDTO resUserDTO = new ResUserDTO();
        resUserDTO.setId(targetUser.getId());
        resUserDTO.setName(targetUser.getName());
        resUserDTO.setActive(targetUser.getIsActive());
        resUserDTO.setAvatarUrl(targetUser.getAvatarUrl());
        resUserDTO.setEmail(targetUser.getEmail());
        resUserDTO.setAge(targetUser.getAge());

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
                .orElseThrow(() -> new IdInvalidException("Người dùng không tồn tại!"));

        boolean wasActive = targetUser.getIsActive();
        boolean willBeActive = reqDTO.getIsActive();

        targetUser.setIsActive(willBeActive);
        this.userRepository.save(targetUser);

        if (wasActive == true && willBeActive == false) {
            new Thread(() -> {
                emailService.sendAccountLockedEmail(targetUser.getEmail(), targetUser.getName());
            }).start();
        }
    }

    public ResUpdateUserDTO handleUpdateUser(long id, ReqUpdateUserDTO reqUser) throws IdInvalidException, InvalidRequestException {

        String email = SecurityUtil.getCurrentUser()
                .orElseThrow(() -> new IdInvalidException("Vui lòng đăng nhập!"));
        User loggedInUser = userRepository.findByEmail(email);

        boolean isAdmin = loggedInUser.getRoles().stream()
                .anyMatch(r -> r.getName().name().equals("ROLE_ADMIN") || r.getName().name().equals("ADMIN"));

        if (!isAdmin && loggedInUser.getId() != id) {
            throw new AccessDeniedException("Lỗi bảo mật: Bạn không có quyền cập nhật thông tin của người khác!");
        }

        User currentUser = this.userRepository.findById(id)
                .orElseThrow(() -> new IdInvalidException("Không tìm thấy người dùng với id " + id + "!"));

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
        String email = SecurityUtil.getCurrentUser()
                .orElseThrow(() -> new IdInvalidException("Vui lòng đăng nhập!"));
        User currentUser = userRepository.findByEmail(email);

        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new IdInvalidException("Công ty không tồn tại!"));

        currentUser.setCompany(company);

        User updatedUser = userRepository.save(currentUser);

        return;
    }

    @Transactional
    public ResUpdateUserDTO handleUpdateUserRoles(long targetUserId, ReqUpdateUserRoleDTO reqDTO) throws Exception, AccessDeniedException {

        String email = SecurityUtil.getCurrentUser().orElseThrow();
        User currentUser = userRepository.findByEmail(email);

        boolean isAdmin = currentUser.getRoles().stream()
                .anyMatch(r -> r.getName().name().equals("ROLE_ADMIN"));

        if (!isAdmin && currentUser.getId() != targetUserId) {
            throw new Exception("Lỗi bảo mật: Bạn không có quyền thay đổi quyền truy cập của người khác!");
        }

        if (!isAdmin) {
            boolean wantsAdminRole = reqDTO.getRoles().contains(ERole.ADMIN);
            if (wantsAdminRole) {
                throw new AccessDeniedException("Lỗi bảo mật: Bạn không có quyền thay đổi quyền truy cập của người khác!");
            }
        }

        User targetUser = userRepository.findById(targetUserId)
                .orElseThrow(() -> new IdInvalidException("Không tìm thấy người dùng!"));

        targetUser.getRoles().clear();
        for (ERole roleName : reqDTO.getRoles()) {
            Role role = roleRepository.findByName(roleName)
                    .orElseThrow(() -> new IdInvalidException("Quyền không hợp lệ: " + roleName));
            targetUser.getRoles().add(role);
        }

        User updatedUser = userRepository.save(targetUser);

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
        String email = SecurityUtil.getCurrentUser().orElseThrow();
        User currentHr = userRepository.findByEmail(email);

        if (currentHr.getCompany() == null) {
            throw new Exception("Bạn chưa tham gia vào công ty nào!");
        }

        Long companyId = currentHr.getCompany().getId();

        long activeJobs = jobRepository.countByCompany_IdAndActiveTrue(companyId);
        long totalApplicants = applicationRepository.countByJob_Company_Id(companyId);
        Double avgScore = applicationRepository.getAverageMatchScoreByCompanyId(companyId);

        ResHrDashboardStatsDTO stats = new ResHrDashboardStatsDTO();
        stats.setTotalActiveJobs(activeJobs);
        stats.setTotalApplicants(totalApplicants);

        stats.setAvgAiMatchRate(avgScore != null ? Math.round(avgScore * 10.0) / 10.0 : 0.0);

        return stats;
    }

    @Transactional
    public void handleChangePassword(ReqChangePasswordDTO reqDTO) throws Exception {
        if (!reqDTO.getNewPassword().equals(reqDTO.getConfirmPassword())) {
            throw new Exception("Mật khẩu mới và xác nhận mật khẩu không khớp!");
        }

        String email = SecurityUtil.getCurrentUser().orElseThrow(() -> new IdInvalidException("Vui lòng đăng nhập!"));
        User currentUser = userRepository.findByEmail(email);

        boolean isOldPasswordCorrect = passwordEncoder.matches(reqDTO.getOldPassword(), currentUser.getPassword());
        if (!isOldPasswordCorrect) {
            throw new Exception("Mật khẩu hiện tại không chính xác!");
        }

        currentUser.setPassword(passwordEncoder.encode(reqDTO.getNewPassword()));
        userRepository.save(currentUser);
    }

    private Specification<User> buildUserFilterSpec(String keyword, Boolean isActive, ERole role) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            boolean needsDistinct = false;

            if (keyword != null && !keyword.trim().isEmpty()) {
                String pattern = "%" + keyword.trim().toLowerCase() + "%";
                Predicate byName = cb.like(cb.lower(root.get("name")), pattern);
                Predicate byEmail = cb.like(cb.lower(root.get("email")), pattern);
                predicates.add(cb.or(byName, byEmail));
            }

            if (isActive != null) {
                predicates.add(cb.equal(root.get("isActive"), isActive));
            }

            if (role != null) {
                Join<User, Role> roleJoin = root.join("roles", JoinType.INNER);
                predicates.add(cb.equal(roleJoin.get("name"), role));
                needsDistinct = true;
            }

            if (needsDistinct) {
                query.distinct(true);
            }

            return predicates.isEmpty() ? cb.conjunction() : cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    public ResultPaginationDTO getAllUsersWithFilters(
            Specification<User> spec,
            Pageable pageable,
            String keyword,
            Boolean isActive,
            ERole role
    ) {
        Specification<User> filterSpec = buildUserFilterSpec(keyword, isActive, role);

        Specification<User> excludeAdminSpec = (root, query, cb) -> {
            Subquery<Long> subquery = query.subquery(Long.class);
            Root<User> subRoot = subquery.from(User.class);
            Join<Object, Object> subRoles = subRoot.join("roles");

            subquery.select(subRoot.get("id"))
                    .where(cb.equal(subRoles.get("name"), ERole.ADMIN));

            return cb.not(root.get("id").in(subquery));
        };

        Specification<User> combinedSpec = spec == null ? filterSpec : spec.and(filterSpec);

        combinedSpec = combinedSpec == null ? excludeAdminSpec : combinedSpec.and(excludeAdminSpec);

        return getAllUsers(combinedSpec, pageable);
    }
}
