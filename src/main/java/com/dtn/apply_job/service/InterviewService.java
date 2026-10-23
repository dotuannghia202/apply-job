package com.dtn.apply_job.service;

import com.dtn.apply_job.domain.InterviewQuestion;
import com.dtn.apply_job.domain.InterviewSession;
import com.dtn.apply_job.domain.request.interview.ReqSubmitInterviewDTO;
import com.dtn.apply_job.domain.response.interview.ResInterviewSessionDTO;
import com.dtn.apply_job.exception.IdInvalidException;
import com.dtn.apply_job.mapper.InterviewMapper;
import com.dtn.apply_job.repository.InterviewSessionRepository;
import com.dtn.apply_job.util.constant.enums.InterviewSessionStatus;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class InterviewService {

    private final InterviewSessionRepository interviewSessionRepository;
    private final FileService fileService;
    private final AiPythonService aiPythonService;
    private final ObjectMapper objectMapper;
    private final InterviewMapper interviewMapper;

    public ResInterviewSessionDTO getSessionDetail(UUID sessionId) throws IdInvalidException {
        InterviewSession session = interviewSessionRepository.findById(sessionId)
                .orElseThrow(() -> new IdInvalidException("Phiên phỏng vấn không tồn tại: " + sessionId));
        return interviewMapper.toResInterviewSessionDTO(session);
    }

    public ResInterviewSessionDTO getSessionByApplication(UUID applicationId) throws IdInvalidException {
        InterviewSession session = interviewSessionRepository.findByApplicationId(applicationId)
                .orElseThrow(() -> new IdInvalidException("Không tìm thấy phiên phỏng vấn cho hồ sơ này: " + applicationId));
        return interviewMapper.toResInterviewSessionDTO(session);
    }

    @Transactional
    public void startSession(UUID sessionId) throws IdInvalidException {
        InterviewSession session = interviewSessionRepository.findById(sessionId)
                .orElseThrow(() -> new IdInvalidException("Phiên phỏng vấn không tồn tại: " + sessionId));
        session.setStatus(InterviewSessionStatus.IN_PROGRESS);
        session.setStartedAt(Instant.now());
        interviewSessionRepository.save(session);
    }

    @Transactional
    public void submitSession(UUID sessionId, MultipartFile videoFile, String progressJson) throws Exception {
        InterviewSession session = interviewSessionRepository.findById(sessionId)
                .orElseThrow(() -> new IdInvalidException("Phiên phỏng vấn không tồn tại: " + sessionId));

        String videoUrl = fileService.storeToCloudinary(videoFile, "interviews");
        session.setMediaRecordUrl(videoUrl);
        session.setStatus(InterviewSessionStatus.EVALUATING);

        if (progressJson != null && !progressJson.isBlank()) {
            ReqSubmitInterviewDTO reqDto = objectMapper.readValue(progressJson, ReqSubmitInterviewDTO.class);
            if (reqDto.getQuestionProgresses() != null) {
                Map<UUID, ReqSubmitInterviewDTO.QuestionProgress> progressMap = reqDto.getQuestionProgresses().stream()
                        .filter(p -> p.getQuestionId() != null)
                        .collect(Collectors.toMap(ReqSubmitInterviewDTO.QuestionProgress::getQuestionId, p -> p, (a, b) -> a));

                for (InterviewQuestion q : session.getQuestions()) {
                    ReqSubmitInterviewDTO.QuestionProgress p = progressMap.get(q.getId());
                    if (p != null) {
                        q.setDurationSeconds(p.getDurationSeconds());
                        q.setStartTimeSeconds(p.getStartTimeSeconds());
                    }
                }
            }
        }

        interviewSessionRepository.save(session);
        aiPythonService.evaluateInterviewSessionAsync(sessionId, videoUrl);
    }
}
