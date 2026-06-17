package com.dtn.apply_job.service;

import com.dtn.apply_job.domain.Application;
import com.dtn.apply_job.domain.Resume;
import com.dtn.apply_job.domain.request.job.ReqGenerateJdDTO;
import com.dtn.apply_job.domain.response.job.ResGenerateJdDTO;
import com.dtn.apply_job.repository.ApplicationRepository;
import com.dtn.apply_job.repository.ResumeRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AiPythonService {

    @Value("${python.ai.base-url}")
    private String pythonAiBaseUrl;

    @Value("${python.ai.extract-cv-path}")
    private String extractCvPath;

    @Value("${python.ai.match-path}")
    private String matchScorePath;

    @Value("${python.ai.generate-jd-path}")
    private String generateJdPath;

    private final ResumeRepository resumeRepository;
    private final RestTemplate restTemplate;
    private final ApplicationRepository applicationRepository;

    public AiPythonService(ResumeRepository resumeRepository, ApplicationRepository applicationRepository) {
        this.resumeRepository = resumeRepository;
        this.applicationRepository = applicationRepository;
        this.restTemplate = new RestTemplate(); // Công cụ để Java gọi API ngoài
    }

    // @Async giúp hàm này chạy ở một luồng (Thread) riêng biệt, không làm chậm web
    @Async
    public void processCvTextAsync(Long resumeId, String fileUrl) {
        try {
            System.out.println(">>> Đang gửi file PDF sang Python AI để đọc...");

            // 1. Chuẩn bị URL của Python
            String pythonApiUrl = pythonAiBaseUrl + extractCvPath;

            // 2. Chuẩn bị Body JSON {"file_url": "..."} giống hệt cái Pydantic schema bên Python
            Map<String, String> requestBody = new HashMap<>();
            requestBody.put("file_url", fileUrl);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, String>> requestEntity = new HttpEntity<>(requestBody, headers);

            // 3. Bắn API sang Python
            ResponseEntity<Map> response = restTemplate.postForEntity(pythonApiUrl, requestEntity, Map.class);

            // 4. Bóc tách kết quả từ RestResponse của Python
            Map<String, Object> responseBody = response.getBody();
            if (responseBody != null && (Integer) responseBody.get("status_code") == 200) {
                Map<String, Object> data = (Map<String, Object>) responseBody.get("data");
                String parsedText = (String) data.get("parsed_text");

                // 5. Cập nhật lại vào Database
                Resume resume = resumeRepository.findById(resumeId).orElseThrow();
                resume.setParsedText(parsedText);
                resumeRepository.save(resume);

                System.out.println(">>> AI đã đọc và lưu Text CV thành công cho Resume ID: " + resumeId);
            } else {
                System.out.println(">>> Python AI báo lỗi: " + responseBody.get("error"));
            }

        } catch (Exception e) {
            System.out.println(">>> Lỗi kết nối đến Python AI: " + e.getMessage());
        }
    }

    @Async
    public void calculateMatchScoreAsync(Long applicationId, String jobText, String cvText) {
        try {
            System.out.println(">>> Đang gửi Text sang Python AI để chấm điểm...");

            String pythonApiUrl = pythonAiBaseUrl + matchScorePath;

            // Chuẩn bị Body gửi đi (Khớp với MatchRequest bên Python)
            Map<String, String> requestBody = new HashMap<>();
            requestBody.put("job_text", jobText != null ? jobText : "");
            requestBody.put("cv_text", cvText != null ? cvText : "");

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, String>> requestEntity = new HttpEntity<>(requestBody, headers);

            // Bắn API
            ResponseEntity<Map> response = restTemplate.postForEntity(pythonApiUrl, requestEntity, Map.class);

            Map<String, Object> responseBody = response.getBody();
            if (responseBody != null && (Integer) responseBody.get("status_code") == 200) {
                // Bóc tách điểm số từ JSON
                Map<String, Object> data = (Map<String, Object>) responseBody.get("data");

                // Mẹo Java: Ép kiểu linh hoạt vì Python có thể trả về Double hoặc Integer (ví dụ 85.0 hoặc 85)
                Double matchScore = Double.valueOf(data.get("match_score").toString());

                // Cập nhật lại vào bảng Application
                Application app = applicationRepository.findById(applicationId).orElseThrow();
                app.setMatchScore(matchScore);
                applicationRepository.save(app);

                System.out.println(">>> AI Đã chấm xong! Điểm số: " + matchScore + "% cho Application ID: " + applicationId);
            } else {
                System.out.println(">>> Python AI báo lỗi: " + responseBody.get("error"));
            }

        } catch (Exception e) {
            System.out.println(">>> Lỗi kết nối đến Python AI (Match): " + e.getMessage());
        }
    }

    // LƯU Ý: Không có @Async ở đây
    public ResGenerateJdDTO generateJdFromPython(ReqGenerateJdDTO reqDTO) throws Exception {
        String pythonApiUrl = pythonAiBaseUrl + generateJdPath;

        // Map DTO sang Map JSON để gửi đi
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
            throw new Exception("Python AI returned empty response body");
        }

        Object statusCodeObj = responseBody.get("status_code");
        int statusCode = convertToInt(statusCodeObj);

        if (statusCode != 200) {
            throw new Exception("Lỗi từ Python AI: " + responseBody.get("error"));
        }

        Object dataObj = responseBody.get("data");
        if (!(dataObj instanceof Map<?, ?> data)) {
            throw new Exception("Invalid Python AI response: data must be an object");
        }

        Object generatedJdObj = data.get("generated_jd");
        if (!(generatedJdObj instanceof Map<?, ?> generatedJd)) {
            throw new Exception("Invalid Python AI response: generated_jd must be an object");
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

        throw new Exception("Invalid status_code from Python AI: " + value);
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
}