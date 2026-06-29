package com.dtn.apply_job.controller;

import com.dtn.apply_job.common.annotation.ApiMessage;
import com.dtn.apply_job.common.response.ResultPaginationDTO;
import com.dtn.apply_job.domain.Resume;
import com.dtn.apply_job.domain.request.resume.ReqCreateResumeDTO;
import com.dtn.apply_job.domain.request.resume.ReqUpdateResumeDTO;
import com.dtn.apply_job.domain.response.resume.ResResumeDTO;
import com.dtn.apply_job.domain.response.resume.ResUpdateResumeDTO;
import com.dtn.apply_job.exception.IdInvalidException;
import com.dtn.apply_job.service.ResumeService;
import com.turkraft.springfilter.boot.Filter;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class ResumeController {

    private final ResumeService resumeService;

    @PostMapping("/resumes")
    @PreAuthorize("hasAnyRole('CANDIDATE', 'ADMIN')")
    @ApiMessage("Create resume")
    public ResponseEntity<ResResumeDTO> createResume(@Valid @RequestBody ReqCreateResumeDTO req) throws Exception {
        ResResumeDTO newResume = this.resumeService.handleCreateResume(req);
        return ResponseEntity.status(HttpStatus.CREATED).body(newResume);
    }

    @PutMapping("/resumes/{id}")
    @PreAuthorize("hasAnyRole('CANDIDATE', 'ADMIN')")
    @ApiMessage("Update resume")
    public ResponseEntity<ResUpdateResumeDTO> updateResume(@PathVariable long id, @Valid @RequestBody ReqUpdateResumeDTO req)
            throws Exception {
        ResUpdateResumeDTO updatedResume = this.resumeService.handleUpdateResume(id, req);
        return ResponseEntity.status(HttpStatus.OK).body(updatedResume);
    }

    @GetMapping("/resumes/{id}")
    @PreAuthorize("hasAnyRole('CANDIDATE', 'ADMIN')")
    @ApiMessage("Fetch resume by Id")
    public ResponseEntity<ResResumeDTO> getResumeById(@PathVariable long id) throws Exception {
        ResResumeDTO resume = this.resumeService.handleGetResumeById(id);
        return ResponseEntity.status(HttpStatus.OK).body(resume);
    }

    @GetMapping("/resumes")
    @PreAuthorize("hasRole('ADMIN')")
    @ApiMessage("Fetch all resumes")
    public ResponseEntity<ResultPaginationDTO> getAllResumes(
            @Filter Specification<Resume> spec,
            Pageable pageable
    ) {
        ResultPaginationDTO result = this.resumeService.handleGetAllResumes(spec, pageable);
        return ResponseEntity.status(HttpStatus.OK).body(result);
    }


    @GetMapping("/my-cvs")
    @PreAuthorize("hasRole('CANDIDATE')")
    @ApiMessage("Get your Cv-list successfully!")
    public ResponseEntity<List<ResResumeDTO>> getMyResumes() throws IdInvalidException {
        List<ResResumeDTO> result = resumeService.handleGetMyResumes();
        return ResponseEntity.ok(result);
    }

    @DeleteMapping("/resumes/{id}")
    @PreAuthorize("hasAnyRole('CANDIDATE', 'ADMIN')")
    @ApiMessage("Delete resume successfully!")
    public ResponseEntity<Void> deleteResume(@PathVariable long id) throws IdInvalidException {
        resumeService.handleSoftDeleteResume(id);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/resumes/{id}/default")
    @PreAuthorize("hasRole('CANDIDATE')")
    @ApiMessage("Set default CV successfully!")
    public ResponseEntity<ResResumeDTO> setDefaultResume(@PathVariable long id) throws IdInvalidException {
        ResResumeDTO updatedResume = this.resumeService.handleSetDefaultResume(id);
        return ResponseEntity.ok(updatedResume);
    }
}
