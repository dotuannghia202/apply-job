package com.dtn.apply_job.controller;

import com.dtn.apply_job.common.annotation.ApiMessage;
import com.dtn.apply_job.common.response.ResultPaginationDTO;
import com.dtn.apply_job.domain.User;
import com.dtn.apply_job.domain.request.user.*;
import com.dtn.apply_job.domain.response.employer.ResHrDashboardStatsDTO;
import com.dtn.apply_job.domain.response.user.ResCreateUserDTO;
import com.dtn.apply_job.domain.response.user.ResUpdateUserDTO;
import com.dtn.apply_job.domain.response.user.ResUserDTO;
import com.dtn.apply_job.exception.EmailExistedException;
import com.dtn.apply_job.exception.IdInvalidException;
import com.dtn.apply_job.service.JobService;
import com.dtn.apply_job.service.UserService;
import com.dtn.apply_job.util.constant.enums.ERole;
import com.turkraft.springfilter.boot.Filter;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
public class UserController {

    private final UserService userService;
    private final JobService jobService;


    public UserController(UserService userService, PasswordEncoder passwordEncoder, JobService jobService) {

        this.userService = userService;
        this.jobService = jobService;
    }

    @GetMapping("/users") // Không cần ghi URL (Tự hiểu là /api/v1/users)
    @PreAuthorize("hasRole('ADMIN')") // Lưu ý: Lấy danh sách toàn bộ User thì chỉ Admin mới được phép gọi
    @ApiMessage("Fetch all users")
    public ResponseEntity<ResultPaginationDTO> getAllUsers(
            @Filter Specification<User> spec, // Filter mặc định của thư viện
            Pageable pageable,
            @RequestParam(required = false) String keyword, // Dùng 1 biến keyword chung cho cả Name và Email
            @RequestParam(required = false) Boolean isActive,
            @RequestParam(required = false) ERole role // Lọc theo Enum quyền
    ) throws Exception {

        ResultPaginationDTO result = this.userService.getAllUsersWithFilters(spec, pageable, keyword, isActive, role);
        return ResponseEntity.status(HttpStatus.OK).body(result);
    }


    @PostMapping("/users")
    @ApiMessage("Create a new user")
    public ResponseEntity<ResCreateUserDTO> createUser(@RequestBody ReqCreateUserDTO user) throws EmailExistedException, IdInvalidException {

        ResCreateUserDTO result = this.userService.handleCreateUser(user);
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }


    @GetMapping("/users/{id:\\d+}")
    @PreAuthorize("isAuthenticated()") // Chỉ cần đã đăng nhập (Quyền nào cũng được)
    @ApiMessage("Fetch user by id")
    public ResponseEntity<ResUserDTO> getUserById(@PathVariable long id) throws IdInvalidException {
        ResUserDTO result = this.userService.getUserById(id);
        return ResponseEntity.ok().body(result);
    }

    @PutMapping("/users/{id:\\d+}/status")
    @PreAuthorize("hasRole('ADMIN')")
    @ApiMessage("Update user status successfully!")
    public ResponseEntity<Void> updateUserStatus(
            @PathVariable("id") long targetUserId,
            @Valid @RequestBody ReqUpdateUserStatusDTO reqDTO) throws Exception {

        userService.handleUpdateUserStatus(targetUserId, reqDTO);
        return ResponseEntity.ok().build();
    }


    @PutMapping("/users/{id}")
    @ApiMessage("Update user")
    public ResponseEntity<ResUpdateUserDTO> updateUser(@PathVariable long id, @RequestBody ReqUpdateUserDTO user) throws IdInvalidException {
        ResUpdateUserDTO result = this.userService.handleUpdateUser(id, user);
        return ResponseEntity.ok().body(result);
    }

    @PostMapping("/users/save-job/{jobId}")
    @ApiMessage("On/off jobs saved status for current user")
    public ResponseEntity<Boolean> toggleSaveJob(@PathVariable Long jobId) throws Exception {
        boolean isSaved = jobService.toggleSavedJob(jobId);
        return ResponseEntity.ok(isSaved);
    }

    @PutMapping("/users/assign-company/{companyId}")
    @ApiMessage("Assign company for employer")
    public ResponseEntity<Void> assignCompany(@PathVariable Long companyId) throws Exception {
        userService.assignCompanyToCurrentUser(companyId);
        return ResponseEntity.ok().body(null);
    }

    @PutMapping("users/{id:\\d+}/roles")
    @PreAuthorize("hasAnyRole('ADMIN', 'CANDIDATE', 'EMPLOYER')")
    @ApiMessage("User permissions update successful!")
    public ResponseEntity<ResUpdateUserDTO> updateUserRoles(
            @PathVariable("id") long targetUserId,
            @Valid @RequestBody ReqUpdateUserRoleDTO reqDTO) throws Exception {

        ResUpdateUserDTO result = userService.handleUpdateUserRoles(targetUserId, reqDTO);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/saved-jobs")
    @PreAuthorize("hasRole('CANDIDATE')") // Chặn HR
    @ApiMessage("Fetch jobs is saved")
    public ResponseEntity<ResultPaginationDTO> getMySavedJobs(Pageable pageable) throws Exception {
        ResultPaginationDTO result = jobService.handleGetSavedJobs(pageable);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/hr/dashboard-stats")
    @PreAuthorize("hasRole('EMPLOYER')")
    @ApiMessage("Get stats for dashboard employer")
    public ResponseEntity<ResHrDashboardStatsDTO> getHrDashboardStats() throws Exception {
        ResHrDashboardStatsDTO result = userService.getHrDashboardStats();
        return ResponseEntity.ok(result);
    }

    @PutMapping("/change-password")
    @PreAuthorize("isAuthenticated()") // Anyone logged in can change their password
    @ApiMessage("Password changed successfully")
    public ResponseEntity<Void> changePassword(@Valid @RequestBody ReqChangePasswordDTO reqDTO) throws Exception {
        userService.handleChangePassword(reqDTO);
        return ResponseEntity.ok().build();
    }

}
