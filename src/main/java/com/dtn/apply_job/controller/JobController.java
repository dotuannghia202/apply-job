package com.dtn.apply_job.controller;

import com.dtn.apply_job.common.annotation.ApiMessage;
import com.dtn.apply_job.common.response.ResultPaginationDTO;
import com.dtn.apply_job.domain.Job;
import com.dtn.apply_job.domain.request.job.ReqCreateJobDTO;
import com.dtn.apply_job.domain.request.job.ReqUpdateJobDTO;
import com.dtn.apply_job.domain.response.job.ResJobDTO;
import com.dtn.apply_job.domain.response.job.ResUpdateJobDTO;
import com.dtn.apply_job.exception.IdInvalidException;
import com.dtn.apply_job.exception.InvalidDateRangeException;
import com.dtn.apply_job.service.JobService;
import com.turkraft.springfilter.boot.Filter;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/jobs")
public class JobController {

    private final JobService jobService;

    public JobController(JobService jobService) {
        this.jobService = jobService;
    }

    @PostMapping
    @ApiMessage("Create a job")
    // Dùng ReqCreateJobDTO để nhận CompanyId, SpecializationId và List<SkillId>
    public ResponseEntity<ResJobDTO> createJob(@Valid @RequestBody ReqCreateJobDTO reqDTO) throws IdInvalidException {
        ResJobDTO newJob = this.jobService.handleCreateJob(reqDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(newJob);
    }

    @PostMapping("/batch")
    @ApiMessage("Create jobs in batch")
    public ResponseEntity<List<ResJobDTO>> createJobsBatch(
            @Valid @RequestBody List<ReqCreateJobDTO> reqDTOs) throws IdInvalidException, InvalidDateRangeException {
        List<ResJobDTO> newJobs = this.jobService.handleCreateJobs(reqDTOs);
        return ResponseEntity.status(HttpStatus.CREATED).body(newJobs);
    }

    @PutMapping("/{id}")
    @ApiMessage("Update a job")
    public ResponseEntity<ResUpdateJobDTO> updateJob(
            @PathVariable long id,
            @Valid @RequestBody ReqUpdateJobDTO reqDTO) throws IdInvalidException {
        // Service của bạn cũng cần sửa lại để trả về ResJobDTO nhé
        ResUpdateJobDTO updatedJob = this.jobService.handleUpdateJob(id, reqDTO);
        return ResponseEntity.ok().body(updatedJob);
    }

    @GetMapping
    @ApiMessage("Get all jobs with pagination and filter")
    public ResponseEntity<ResultPaginationDTO> getAllJobs(
            @Filter Specification<Job> spec,
            Pageable pageable,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) List<String> levels,
            @RequestParam(required = false) Long specialization,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String skill,
            @RequestParam(required = false) Boolean active
    ) throws IdInvalidException {
        ResultPaginationDTO result = this.jobService.handleGetAllJobsWithFilters(
                spec, pageable, location, levels, specialization, name, skill, active);
        return ResponseEntity.ok().body(result);
    }

    @GetMapping("/{id}")
    @ApiMessage("Fetch job by id")
    public ResponseEntity<ResJobDTO> getJobById(@PathVariable long id) throws IdInvalidException {
        ResJobDTO dto = this.jobService.handleGetJobById(id);
        return ResponseEntity.ok().body(dto);
    }

    @DeleteMapping("/{id}")
    @ApiMessage("Delete a job")
    public ResponseEntity<Void> deleteJob(@PathVariable long id) throws IdInvalidException {
        this.jobService.handleDeleteJob(id);
        return ResponseEntity.ok().build();
    }
}