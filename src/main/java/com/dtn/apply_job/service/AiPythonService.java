package com.dtn.apply_job.service;

import com.dtn.apply_job.domain.Application;
import com.dtn.apply_job.domain.InterviewQuestion;
import com.dtn.apply_job.domain.InterviewSession;
import com.dtn.apply_job.domain.Resume;
import com.dtn.apply_job.domain.request.job.ReqGenerateJdDTO;
import com.dtn.apply_job.domain.response.job.ResGenerateJdDTO;
import com.dtn.apply_job.repository.ApplicationRepository;
import com.dtn.apply_job.repository.InterviewQuestionRepository;
import com.dtn.apply_job.repository.InterviewSessionRepository;
import com.dtn.apply_job.repository.ResumeRepository;
import com.dtn.apply_job.util.constant.enums.InterviewSessionStatus;
import com.dtn.apply_job.util.constant.enums.QuestionCategory;
import java.time.Instant;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Service
public class AiPythonService {

    @NonFinal
    @Value("${python.ai.base-url}")
    String pythonAiBaseUrl;

    @NonFinal
    @Value("${python.ai.extract-cv-path}")
    String extractCvPath;

    @NonFinal
    @Value("${python.ai.match-path}")
    String matchScorePath;

    @NonFinal
    @Value("${python.ai.generate-jd-path}")
    String generateJdPath;

    @NonFinal
    @Value("${python.ai.generate-interview-question-path}")
    String generateInterviewQuestionPath;

    @NonFinal
    @Value("${python.ai.translate-path:/api/v1/ai/translate}")
    String translatePath;

    @NonFinal
    @Value("${python.ai.translate-batch-path:/api/v1/ai/translate-batch}")
    String translateBatchPath;

    ResumeRepository resumeRepository;
    RestTemplate restTemplate;
    ApplicationRepository applicationRepository;
    InterviewSessionRepository interviewSessionRepository;
    InterviewQuestionRepository interviewQuestionRepository;

    //Run in the background while uploading the CV
    //Convert the CV to text format.
    @Async
    public void processCvTextAsync(UUID resumeId, String fileUrl) {
        try {
            System.out.println(">>> Sending the PDF file to the Python AI for reading...");

            String pythonApiUrl = pythonAiBaseUrl + extractCvPath;


            Map<String, String> requestBody = new HashMap<>();
            requestBody.put("file_url", fileUrl);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, String>> requestEntity = new HttpEntity<>(requestBody, headers);


            ResponseEntity<Map> response = restTemplate.postForEntity(pythonApiUrl, requestEntity, Map.class);


            Map<String, Object> responseBody = response.getBody();
            if (responseBody != null && (Integer) responseBody.get("status_code") == 200) {
                Map<String, Object> data = (Map<String, Object>) responseBody.get("data");
                String parsedText = (String) data.get("parsed_text");


                Resume resume = resumeRepository.findById(resumeId).orElseThrow();
                resume.setParsedText(parsedText);
                resumeRepository.save(resume);

                System.out.println(">>> AI has successfully read and saved the text CV for Resume ID:" + resumeId);
            } else {
                System.out.println(">>> Python AI reports an error: " + responseBody.get("error"));
            }

        } catch (Exception e) {
            System.out.println(">>> Connection error to Python AI: " + e.getMessage());
        }
    }

    @Async
    public void calculateMatchScoreAsync(UUID applicationId, String jobText, String cvText) {
        try {
            System.out.println(">>> Sending text to Python AI for scoring...");

            String pythonApiUrl = pythonAiBaseUrl + matchScorePath;


            Map<String, String> requestBody = new HashMap<>();
            requestBody.put("job_text", jobText != null ? jobText : "");
            requestBody.put("cv_text", cvText != null ? cvText : "");

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, String>> requestEntity = new HttpEntity<>(requestBody, headers);


            ResponseEntity<Map> response = restTemplate.postForEntity(pythonApiUrl, requestEntity, Map.class);

            Map<String, Object> responseBody = response.getBody();
            if (responseBody != null && (Integer) responseBody.get("status_code") == 200) {

                Map<String, Object> data = (Map<String, Object>) responseBody.get("data");


                Double matchScore = Double.valueOf(data.get("match_score").toString());


                List<String> matchedSkills = (List<String>) data.get("matched_skills");
                List<String> missingSkills = (List<String>) data.get("missing_skills");

                Application app = applicationRepository.findById(applicationId).orElseThrow();
                app.setMatchScore(matchScore);

                app.setMatchedSkills(matchedSkills);
                app.setMissingSkills(missingSkills);

                applicationRepository.save(app);
                System.out.println(">>> AI analysis complete! Application ID: " + applicationId);
            } else {
                System.out.println(">>> Python AI reports an error: " + responseBody.get("error"));
            }

        } catch (Exception e) {
            System.out.println(">>> Connection error to Python AI (Match): " + e.getMessage());
        }
    }


    public ResGenerateJdDTO generateJdFromPython(ReqGenerateJdDTO reqDTO) throws Exception {
        String pythonApiUrl = pythonAiBaseUrl + generateJdPath;


        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("title", reqDTO.getTitle());
        requestBody.put("skills", reqDTO.getSkills());
        requestBody.put("levels", reqDTO.getLevels());


        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, String>> requestEntity = new HttpEntity<>(requestBody, headers);

        ResponseEntity<Map> response = restTemplate.postForEntity(pythonApiUrl, requestEntity, Map.class);
        Map<String, Object> responseBody = response.getBody();

        if (responseBody == null) {
            throw new Exception("Python return empty response");
        }

        Object statusCodeObj = responseBody.get("status_code");
        int statusCode = convertToInt(statusCodeObj);

        if (statusCode != 200) {
            throw new Exception("Error from Python AI: " + responseBody.get("error"));
        }

        Object dataObj = responseBody.get("data");
        if (!(dataObj instanceof Map<?, ?> data)) {
            throw new Exception("Response from Python AI invalid: data must be an object");
        }

        Object generatedJdObj = data.get("generated_jd");
        if (!(generatedJdObj instanceof Map<?, ?> generatedJd)) {
            throw new Exception("Response from Python AI invalid: generated_jd must be an object");
        }

        ResGenerateJdDTO result = new ResGenerateJdDTO();

        result.setDescription(
                generatedJd.get("description") != null
                        ? generatedJd.get("description").toString()
                        : ""
        );

        result.setRequirements(toStringList(generatedJd.get("requirements")));
        result.setBenefits(toStringList(generatedJd.get("benefits")));

        return result;
    }

    private int convertToInt(Object value) throws Exception {
        if (value instanceof Number number) {
            return number.intValue();
        }

        if (value instanceof String str) {
            return Integer.parseInt(str);
        }

        throw new Exception("Status code invalid from AI Python: " + value);
    }

    private List<String> toStringList(Object value) {
        if (value == null) {
            return Collections.emptyList();
        }

        if (value instanceof List<?> list) {
            return list.stream()
                    .map(String::valueOf)
                    .collect(Collectors.toList());
        }

        return List.of(String.valueOf(value));
    }


    // Call the Python API to generate five interview questions
    @Async
    public void generateInterviewQuestionsAsync(UUID sessionId, String jobText, String cvText) {
        try {
            System.out.println(">>> [AI Interview] Sending JD & CV to Python AI to generate interview question for Session ID: " + sessionId);

            String pythonApiUrl = pythonAiBaseUrl + generateInterviewQuestionPath;

            Map<String, String> requestBody = new HashMap<>();
            requestBody.put("job_text", jobText != null ? jobText : "");
            requestBody.put("cv_text", cvText != null ? cvText : "");

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, String>> requestEntity = new HttpEntity<>(requestBody, headers);

            ResponseEntity<Map> response = restTemplate.postForEntity(pythonApiUrl, requestEntity, Map.class);
            Map<String, Object> responseBody = response.getBody();

            if (responseBody != null && (Integer) responseBody.get("status_code") == 200) {

                List<Map<String, Object>> rawQuestions = (List<Map<String, Object>>) responseBody.get("data");

                if (rawQuestions == null) {
                    log.warn(">>> [AI Interview] The list of questions from Python is empty!");
                    return;
                }


                InterviewSession session = interviewSessionRepository.findById(sessionId)
                        .orElseThrow(() -> new RuntimeException("InterviewSession ID: " + sessionId + "not found"));

                List<InterviewQuestion> questions = new ArrayList<>();
                for (Map<String, Object> q : rawQuestions) {
                    String qText = (String) q.get("question_text");
                    String qTextVi = q.get("question_text_vi") != null ? (String) q.get("question_text_vi") : qText;
                    String qTextEn = q.get("question_text_en") != null ? (String) q.get("question_text_en") : qText;
                    InterviewQuestion question = InterviewQuestion.builder()
                            .interviewSession(session)
                            .orderIndex((Integer) q.get("order_index"))
                            .questionTextVi(qTextVi)
                            .questionTextEn(qTextEn)
                            .category(QuestionCategory.valueOf(((String) q.get("category")).toUpperCase()))
                            .timeLimitSeconds((Integer) q.get("time_limit_seconds"))
                            .rubric((String) q.get("rubric"))
                            .build();
                    questions.add(question);
                }

                interviewQuestionRepository.saveAll(questions);
                System.out.println(">>> [AI Interview] has just been generated" + questions.size() + " interview question for Session: " + sessionId);
            } else {
                System.err.println(">>> [AI Interview] Python error: " + (responseBody != null ? responseBody.get("error") : "empty"));
            }
        } catch (HttpStatusCodeException ex) {
            // ex.getResponseBodyAsString() chứa JSON lỗi từ Python
            String responseBody = ex.getResponseBodyAsString();
            log.error(">>> [AI Interview] Python error: Status = {}, Body = {}",
                    ex.getStatusCode(), responseBody);
        } catch (ResourceAccessException ex) {
            log.error(">>> [AI Interview] Unable to connect to the Python service (Connection Timeout/Refused)");
        } catch (Exception ex) {
            log.error(">>> [AI Interview] Unknown error: ", ex);
        }
    }

    @Async
    public void evaluateInterviewSessionAsync(UUID sessionId, String videoUrl) {
        try {
            InterviewSession session = interviewSessionRepository.findById(sessionId)
                    .orElseThrow(() -> new RuntimeException("Session not found: " + sessionId));

            String evalUrl = pythonAiBaseUrl + "/api/v1/ai/interview/evaluate";

            List<Map<String, Object>> questionsData = session.getQuestions().stream().map(q -> {
                Map<String, Object> map = new HashMap<>();
                map.put("question_id", q.getId().toString());
                map.put("question_text", q.getQuestionTextVi() != null ? q.getQuestionTextVi() : q.getQuestionText());
                map.put("rubric", q.getRubric());
                map.put("duration_seconds", q.getDurationSeconds() != null ? q.getDurationSeconds() : 60);
                map.put("time_limit_seconds", q.getTimeLimitSeconds());
                map.put("start_time_seconds", q.getStartTimeSeconds() != null ? q.getStartTimeSeconds() : 0);
                return map;
            }).toList();

            Map<String, Object> requestPayload = Map.of(
                    "session_id", sessionId.toString(),
                    "video_url", videoUrl,
                    "questions", questionsData
            );

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestPayload, headers);

            ResponseEntity<Map> response = restTemplate.postForEntity(evalUrl, entity, Map.class);
            Map<String, Object> body = response.getBody();

            if (body != null && Integer.valueOf(200).equals(body.get("status_code"))) {
                Map<String, Object> data = (Map<String, Object>) body.get("data");

                session.setTotalScore(((Number) data.get("total_score")).doubleValue());
                session.setAiOverallFeedbackVi((String) data.get("ai_overall_feedback_vi"));
                session.setAiOverallFeedbackEn((String) data.get("ai_overall_feedback_en"));
                session.setStatus(InterviewSessionStatus.COMPLETED);
                session.setCompletedAt(Instant.now());

                List<Map<String, Object>> evals = (List<Map<String, Object>>) data.get("evaluations");
                Map<UUID, Map<String, Object>> evalMap = evals.stream()
                        .collect(Collectors.toMap(e -> UUID.fromString(String.valueOf(e.get("question_id"))), e -> e));

                for (InterviewQuestion q : session.getQuestions()) {
                    Map<String, Object> res = evalMap.get(q.getId());
                    if (res != null) {
                        q.setTranscribedTextVi((String) res.get("transcribed_text_vi"));
                        q.setTranscribedTextEn((String) res.get("transcribed_text_en"));
                        q.setAccuracyScore(((Number) res.get("accuracy_score")).doubleValue());
                        q.setSpeedFactor(((Number) res.get("speed_factor")).doubleValue());
                        q.setFinalScore(((Number) res.get("final_score")).doubleValue());
                        q.setEvidenceQuote((String) res.get("evidence_quote"));
                        q.setFeedbackVi((String) res.get("feedback_vi"));
                        q.setFeedbackEn((String) res.get("feedback_en"));
                    }
                }

                interviewSessionRepository.save(session);
                log.info(">>> [AI Interview] Đã hoàn thành chấm điểm buổi phỏng vấn Session: {}", sessionId);
            }
        } catch (Exception ex) {
            log.error(">>> [AI Interview] Lỗi chấm điểm buổi phỏng vấn: ", ex);
        }
    }

    public record BilingualTextDTO(String detectedLang, String textVi, String textEn) {}
    public record BilingualBatchDTO(String detectedLang, List<String> textsVi, List<String> textsEn) {}

    public BilingualTextDTO autoDetectAndTranslate(String text) {
        if (text == null || text.isBlank()) {
            return new BilingualTextDTO("vi", "", "");
        }
        String cleanText = text.trim();
        try {
            String pythonApiUrl = pythonAiBaseUrl + translatePath;
            Map<String, String> requestBody = Map.of("text", cleanText);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, String>> requestEntity = new HttpEntity<>(requestBody, headers);

            ResponseEntity<Map> response = restTemplate.postForEntity(pythonApiUrl, requestEntity, Map.class);
            Map<String, Object> responseBody = response.getBody();

            if (responseBody != null && Integer.valueOf(200).equals(responseBody.get("status_code"))) {
                Map<String, Object> data = (Map<String, Object>) responseBody.get("data");
                if (data != null) {
                    String detectedLang = (String) data.getOrDefault("detected_lang", "vi");
                    String textVi = (String) data.getOrDefault("text_vi", cleanText);
                    String textEn = (String) data.getOrDefault("text_en", cleanText);
                    return new BilingualTextDTO(detectedLang, textVi, textEn);
                }
            }
        } catch (Exception ex) {
            log.warn(">>> [AI Translation] Could not translate text via Python AI service ({}), falling back to raw text.", ex.getMessage());
        }

        return new BilingualTextDTO("vi", cleanText, cleanText);
    }

    public BilingualBatchDTO autoDetectAndTranslateBatch(List<String> texts) {
        if (texts == null || texts.isEmpty()) {
            return new BilingualBatchDTO("vi", Collections.emptyList(), Collections.emptyList());
        }
        try {
            String pythonApiUrl = pythonAiBaseUrl + translateBatchPath;
            Map<String, Object> requestBody = Map.of("texts", texts);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);

            ResponseEntity<Map> response = restTemplate.postForEntity(pythonApiUrl, requestEntity, Map.class);
            Map<String, Object> responseBody = response.getBody();

            if (responseBody != null && Integer.valueOf(200).equals(responseBody.get("status_code"))) {
                Map<String, Object> data = (Map<String, Object>) responseBody.get("data");
                if (data != null) {
                    String detectedLang = (String) data.getOrDefault("detected_lang", "vi");
                    List<String> textsVi = (List<String>) data.getOrDefault("texts_vi", texts);
                    List<String> textsEn = (List<String>) data.getOrDefault("texts_en", texts);
                    return new BilingualBatchDTO(detectedLang, textsVi, textsEn);
                }
            }
        } catch (Exception ex) {
            log.warn(">>> [AI Batch Translation] Could not translate batch via Python AI service ({}), falling back to raw texts.", ex.getMessage());
        }

        return new BilingualBatchDTO("vi", texts, texts);
    }
}