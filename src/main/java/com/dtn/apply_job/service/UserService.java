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
import com.dtn.apply_job.mapper.UserMapper;
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
import java.util.UUID;

@Service
public class UserService {
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final CompanyRepository companyRepository;
    private final RoleRepository roleRepository;
    private final JobRepository jobRepository;
    private final ApplicationRepository applicationRepository;
    private final EmailService emailService;
    private final UserMapper userMapper;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, CompanyRepository companyRepository, RoleRepository roleRepository, JobRepository jobRepository, ApplicationRepository applicationRepository, EmailService emailService, UserMapper userMapper) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.companyRepository = companyRepository;
        this.roleRepository = roleRepository;
        this.jobRepository = jobRepository;
        this.applicationRepository = applicationRepository;
        this.emailService = emailService;
        this.userMapper = userMapper;
    }

    public ResultPaginationDTO getAllUsers(Specification<User> spec, Pageable pageable) {
        Page<User> pageUser = this.userRepository.findAll(spec, pageable);

        List<ResUserDTO> results = pageUser.getContent().stream()
                .map(this.userMapper::toResUserDTO)
                .toList();

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

        User newUser = userMapper.toUser(reqDTO);
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
        return this.userMapper.toResCreateUserDTO(savedUser);
    }

    public ResUserDTO getUserById(UUID id) throws IdInvalidException {
        User targetUser = this.userRepository.findById(id)
                .orElseThrow(() -> new IdInvalidException("Không tìm thấy người dùng với id " + id + "!"));

        String email = SecurityUtil.getCurrentUser()
                .orElseThrow(() -> new AccessDeniedException("Please log in to continue!"));
        User currentUser = userRepository.findByEmail(email);

        boolean isAdmin = currentUser.getRoles().stream()
                .anyMatch(r -> r.getName().name().equals("ROLE_ADMIN") || r.getName().name().equals("ADMIN"));

        if (!isAdmin && !currentUser.getId().equals(id)) {
            throw new AccessDeniedException("Lỗi bảo mật: Bạn không có quyền xem thông tin cá nhân của người khác!");
        }

        return this.userMapper.toResUserDTO(targetUser);
    }

    @Transactional
    public void handleUpdateUserStatus(UUID targetUserId, ReqUpdateUserStatusDTO reqDTO) throws IdInvalidException {
        User targetUser = this.userRepository.findById(targetUserId)
                .orElseThrow(() -> new IdInvalidException("User not found!"));

        boolean wasActive = targetUser.getIsActive();
        boolean willBeActive = reqDTO.getIsActive();

        targetUser.setIsActive(willBeActive);
        this.userRepository.save(targetUser);

        if (wasActive == true && willBeActive == false) {
            String lang = targetUser.getPreferredLanguage();
            if (lang == null || lang.isBlank()) {
                lang = "vi";
            }
            final String userLang = lang;
            new Thread(() -> {
                emailService.sendAccountLockedEmail(targetUser.getEmail(), targetUser.getName(), userLang);
            }).start();
        }
    }

    public ResUpdateUserDTO handleUpdateUser(UUID id, ReqUpdateUserDTO reqUser) throws IdInvalidException, InvalidRequestException {

        String email = SecurityUtil.getCurrentUser()
                .orElseThrow(() -> new IdInvalidException("Please log in to continue!"));
        User loggedInUser = userRepository.findByEmail(email);

        boolean isAdmin = loggedInUser.getRoles().stream()
                .anyMatch(r -> r.getName().name().equals("ROLE_ADMIN") || r.getName().name().equals("ADMIN"));

        if (!isAdmin && !loggedInUser.getId().equals(id)) {
            throw new AccessDeniedException("Lỗi bảo mật: Bạn không có quyền cập nhật thông tin của người khác!");
        }

        User currentUser = this.userRepository.findById(id)
                .orElseThrow(() -> new IdInvalidException("Không tìm thấy người dùng với id " + id + "!"));

        userMapper.updateUserFromDTO(reqUser, currentUser);

        User updatedUser = this.userRepository.save(currentUser);

        return this.userMapper.toResUpdateUserDTO(updatedUser);
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
    public void assignCompanyToCurrentUser(UUID companyId) throws Exception {
        String email = SecurityUtil.getCurrentUser()
                .orElseThrow(() -> new IdInvalidException("Please log in to continue!"));
        User currentUser = userRepository.findByEmail(email);

        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new IdInvalidException("Công ty không tồn tại!"));

        currentUser.setCompany(company);

        User updatedUser = userRepository.save(currentUser);

        return;
    }

    @Transactional
    public void handleUpdatePreferredLanguage(String language) throws IdInvalidException {
        if (language == null || (!language.equalsIgnoreCase("vi") && !language.equalsIgnoreCase("en"))) {
            language = "vi";
        }
        String email = SecurityUtil.getCurrentUser()
                .orElseThrow(() -> new IdInvalidException("Please log in to continue!"));
        User user = userRepository.findByEmail(email);
        if (user != null) {
            user.setPreferredLanguage(language.toLowerCase());
            userRepository.save(user);
        }
    }

    public ResUpdateUserDTO handleUpdateUserRoles(UUID targetUserId, ReqUpdateUserRoleDTO reqDTO) throws Exception, AccessDeniedException {

        String email = SecurityUtil.getCurrentUser().orElseThrow();
        User currentUser = userRepository.findByEmail(email);

        boolean isAdmin = currentUser.getRoles().stream()
                .anyMatch(r -> r.getName().name().equals("ROLE_ADMIN"));

        if (!isAdmin && !currentUser.getId().equals(targetUserId)) {
            throw new Exception("Security error: You do not have permission to modify another user's roles!");
        }

        if (!isAdmin) {
            boolean wantsAdminRole = reqDTO.getRoles().contains(ERole.ADMIN);
            if (wantsAdminRole) {
                throw new AccessDeniedException("Security error: You do not have permission to modify another user's roles!");
            }
        }

        User targetUser = userRepository.findById(targetUserId)
                .orElseThrow(() -> new IdInvalidException("User not found!"));

        targetUser.getRoles().clear();
        for (ERole roleName : reqDTO.getRoles()) {
            Role role = roleRepository.findByName(roleName)
                    .orElseThrow(() -> new IdInvalidException("Invalid role: " + roleName));
            targetUser.getRoles().add(role);
        }

        User updatedUser = userRepository.save(targetUser);

        return this.userMapper.toResUpdateUserDTO(updatedUser);
    }

    public ResHrDashboardStatsDTO getHrDashboardStats() throws Exception {
        String email = SecurityUtil.getCurrentUser().orElseThrow();
        User currentHr = userRepository.findByEmail(email);

        if (currentHr.getCompany() == null) {
            throw new Exception("Bạn chưa tham gia vào công ty nào!");
        }

        UUID companyId = currentHr.getCompany().getId();

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

        String email = SecurityUtil.getCurrentUser().orElseThrow(() -> new IdInvalidException("Please log in to continue!"));
        User currentUser = userRepository.findByEmail(email);

        boolean isOldPasswordCorrect = passwordEncoder.matches(reqDTO.getOldPassword(), currentUser.getPassword());
        if (!isOldPasswordCorrect) {
            throw new Exception("Current password is incorrect!");
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
            Subquery<UUID> subquery = query.subquery(UUID.class);
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
