package com.dtn.apply_job.controller;

import com.dtn.apply_job.common.annotation.ApiMessage;
import com.dtn.apply_job.common.response.ResultPaginationDTO;
import com.dtn.apply_job.domain.Application;
import com.dtn.apply_job.domain.request.application.ReqCreateApplicationDTO;
import com.dtn.apply_job.domain.request.application.ReqUpdateAppByCandidateDTO;
import com.dtn.apply_job.domain.request.application.ReqUpdateApplicationStatusDTO;
import com.dtn.apply_job.domain.response.application.ResApplicationDTO;
import com.dtn.apply_job.domain.response.application.ResCreateApplicationDTO;
import com.dtn.apply_job.domain.response.application.ResUpdateApplicationDTO;
import com.dtn.apply_job.service.ApplicationService;
import com.turkraft.springfilter.boot.Filter;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/applications")
public class ApplicationController {

    private final ApplicationService applicationService;

    public ApplicationController(ApplicationService applicationService) {
        this.applicationService = applicationService;
    }

    @PostMapping
    @PreAuthorize("hasRole('CANDIDATE')")
    @ApiMessage("Successfully created a recruitment profile")
    public ResponseEntity<ResCreateApplicationDTO> create(@Valid @RequestBody ReqCreateApplicationDTO reqDTO) throws Exception {
        return ResponseEntity.status(HttpStatus.CREATED).body(applicationService.handleCreateApplication(reqDTO));
    }

    @GetMapping
    @ApiMessage("Get list applications")
    // Mở cho cả 3 Role, dữ liệu sẽ được Service tự động lọc
    @PreAuthorize("hasAnyRole('CANDIDATE', 'EMPLOYER', 'ADMIN')")
    public ResponseEntity<ResultPaginationDTO> getAll(
            @Filter Specification<Application> spec,
            Pageable pageable,
            @RequestParam(required = false) String status
    ) throws Exception {
        return ResponseEntity.ok(applicationService.handleGetAllApps(spec, pageable, status));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('CANDIDATE', 'EMPLOYER', 'ADMIN')")
    @ApiMessage("Get application by id")
    public ResponseEntity<ResApplicationDTO> getById(@PathVariable long id) throws Exception {
        return ResponseEntity.ok(applicationService.handleGetAppById(id));
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('EMPLOYER', 'ADMIN')") // Chặn cứng Candidate
    @ApiMessage("Update status application successfully!")
    public ResponseEntity<ResUpdateApplicationDTO> updateStatus(
            @PathVariable long id,
            @Valid @RequestBody ReqUpdateApplicationStatusDTO reqDTO) throws Exception {
        return ResponseEntity.ok(applicationService.handleUpdateStatus(id, reqDTO));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('CANDIDATE')") // Chặn HR và Admin
    @ApiMessage("Application updated successfully")
    public ResponseEntity<ResApplicationDTO> updateAppByCandidate(
            @PathVariable long id,
            @Valid @RequestBody ReqUpdateAppByCandidateDTO reqDTO) throws Exception {

        return ResponseEntity.ok(applicationService.handleUpdateAppByCandidate(id, reqDTO));
    }

    @GetMapping("/hr") // Đường dẫn: GET /api/v1/applications/hr
    @PreAuthorize("hasAnyRole('EMPLOYER', 'ADMIN')") // Chặn luôn CANDIDATE từ ngoài cửa
    @ApiMessage("Fetch applications for HR and Admin")
    public ResponseEntity<ResultPaginationDTO> getApplicationsForHr(
            @Filter Specification<Application> spec,
            Pageable pageable) throws Exception {

        ResultPaginationDTO result = applicationService.handleGetApplicationsForHrAndAdmin(spec, pageable);
        return ResponseEntity.ok(result);
    }
}