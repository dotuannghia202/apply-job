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
import com.dtn.apply_job.util.constant.enums.QuestionCategory;
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

    ResumeRepository resumeRepository;
    RestTemplate restTemplate;
    ApplicationRepository applicationRepository;
    InterviewSessionRepository interviewSessionRepository;
    InterviewQuestionRepository interviewQuestionRepository;

    //Run in the background while uploading the CV
    //Convert the CV to text format.
    @Async
    public void processCvTextAsync(Long resumeId, String fileUrl) {
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
    public void calculateMatchScoreAsync(Long applicationId, String jobText, String cvText) {
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
    public void generateInterviewQuestionsAsync(Long sessionId, String jobText, String cvText) {
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
                    InterviewQuestion question = InterviewQuestion.builder()
                            .interviewSession(session)
                            .orderIndex((Integer) q.get("order_index"))
                            .questionText((String) q.get("question_text"))
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
}