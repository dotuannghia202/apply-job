package com.dtn.apply_job.controller;

import com.dtn.apply_job.common.annotation.ApiMessage;
import com.dtn.apply_job.domain.Resume;
import com.dtn.apply_job.domain.request.resume.ReqCreateResumeDTO;
import com.dtn.apply_job.domain.request.resume.ReqUpdateResumeDTO;
import com.dtn.apply_job.domain.response.resume.ResResumeDTO;
import com.dtn.apply_job.domain.response.user.ResultPaginationDTO;
import com.dtn.apply_job.exception.IdInvalidException;
import com.dtn.apply_job.service.ResumeService;
import com.turkraft.springfilter.boot.Filter;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class ResumeController {

    private final ResumeService resumeService;

    @PostMapping("/resumes")
    @ApiMessage("Create resume")
    public ResponseEntity<ResResumeDTO> createResume(@Valid @RequestBody ReqCreateResumeDTO req) throws IdInvalidException {
        ResResumeDTO newResume = this.resumeService.handleCreateResume(req);
        return ResponseEntity.status(HttpStatus.CREATED).body(newResume);
    }

    @PutMapping("/resumes/{id}")
    @ApiMessage("Update resume")
    public ResponseEntity<ResResumeDTO> updateResume(@PathVariable long id, @Valid @RequestBody ReqUpdateResumeDTO req)
            throws IdInvalidException {
        ResResumeDTO updatedResume = this.resumeService.handleUpdateResume(id, req);
        return ResponseEntity.status(HttpStatus.OK).body(updatedResume);
    }

    @GetMapping("/resumes/{id}")
    @ApiMessage("Fetch resume by Id")
    public ResponseEntity<ResResumeDTO> getResume(@PathVariable long id) throws IdInvalidException {
        ResResumeDTO resume = this.resumeService.handleGetResumeById(id);
        return ResponseEntity.status(HttpStatus.OK).body(resume);
    }

    @GetMapping("/resumes")
    @ApiMessage("Fetch all resumes")
    public ResponseEntity<ResultPaginationDTO> getAllResumes(
            @Filter Specification<Resume> spec,
            Pageable pageable
    ) {
        ResultPaginationDTO result = this.resumeService.handleGetAllResumes(spec, pageable);
        return ResponseEntity.status(HttpStatus.OK).body(result);
    }

    @DeleteMapping("/resumes/{id}")
    @ApiMessage("Delete resume")
    public ResponseEntity<Void> deleteResume(@PathVariable long id) throws IdInvalidException {
        this.resumeService.handleDeleteResume(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}

