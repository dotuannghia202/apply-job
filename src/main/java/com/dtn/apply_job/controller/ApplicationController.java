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
    @ApiMessage("Nộp hồ sơ ứng tuyển thành công")
    public ResponseEntity<ResCreateApplicationDTO> create(@Valid @RequestBody ReqCreateApplicationDTO reqDTO) throws Exception {
        return ResponseEntity.status(HttpStatus.CREATED).body(applicationService.handleCreateApplication(reqDTO));
    }

    @GetMapping
    @ApiMessage("Lấy danh sách ứng tuyển thành công")
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
    @ApiMessage("Lấy chi tiết ứng tuyển thành công")
    public ResponseEntity<ResApplicationDTO> getById(@PathVariable long id) throws Exception {
        return ResponseEntity.ok(applicationService.handleGetAppById(id));
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('EMPLOYER', 'ADMIN')")
    @ApiMessage("Cập nhật trạng thái ứng tuyển thành công!")
    public ResponseEntity<ResUpdateApplicationDTO> updateStatus(
            @PathVariable long id,
            @Valid @RequestBody ReqUpdateApplicationStatusDTO reqDTO) throws Exception {
        return ResponseEntity.ok(applicationService.handleUpdateStatus(id, reqDTO));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('CANDIDATE')")
    @ApiMessage("Cập nhật ứng tuyển thành công")
    public ResponseEntity<ResApplicationDTO> updateAppByCandidate(
            @PathVariable long id,
            @Valid @RequestBody ReqUpdateAppByCandidateDTO reqDTO) throws Exception {

        return ResponseEntity.ok(applicationService.handleUpdateAppByCandidate(id, reqDTO));
    }

    @GetMapping("/hr")
    @PreAuthorize("hasAnyRole('EMPLOYER', 'ADMIN')")
    @ApiMessage("Lấy danh sách hồ sơ ứng tuyển thành công")
    public ResponseEntity<ResultPaginationDTO> getApplicationsForHr(
            @Filter Specification<Application> spec,
            Pageable pageable,
            @RequestParam(required = false) String status) throws Exception {

        ResultPaginationDTO result = applicationService.handleGetApplicationsForHrAndAdmin(spec, pageable, status);
        return ResponseEntity.ok(result);
    }
}