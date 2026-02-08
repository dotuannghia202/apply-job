package com.dtn.apply_job.controller;

import com.dtn.apply_job.domain.Job;
import com.dtn.apply_job.domain.request.job.ReqUpdateJobDTO;
import com.dtn.apply_job.domain.response.job.ResCreateJobDTO;
import com.dtn.apply_job.domain.response.job.ResJobDTO;
import com.dtn.apply_job.domain.response.job.ResUpdateJobDTO;
import com.dtn.apply_job.domain.response.user.ResultPaginationDTO;
import com.dtn.apply_job.exception.IdInvalidException;
import com.dtn.apply_job.service.JobService;
import com.dtn.apply_job.util.annotation.ApiMessage;
import com.turkraft.springfilter.boot.Filter;
import jakarta.validation.Valid;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Setter
@Getter
@RestController
@RequestMapping("api/v1")
public class JobController {
    private JobService jobService;

    public JobController(JobService jobService) {
        this.jobService = jobService;
    }

    @PostMapping("/jobs")
    @ApiMessage("Create a job")
    public ResponseEntity<ResCreateJobDTO> createJob(@Valid @RequestBody Job job) {
        ResCreateJobDTO newJob = this.jobService.handleCreateJob(job);
        return ResponseEntity.status(HttpStatus.CREATED).body(newJob);
    }

    @PutMapping("/jobs/{id}")
    @ApiMessage("Update job")
    public ResponseEntity<ResUpdateJobDTO> updateJob(@PathVariable long id, @Valid @RequestBody ReqUpdateJobDTO dto) throws IdInvalidException {
        ResUpdateJobDTO updateJobDTO = this.jobService.handleUpdateJob(id, dto);
        return ResponseEntity.status(HttpStatus.OK).body(updateJobDTO);
    }

    @GetMapping("jobs")
    @ApiMessage("Get All Jobs")
    public ResponseEntity<ResultPaginationDTO> getAllJobs(@Filter Specification<Job> spec, Pageable pageable) {
        ResultPaginationDTO result = this.jobService.handleGetAllJobs(spec, pageable);
        return ResponseEntity.status(HttpStatus.OK).body(result);
    }

    @GetMapping("jobs/{id}")
    @ApiMessage("Fetch Job By Id")
    public ResponseEntity<ResJobDTO> getJobById(@PathVariable long id) throws IdInvalidException {
        ResJobDTO dto = this.jobService.handleGetJobById(id);
        return ResponseEntity.status(HttpStatus.OK).body(dto);
    }

    @DeleteMapping("jobs/{id}")
    @ApiMessage("Delete job")
    public ResponseEntity<Void> deleteJob(@PathVariable long id) throws IdInvalidException {
        this.jobService.handleDeleteJob(id);
        return ResponseEntity.status(HttpStatus.OK).build();
    }
}
