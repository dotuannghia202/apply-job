package com.dtn.apply_job.controller;

import com.dtn.apply_job.common.annotation.ApiMessage;
import com.dtn.apply_job.common.response.ResultPaginationDTO;
import com.dtn.apply_job.domain.Job;
import com.dtn.apply_job.domain.request.job.ReqCreateJobDTO;
import com.dtn.apply_job.domain.request.job.ReqGenerateJdDTO;
import com.dtn.apply_job.domain.request.job.ReqUpdateJobDTO;
import com.dtn.apply_job.domain.response.job.ResGenerateJdDTO;
import com.dtn.apply_job.domain.response.job.ResJobDTO;
import com.dtn.apply_job.domain.response.job.ResUpdateJobDTO;
import com.dtn.apply_job.exception.IdInvalidException;
import com.dtn.apply_job.exception.InvalidDateRangeException;
import com.dtn.apply_job.service.AiPythonService;
import com.dtn.apply_job.service.JobService;
import com.turkraft.springfilter.boot.Filter;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/jobs")
public class JobController {

    private final JobService jobService;
    private final AiPythonService aiPythonService;

    public JobController(JobService jobService, AiPythonService aiPythonService) {
        this.jobService = jobService;
        this.aiPythonService = aiPythonService;
    }

    @PostMapping
    @ApiMessage("Tạo công việc thành công")
    public ResponseEntity<ResJobDTO> createJob(@Valid @RequestBody ReqCreateJobDTO reqDTO) throws IdInvalidException, InvalidDateRangeException, AccessDeniedException {
        ResJobDTO newJob = this.jobService.handleCreateJob(reqDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(newJob);
    }

    @PostMapping("/batch")
    @ApiMessage("Tạo hàng loạt công việc thành công")
    public ResponseEntity<List<ResJobDTO>> createJobsBatch(
            @Valid @RequestBody List<ReqCreateJobDTO> reqDTOs) throws IdInvalidException, InvalidDateRangeException, AccessDeniedException {
        List<ResJobDTO> newJobs = this.jobService.handleCreateJobs(reqDTOs);
        return ResponseEntity.status(HttpStatus.CREATED).body(newJobs);
    }

    @PutMapping("/{id:\\d+}")
    @ApiMessage("Cập nhật công việc thành công")
    public ResponseEntity<ResUpdateJobDTO> updateJob(
            @PathVariable long id,
            @Valid @RequestBody ReqUpdateJobDTO reqDTO) throws Exception {
        ResUpdateJobDTO updatedJob = this.jobService.handleUpdateJob(id, reqDTO);
        return ResponseEntity.ok().body(updatedJob);
    }

    @GetMapping
    @ApiMessage("Lấy danh sách công việc thành công")
    public ResponseEntity<ResultPaginationDTO> getAllJobs(
            @Filter Specification<Job> spec,
            Pageable pageable,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) List<String> levels,
            @RequestParam(required = false) Long specialization,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String companyName,
            @RequestParam(required = false) String skill,
            @RequestParam(required = false) Boolean active,
            @RequestParam(required = false) Integer sortCreatedAt
    ) throws IdInvalidException {
        Specification<Job> notExpiredSpec = (root, query, cb) ->
                cb.greaterThanOrEqualTo(root.get("endDate"), java.time.Instant.now());
        Specification<Job> finalSpec = spec == null ? notExpiredSpec : spec.and(notExpiredSpec);
        ResultPaginationDTO result = this.jobService.handleGetAllJobsWithFilters(
                finalSpec, pageable, location, levels, specialization,
                companyName, null, null, name, skill, active, sortCreatedAt);
        return ResponseEntity.ok().body(result);
    }

    @GetMapping("/{id:\\d+}")
    @ApiMessage("Lấy thông tin công việc thành công")
    public ResponseEntity<ResJobDTO> getJobById(@PathVariable long id) throws IdInvalidException {
        ResJobDTO dto = this.jobService.handleGetJobById(id);
        return ResponseEntity.ok().body(dto);
    }

    @DeleteMapping("/{id:\\d+}")
    @PreAuthorize("hasAnyRole('EMPLOYER', 'ADMIN')")
    @ApiMessage("Xóa công việc thành công")
    public ResponseEntity<Void> deleteJob(@PathVariable long id) throws Exception {
        this.jobService.handleDeleteJob(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/hr")
    @PreAuthorize("hasRole('EMPLOYER')")
    @ApiMessage("Lấy danh sách công việc của công ty thành công")
    public ResponseEntity<ResultPaginationDTO> getJobsByCurrentHr(
            @Filter Specification<Job> spec,
            Pageable pageable,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) List<String> levels,
            @RequestParam(required = false) Long specialization,
            @RequestParam(required = false) String company,
            @RequestParam(required = false) Double minSalary,
            @RequestParam(required = false) Double maxSalary,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String skill,
            @RequestParam(required = false) Boolean active,
            @RequestParam(required = false) Integer sortCreatedAt
    ) throws Exception {

        ResultPaginationDTO result = this.jobService.handleGetJobsByCurrentHrWithFilters(
                spec, pageable, location, levels, specialization, company,
                minSalary, maxSalary, name, keyword, skill, active, sortCreatedAt);

        return ResponseEntity.ok().body(result);
    }

    @PostMapping("/generate-jd")
    @PreAuthorize("hasAnyRole('EMPLOYER', 'ADMIN')")
    @ApiMessage("Tự động sinh JD bằng AI thành công")
    public ResponseEntity<ResGenerateJdDTO> generateJdByAi(@RequestBody ReqGenerateJdDTO reqDTO) throws Exception {

        ResGenerateJdDTO generatedContent = aiPythonService.generateJdFromPython(reqDTO);

        return ResponseEntity.ok(generatedContent);
    }

}
