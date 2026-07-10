package com.dtn.apply_job.service;

import com.dtn.apply_job.common.response.ResultPaginationDTO;
import com.dtn.apply_job.domain.*;
import com.dtn.apply_job.domain.request.application.ReqCreateApplicationDTO;
import com.dtn.apply_job.domain.request.application.ReqUpdateAppByCandidateDTO;
import com.dtn.apply_job.domain.request.application.ReqUpdateApplicationStatusDTO;
import com.dtn.apply_job.domain.response.application.ResApplicationDTO;
import com.dtn.apply_job.domain.response.application.ResCreateApplicationDTO;
import com.dtn.apply_job.domain.response.application.ResUpdateApplicationDTO;
import com.dtn.apply_job.exception.IdInvalidException;
import com.dtn.apply_job.repository.ApplicationRepository;
import com.dtn.apply_job.repository.JobRepository;
import com.dtn.apply_job.repository.ResumeRepository;
import com.dtn.apply_job.repository.UserRepository;
import com.dtn.apply_job.security.SecurityUtil;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ApplicationService {
    private final ApplicationRepository applicationRepository;
    private final UserRepository userRepository;
    private final JobRepository jobRepository;
    private final ResumeRepository resumeRepository;
    private final AiPythonService aiPythonService;
    private final NotificationService notificationService;
    private final GmailOAuthService gmailOAuthService;


    public ApplicationService(ApplicationRepository applicationRepository, UserRepository userRepository, JobRepository jobRepository, ResumeRepository resumeRepository, AiPythonService aiPythonService, NotificationService notificationService, GmailOAuthService gmailOAuthService) {
        this.applicationRepository = applicationRepository;
        this.userRepository = userRepository;
        this.jobRepository = jobRepository;
        this.resumeRepository = resumeRepository;
        this.aiPythonService = aiPythonService;
        this.notificationService = notificationService;
        this.gmailOAuthService = gmailOAuthService;
    }


    private Application getAppAndCheckAccess(long id) throws Exception {
        Application app = applicationRepository.findById(id)
                .orElseThrow(() -> new IdInvalidException("The application form does not exist!"));

        String email = SecurityUtil.getCurrentUser().orElseThrow(() -> new Exception("Not logged in yet!"));
        User currentUser = userRepository.findByEmail(email);

        boolean isAdmin = currentUser.getRoles().stream().anyMatch(r -> r.getName().name().equals("ADMIN"));
        boolean isEmployer = currentUser.getRoles().stream().anyMatch(r -> r.getName().name().equals("EMPLOYER"));

        if (isAdmin) return app;

        if (isEmployer) {

            if (currentUser.getCompany() == null ||
                    app.getJob().getCompany().getId() != currentUser.getCompany().getId()) {
                throw new Exception("Bạn không được phép xem hồ sơ ứng tuyển của công ty khác!");
            }
            return app;
        }


        if (app.getResume().getCandidate().getId() != currentUser.getId()) {
            throw new Exception("Bạn không được phép xem hồ sơ ứng tuyển của người khác!");
        }

        return app;
    }


    @Transactional
    public ResCreateApplicationDTO handleCreateApplication(ReqCreateApplicationDTO reqDTO) throws IdInvalidException, Exception {


        String email = SecurityUtil.getCurrentUser().orElseThrow();
        User candidate = userRepository.findByEmail(email);


        Resume resume = resumeRepository.findById(reqDTO.getResumeId())
                .orElseThrow(() -> new IdInvalidException("CV doesn't exist!"));


        if (resume.getCandidate().getId() != candidate.getId()) {
            throw new Exception("Bạn không được phép sử dụng CV của người khác!");
        }


        Job job = jobRepository.findById(reqDTO.getJobId())
                .orElseThrow(() -> new IdInvalidException("Job doesn't exist!"));

        if (!job.getActive()) {
            throw new Exception("Công việc này đã đóng hoặc thời hạn nộp hồ sơ đã hết hạn!");
        }


        boolean isAlreadyApplied = applicationRepository.existsByResumeCandidateAndJob(candidate, job);
        if (isAlreadyApplied) {
            throw new Exception("Bạn đã ứng tuyển công việc này rồi. Vui lòng kiểm tra trang Quản lý ứng tuyển!");
        }


        Application application = new Application();
        application.setJob(job);
        application.setResume(resume);
        application.setCoverLetter(reqDTO.getCoverLetter());


        application.setMatchScore(null);

        Application savedApp = applicationRepository.save(application);


        try {
            Company company = job.getCompany();

            if (company != null && company.getUsers() != null) {
                String candidateName = candidate.getName();
                String jobTitle = job.getName();

                String title = "Có ứng viên mới nộp CV!";
                String message = "Ứng viên " + candidateName + " vừa ứng tuyển vào vị trí [" + jobTitle + "].";


                for (User hr : company.getUsers()) {

                    boolean isEmployer = hr.getRoles().stream()
                            .anyMatch(r -> r.getName().name().equals("ROLE_EMPLOYER") || r.getName().name().equals("EMPLOYER"));

                    if (isEmployer) {
                        notificationService.sendToUser(
                                hr,
                                title,
                                message,
                                "NEW_APPLICATION",
                                savedApp.getId(),
                                com.dtn.apply_job.util.constant.enums.ERole.EMPLOYER
                        );
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Lỗi khi gửi thông báo cho HR: " + e.getMessage());
        }

        String jobTextForAI = "";
        if (job.getDescription() != null) jobTextForAI += job.getDescription() + "\n";
        if (job.getRequirements() != null) jobTextForAI += job.getRequirements();


        String cvTextForAI = resume.getParsedText();


        aiPythonService.calculateMatchScoreAsync(savedApp.getId(), jobTextForAI, cvTextForAI);

        ResCreateApplicationDTO res = new ResCreateApplicationDTO();
        res.setId(savedApp.getId());
        res.setStatus(savedApp.getStatus());
        res.setAppliedAt(savedApp.getAppliedAt());
        return res;
    }


    public ResApplicationDTO handleGetAppById(long id) throws Exception {
        Application app = getAppAndCheckAccess(id);
        return convertToResAppDTO(app);
    }


    public ResultPaginationDTO handleGetAllApps(Specification<Application> spec, Pageable pageable,
                                                String status) throws Exception {
        String email = SecurityUtil.getCurrentUser().orElseThrow();
        User currentUser = userRepository.findByEmail(email);


        boolean isAdmin = currentUser.getRoles().stream()
                .anyMatch(r -> r.getName().name().equals("ROLE_ADMIN") || r.getName().name().equals("ADMIN"));
        boolean isEmployer = currentUser.getRoles().stream()
                .anyMatch(r -> r.getName().name().equals("ROLE_EMPLOYER") || r.getName().name().equals("EMPLOYER"));

        Specification<Application> roleSpec;
        Sort customSort;

        if (isAdmin) {
            roleSpec = spec;

            customSort = Sort.by(Sort.Direction.DESC, "appliedAt");

        } else if (isEmployer) {
            long companyId = currentUser.getCompany() != null ? currentUser.getCompany().getId() : 0;
            Specification<Application> hrSpec = (root, query, cb) ->
                    cb.equal(root.get("job").get("company").get("id"), companyId);
            roleSpec = spec == null ? hrSpec : spec.and(hrSpec);


            customSort = Sort.by(Sort.Direction.DESC, "matchScore")
                    .and(Sort.by(Sort.Direction.DESC, "appliedAt"));

        } else {
            Specification<Application> candSpec = (root, query, cb) ->
                    cb.equal(root.get("resume").get("candidate").get("id"), currentUser.getId());
            roleSpec = spec == null ? candSpec : spec.and(candSpec);


            customSort = Sort.by(Sort.Direction.DESC, "appliedAt");
        }

        if (status != null && !status.trim().isEmpty()) {
            com.dtn.apply_job.util.constant.enums.ApplicationStatus statusEnum = parseStatus(status);
            Specification<Application> statusSpec = (root, query, cb) -> cb.equal(root.get("status"), statusEnum);
            roleSpec = roleSpec == null ? statusSpec : roleSpec.and(statusSpec);
        }


        Pageable customPageable = PageRequest.of(
                pageable.getPageNumber(),
                pageable.getPageSize(),
                customSort
        );

        Page<Application> pageData = applicationRepository.findAll(roleSpec, customPageable);

        List<ResApplicationDTO> results = pageData.getContent().stream()
                .map(this::convertToResAppDTO)
                .collect(java.util.stream.Collectors.toList());

        ResultPaginationDTO resultPaginationDTO = new ResultPaginationDTO();
        ResultPaginationDTO.Meta meta = new ResultPaginationDTO.Meta();
        meta.setPage(pageData.getNumber() + 1);
        meta.setPageSize(pageData.getSize());
        meta.setPages(pageData.getTotalPages());
        meta.setTotal(pageData.getTotalElements());

        resultPaginationDTO.setMeta(meta);
        resultPaginationDTO.setResult(results);

        return resultPaginationDTO;
    }

    @Transactional
    public ResUpdateApplicationDTO handleUpdateStatus(long id, ReqUpdateApplicationStatusDTO reqDTO) throws Exception {
        Application app = getAppAndCheckAccess(id);

        String email = SecurityUtil.getCurrentUser().get();
        User currentUser = userRepository.findByEmail(email);
        boolean isCandidate = currentUser.getRoles().stream().anyMatch(r -> r.getName().name().equals("ROLE_CANDIDATE") || r.getName().name().equals("CANDIDATE"));

        if (isCandidate) {
            throw new org.springframework.security.access.AccessDeniedException("Người ứng tuyển không được phép thay đổi trạng thái ứng tuyển!");
        }

        String statusName = reqDTO.getStatus().name();
        String finalHrMessage = "";

        if (statusName.equals("INTERVIEW")) {

            if (reqDTO.getInterviewTime() == null || reqDTO.getInterviewLocation() == null || reqDTO.getInterviewLocation().isBlank()) {
                throw new IdInvalidException("Vui lòng cung cấp đầy đủ Thời gian và Địa điểm phỏng vấn!");
            }

            // Bắt lỗi: HR phải liên kết Gmail rồi mới được gửi thư
            if (currentUser.getGoogleRefreshToken() == null) {
                throw new IdInvalidException("Vui lòng vào trang Hồ sơ để liên kết tài khoản Gmail trước khi lên lịch phỏng vấn!");
            }

            finalHrMessage = "Vui lòng chuẩn bị kỹ lưỡng và phản hồi lại email này để xác nhận khả năng tham dự phỏng vấn của bạn. Hẹn gặp lại bạn!";
            if (reqDTO.getInterviewMessage() != null && !reqDTO.getInterviewMessage().isBlank()) {
                finalHrMessage = reqDTO.getInterviewMessage().trim();
            }

            app.setInterviewTime(reqDTO.getInterviewTime());
            app.setInterviewLocation(reqDTO.getInterviewLocation().trim());
            app.setInterviewMessage(finalHrMessage);
        }

        app.setStatus(reqDTO.getStatus());
        Application updatedApp = applicationRepository.save(app);

        try {
            User candidate = updatedApp.getResume().getCandidate();
            String jobTitle = updatedApp.getJob().getName();

            String title = "Cập nhật trạng thái ứng tuyển";
            String message = "Đơn ứng tuyển của bạn cho vị trí [" + jobTitle + "] đã chuyển sang trạng thái: " + statusName;

            switch (statusName) {
                case "APPROVED":
                    title = "🎉 Chúc mừng! Đơn ứng tuyển được duyệt";
                    message = "Nhà tuyển dụng đã duyệt CV của bạn cho vị trí [" + jobTitle + "].";
                    break;
                case "REJECTED":
                    title = "Cập nhật trạng thái ứng tuyển";
                    message = "Rất tiếc, đơn ứng tuyển vị trí [" + jobTitle + "] của bạn chưa phù hợp ở thời điểm hiện tại.";
                    break;
                case "REVIEWING":
                    title = "CV đang được xem xét";
                    message = "Nhà tuyển dụng đang xem xét CV của bạn cho vị trí [" + jobTitle + "].";
                    break;
                case "INTERVIEW":
                    title = "📅 Lời mời phỏng vấn!";
                    message = "Bạn có một lời mời phỏng vấn cho vị trí [" + jobTitle + "]. Vui lòng kiểm tra email để xem chi tiết thời gian và địa điểm.";

                    // Format thời gian đẹp để nhét vào Email
                    java.time.ZoneId zoneId = java.time.ZoneId.of("Asia/Ho_Chi_Minh");
                    java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("HH:mm - dd/MM/yyyy").withZone(zoneId);
                    String formattedTime = formatter.format(reqDTO.getInterviewTime());

                    // Gọi Gmail OAuth Service gửi thư thật bằng mail HR
                    gmailOAuthService.sendInterviewInvitationAsync(
                            currentUser,
                            candidate.getEmail(),
                            candidate.getName(),
                            updatedApp.getJob().getCompany().getName(),
                            jobTitle,
                            formattedTime,
                            reqDTO.getInterviewLocation(),
                            finalHrMessage
                    );
                    break;
            }

            // Bắn thông báo lên chuông Web
            notificationService.sendToUser(
                    candidate,
                    title,
                    message,
                    "APPLICATION_STATUS_UPDATED",
                    updatedApp.getId(),
                    com.dtn.apply_job.util.constant.enums.ERole.CANDIDATE
            );

        } catch (Exception e) {
            System.err.println(">>> Lỗi khi gửi thông báo/email: " + e.getMessage());
        }

        return convertToResUpdateAppDTO(updatedApp);
    }

    @Transactional
    public ResApplicationDTO handleUpdateAppByCandidate(long id, ReqUpdateAppByCandidateDTO reqDTO) throws Exception {

        Application app = getAppAndCheckAccess(id);

        String email = SecurityUtil.getCurrentUser().get();
        User currentUser = userRepository.findByEmail(email);

        if (app.getResume().getCandidate().getId() != currentUser.getId()) {
            throw new Exception("Bạn không có quyền chỉnh sửa hồ sơ ứng tuyển này!");
        }

        if (!app.getStatus().name().equals("PENDING")) {
            throw new Exception("Hồ sơ ứng tuyển của bạn đã được nhà tuyển dụng tiếp nhận. Bạn không thể thay đổi thông tin được nữa!");
        }

        boolean isResumeChanged = false;


        if (reqDTO.getResumeId() != null && reqDTO.getResumeId() != app.getResume().getId()) {
            Resume newResume = resumeRepository.findById(reqDTO.getResumeId())
                    .orElseThrow(() -> new IdInvalidException("CV mới không tồn tại!"));

            if (newResume.getCandidate().getId() != currentUser.getId()) {
                throw new Exception("Bạn không thể dùng CV của người khác!");
            }

            app.setResume(newResume);
            isResumeChanged = true;

            app.setMatchScore(null);
        }

        if (reqDTO.getCoverLetter() != null) {
            app.setCoverLetter(reqDTO.getCoverLetter());
        }

        Application updatedApp = applicationRepository.save(app);

        if (isResumeChanged) {


        }


        return convertToResAppDTO(updatedApp);
    }


    private ResApplicationDTO convertToResAppDTO(Application app) {
        ResApplicationDTO dto = new ResApplicationDTO();
        dto.setId(app.getId());
        dto.setStatus(app.getStatus());
        dto.setMatchScore(app.getMatchScore());
        dto.setMatchedSkills(app.getMatchedSkills());
        dto.setMissingSkills(app.getMissingSkills());
        dto.setCoverLetter(app.getCoverLetter());
        dto.setHasCoverLetter(app.getCoverLetter() != null && !app.getCoverLetter().trim().isEmpty());
        dto.setAppliedAt(app.getAppliedAt());
        dto.setInterviewTime(app.getInterviewTime());
        dto.setInterviewLocation(app.getInterviewLocation());
        dto.setInterviewMessage(app.getInterviewMessage());

        ResApplicationDTO.JobInfo jobInfo = new ResApplicationDTO.JobInfo();
        jobInfo.setId(app.getJob().getId());
        jobInfo.setName(app.getJob().getName());
        jobInfo.setMinSalary(app.getJob().getMinSalary());
        jobInfo.setMaxSalary(app.getJob().getMaxSalary());
        jobInfo.setCompanyName(app.getJob().getCompany().getName());
        jobInfo.setLocation(app.getJob().getLocation());
        jobInfo.setCompanyLogo(app.getJob().getCompany().getLogo());
        dto.setJob(jobInfo);

        ResApplicationDTO.ResumeInfo resumeInfo = new ResApplicationDTO.ResumeInfo();
        resumeInfo.setId(app.getResume().getId());
        resumeInfo.setFileName(app.getResume().getFileName());
        resumeInfo.setFileUrl(app.getResume().getFileUrl());
        dto.setResume(resumeInfo);

        ResApplicationDTO.CandidateInfo candInfo = new ResApplicationDTO.CandidateInfo();
        candInfo.setId(app.getResume().getCandidate().getId());
        candInfo.setName(app.getResume().getCandidate().getName());
        candInfo.setEmail(app.getResume().getCandidate().getEmail());
        candInfo.setAvatarUrl(app.getResume().getCandidate().getAvatarUrl());
        dto.setCandidate(candInfo);

        return dto;
    }

    private ResUpdateApplicationDTO convertToResUpdateAppDTO(Application app) {
        ResUpdateApplicationDTO dto = new ResUpdateApplicationDTO();
        dto.setId(app.getId());
        dto.setStatus(app.getStatus());
        return dto;
    }


    public ResultPaginationDTO handleGetApplicationsForHrAndAdmin(Specification<Application> spec, Pageable pageable, String status) throws Exception {


        String email = SecurityUtil.getCurrentUser()
                .orElseThrow(() -> new IdInvalidException("Login please!"));
        User currentUser = userRepository.findByEmail(email);

        boolean isAdmin = currentUser.getRoles().stream()
                .anyMatch(r -> r.getName().name().equals("ROLE_ADMIN") || r.getName().name().equals("ADMIN"));

        boolean isEmployer = currentUser.getRoles().stream()
                .anyMatch(r -> r.getName().name().equals("ROLE_EMPLOYER") || r.getName().name().equals("EMPLOYER"));

        if (!isAdmin && !isEmployer) {
            throw new AccessDeniedException("Bạn không có quyền xem danh sách này!");
        }

        Specification<Application> finalSpec = spec;

        if (status != null && !status.trim().isEmpty()) {
            com.dtn.apply_job.util.constant.enums.ApplicationStatus statusEnum = parseStatus(status);
            Specification<Application> statusSpec = (root, query, cb) -> cb.equal(root.get("status"), statusEnum);
            finalSpec = finalSpec == null ? statusSpec : finalSpec.and(statusSpec);
        }


        if (isEmployer && !isAdmin) {
            if (currentUser.getCompany() == null) {
                throw new IdInvalidException("Bạn chưa tham gia vào công ty nào!");
            }
            long companyId = currentUser.getCompany().getId();


            Specification<Application> securitySpec = (root, query, cb) ->
                    cb.equal(root.get("job").get("company").get("id"), companyId);


            finalSpec = (finalSpec == null) ? securitySpec : finalSpec.and(securitySpec);
        }


        Page<Application> pageApp = applicationRepository.findAll(finalSpec, pageable);


        List<ResApplicationDTO> listAppDTO = pageApp.getContent().stream()
                .map(this::convertToResAppDTO)
                .collect(Collectors.toList());


        ResultPaginationDTO rs = new ResultPaginationDTO();
        ResultPaginationDTO.Meta meta = new ResultPaginationDTO.Meta();
        meta.setPage(pageApp.getNumber() + 1);
        meta.setPageSize(pageApp.getSize());
        meta.setPages(pageApp.getTotalPages());
        meta.setTotal(pageApp.getTotalElements());

        rs.setMeta(meta);
        rs.setResult(listAppDTO);

        return rs;
    }

    private com.dtn.apply_job.util.constant.enums.ApplicationStatus parseStatus(String status) throws IdInvalidException {
        try {
            return com.dtn.apply_job.util.constant.enums.ApplicationStatus.valueOf(status.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new IdInvalidException("Trạng thái không hợp lệ: " + status);
        }
    }
}
