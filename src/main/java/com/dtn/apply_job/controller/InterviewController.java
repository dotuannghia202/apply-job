package com.dtn.apply_job.controller;

import com.dtn.apply_job.common.annotation.ApiMessage;
import com.dtn.apply_job.domain.response.interview.ResInterviewSessionDTO;
import com.dtn.apply_job.service.InterviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/interviews")
@RequiredArgsConstructor
public class InterviewController {

    private final InterviewService interviewService;

    @GetMapping("/{sessionId}")
    @PreAuthorize("isAuthenticated()")
    @ApiMessage("Interview session retrieved successfully")
    public ResponseEntity<ResInterviewSessionDTO> getSession(@PathVariable UUID sessionId) throws Exception {
        return ResponseEntity.ok(interviewService.getSessionDetail(sessionId));
    }

    @GetMapping("/by-application/{applicationId}")
    @PreAuthorize("isAuthenticated()")
    @ApiMessage("Interview session by application retrieved successfully")
    public ResponseEntity<ResInterviewSessionDTO> getSessionByApplication(@PathVariable UUID applicationId) throws Exception {
        return ResponseEntity.ok(interviewService.getSessionByApplication(applicationId));
    }

    @PostMapping("/{sessionId}/start")
    @PreAuthorize("hasAnyRole('CANDIDATE', 'ROLE_CANDIDATE', 'ADMIN', 'ROLE_ADMIN')")
    @ApiMessage("Interview session started successfully")
    public ResponseEntity<Void> startInterview(@PathVariable UUID sessionId) throws Exception {
        interviewService.startSession(sessionId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{sessionId}/submit")
    @PreAuthorize("hasAnyRole('CANDIDATE', 'ROLE_CANDIDATE', 'ADMIN', 'ROLE_ADMIN')")
    @ApiMessage("Interview session submitted successfully")
    public ResponseEntity<Void> submitInterview(
            @PathVariable UUID sessionId,
            @RequestParam("video") MultipartFile video,
            @RequestParam("progress") String progressJson) throws Exception {
        interviewService.submitSession(sessionId, video, progressJson);
        return ResponseEntity.ok().build();
    }
}
