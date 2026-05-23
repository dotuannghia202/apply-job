package com.dtn.apply_job.controller;

import com.dtn.apply_job.common.annotation.ApiMessage;
import com.dtn.apply_job.domain.response.admin.ResAdminDashboardDTO;
import com.dtn.apply_job.service.DashboardService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin")
public class AdminController {

    private final DashboardService dashboardService;

    public AdminController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/dashboard-stats")
    @PreAuthorize("hasRole('ADMIN')") // 🚨 BỨC TƯỜNG LỬA: CHỈ ADMIN MỚI VÀO ĐƯỢC
    @ApiMessage("Admin Dashboard statistics data retrieved successfully")
    public ResponseEntity<ResAdminDashboardDTO> getAdminDashboardStats() {

        ResAdminDashboardDTO result = dashboardService.getAdminDashboardStats();
        return ResponseEntity.ok(result);
    }
}