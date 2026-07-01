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
import com.dtn.apply_job.util.constant.enums.ERole;
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


    public ApplicationService(ApplicationRepository applicationRepository, UserRepository userRepository, JobRepository jobRepository, ResumeRepository resumeRepository, AiPythonService aiPythonService, NotificationService notificationService) {
        this.applicationRepository = applicationRepository;
        this.userRepository = userRepository;
        this.jobRepository = jobRepository;
        this.resumeRepository = resumeRepository;
        this.aiPythonService = aiPythonService;
        this.notificationService = notificationService;
    }

    // HÀM KIỂM TRA QUYỀN TRUY CẬP (CHỐNG LỖI IDOR)
    private Application getAppAndCheckAccess(long id) throws Exception {
        Application app = applicationRepository.findById(id)
                .orElseThrow(() -> new IdInvalidException("The application form does not exist!"));

        String email = SecurityUtil.getCurrentUser().orElseThrow(() -> new Exception("Not logged in yet!"));
        User currentUser = userRepository.findByEmail(email);

        boolean isAdmin = currentUser.getRoles().stream().anyMatch(r -> r.getName().name().equals("ADMIN"));
        boolean isEmployer = currentUser.getRoles().stream().anyMatch(r -> r.getName().name().equals("EMPLOYER"));

        if (isAdmin) return app;

        if (isEmployer) {
            // HR chỉ được xem đơn ứng tuyển vào Công ty của mình
            if (currentUser.getCompany() == null ||
                    app.getJob().getCompany().getId() != currentUser.getCompany().getId()) {
                throw new Exception("You are not allowed to view job applications from other companies!");
            }
            return app;
        }

        // Nếu là CANDIDATE: Chỉ được xem đơn của chính mình
        if (app.getResume().getCandidate().getId() != currentUser.getId()) {
            throw new Exception("You are not allowed to view other people's applications!");
        }

        return app;
    }


    @Transactional
    public ResCreateApplicationDTO handleCreateApplication(ReqCreateApplicationDTO reqDTO) throws IdInvalidException, Exception {

        // 1. LẤY THÔNG TIN ỨNG VIÊN ĐANG ĐĂNG NHẬP
        String email = SecurityUtil.getCurrentUser().orElseThrow();
        User candidate = userRepository.findByEmail(email);

        // 2. CHECK BẢO MẬT CV (Chống lỗi IDOR)
        Resume resume = resumeRepository.findById(reqDTO.getResumeId())
                .orElseThrow(() -> new IdInvalidException("CV doesn't exist!"));

        // Bắt buộc phải kiểm tra CV này CÓ ĐÚNG LÀ CỦA USER ĐANG ĐĂNG NHẬP KHÔNG?
        if (resume.getCandidate().getId() != candidate.getId()) {
            throw new Exception("You are not allowed to use someone else's CV!");
        }

        // 3. CHECK JOB HỢP LỆ
        Job job = jobRepository.findById(reqDTO.getJobId())
                .orElseThrow(() -> new IdInvalidException("Job doesn't exist!"));

        if (!job.getActive()) {
            throw new Exception("This job is closed or the application period has expired!");
        }

        // 4. CHỐNG SPAM (1 User chỉ nộp 1 Job đúng 1 lần)
        boolean isAlreadyApplied = applicationRepository.existsByResumeCandidateAndJob(candidate, job);
        if (isAlreadyApplied) {
            throw new Exception("You have already applied for this job. Please check the Manage Applications page!");
        }

        // 5. TẠO ĐƠN ỨNG TUYỂN MỚI
        Application application = new Application();
        application.setJob(job);
        application.setResume(resume);
        application.setCoverLetter(reqDTO.getCoverLetter());

        // Match Score sẽ được gọi bằng Python AI ở Background (Tạm gán null)
        application.setMatchScore(null);

        Application savedApp = applicationRepository.save(application);


        // 6. THÊM MỚI: BẮN THÔNG BÁO CHO NHÀ TUYỂN DỤNG (HR) CỦA CÔNG TY ĐÓ
        try {
            // Lấy danh sách HR thuộc công ty đăng Job này
            Company company = job.getCompany();

            if (company != null && company.getUsers() != null) {
                String candidateName = candidate.getName();
                String jobTitle = job.getName();

                String title = "Có ứng viên mới nộp CV!";
                String message = "Ứng viên " + candidateName + " vừa ứng tuyển vào vị trí [" + jobTitle + "].";

                // Duyệt qua tất cả các user thuộc công ty này
                for (User hr : company.getUsers()) {
                    // Kiểm tra chắc chắn user này có quyền HR/EMPLOYER (tránh gửi nhầm cho Admin nếu có)
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


        // 7. KÍCH HOẠT AI CHẠY NGẦM SAU KHI LƯU DB
        // 7.1. Gộp Job Description và Requirements thành 1 khối Text để AI đọc
        String jobTextForAI = "";
        if (job.getDescription() != null) jobTextForAI += job.getDescription() + "\n";
        if (job.getRequirements() != null) jobTextForAI += job.getRequirements();

        // 7.2. Lấy Text của CV đã được Python bóc tách từ trước
        String cvTextForAI = resume.getParsedText();

        // 7.3. Gọi Service chạy ngầm
        aiPythonService.calculateMatchScoreAsync(savedApp.getId(), jobTextForAI, cvTextForAI);

        ResCreateApplicationDTO res = new ResCreateApplicationDTO();
        res.setId(savedApp.getId());
        res.setStatus(savedApp.getStatus());
        res.setAppliedAt(savedApp.getAppliedAt());
        return res;
    }

    // 2. LẤY CHI TIẾT (Read by ID) - Áp dụng hàm Check Access
    public ResApplicationDTO handleGetAppById(long id) throws Exception {
        Application app = getAppAndCheckAccess(id);
        return convertToResAppDTO(app);
    }

    // 3. LẤY DANH SÁCH (Read All) - Tự động Lọc theo Role
    public ResultPaginationDTO handleGetAllApps(Specification<Application> spec, Pageable pageable,
                                                String status) throws Exception {
        String email = SecurityUtil.getCurrentUser().orElseThrow();
        User currentUser = userRepository.findByEmail(email);

        // Kiểm tra Role (Thêm check cả có/không có tiền tố ROLE_ cho an toàn)
        boolean isAdmin = currentUser.getRoles().stream()
                .anyMatch(r -> r.getName().name().equals("ROLE_ADMIN") || r.getName().name().equals("ADMIN"));
        boolean isEmployer = currentUser.getRoles().stream()
                .anyMatch(r -> r.getName().name().equals("ROLE_EMPLOYER") || r.getName().name().equals("EMPLOYER"));

        Specification<Application> roleSpec;
        Sort customSort; // 👉 Khai báo biến chứa luật Sắp xếp

        if (isAdmin) {
            roleSpec = spec;
            // Admin: Mặc định xem mới nhất lên đầu
            customSort = Sort.by(Sort.Direction.DESC, "appliedAt");

        } else if (isEmployer) {
            long companyId = currentUser.getCompany() != null ? currentUser.getCompany().getId() : 0;
            Specification<Application> hrSpec = (root, query, cb) ->
                    cb.equal(root.get("job").get("company").get("id"), companyId);
            roleSpec = spec == null ? hrSpec : spec.and(hrSpec);

            // 👉 NHÀ TUYỂN DỤNG: Sắp xếp theo Điểm AI giảm dần (Match Score DESC).
            // Mẹo: Nếu 2 người điểm bằng nhau, thì ai nộp trước (appliedAt DESC) lên trước.
            customSort = Sort.by(Sort.Direction.DESC, "matchScore")
                    .and(Sort.by(Sort.Direction.DESC, "appliedAt"));

        } else {
            Specification<Application> candSpec = (root, query, cb) ->
                    cb.equal(root.get("resume").get("candidate").get("id"), currentUser.getId());
            roleSpec = spec == null ? candSpec : spec.and(candSpec);

            // 👉 ỨNG VIÊN: Sắp xếp theo Ngày nộp giảm dần (Mới nhất lên đầu)
            customSort = Sort.by(Sort.Direction.DESC, "appliedAt");
        }

        if (status != null && !status.trim().isEmpty()) {
            com.dtn.apply_job.util.constant.enums.ApplicationStatus statusEnum = parseStatus(status);
            Specification<Application> statusSpec = (root, query, cb) -> cb.equal(root.get("status"), statusEnum);
            roleSpec = roleSpec == null ? statusSpec : roleSpec.and(statusSpec);
        }

        // =========================================================
        // 👉 ĐÓNG GÓI LẠI PAGEABLE MỚI:
        // Lấy số trang và số phần tử từ Frontend, nhưng ÉP dùng luật Sắp xếp (customSort) của Backend
        // =========================================================
        Pageable customPageable = PageRequest.of(
                pageable.getPageNumber(),
                pageable.getPageSize(),
                customSort
        );

        // Truyền customPageable vào Database
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

    // 4. CẬP NHẬT TRẠNG THÁI (Chỉ HR/Admin)
    // =======================================================
    public ResUpdateApplicationDTO handleUpdateStatus(long id, ReqUpdateApplicationStatusDTO reqDTO) throws Exception {
        // Hàm này tự chặn Candidate
        Application app = getAppAndCheckAccess(id);

        String email = SecurityUtil.getCurrentUser().get();
        User currentUser = userRepository.findByEmail(email);
        boolean isCandidate = currentUser.getRoles().stream().anyMatch(r -> r.getName().name().equals("ROLE_CANDIDATE"));

        if (isCandidate) {
            throw new Exception("Applicants are not allowed to change the application status!");
        }

        app.setStatus(reqDTO.getStatus());
        Application updatedApp = applicationRepository.save(app);

        // BẮT ĐẦU LOGIC BẮN THÔNG BÁO CHO ỨNG VIÊN
        // =========================================================
        try {
            // 1. Tìm ra ai là ứng viên của đơn này và tên công việc là gì
            User candidate = updatedApp.getResume().getCandidate();
            String jobTitle = updatedApp.getJob().getName();
            String statusName = updatedApp.getStatus().name();

            // 2. Tùy chỉnh lời nhắn dựa vào trạng thái (Bạn hãy sửa lại các case cho khớp với Enum của bạn)
            String title = "Cập nhật trạng thái ứng tuyển";
            String message = "Đơn ứng tuyển của bạn cho vị trí [" + jobTitle + "] đã chuyển sang trạng thái: " + statusName;

            switch (statusName) {
                case "APPROVED": // Hoặc ACCEPTED tùy bạn đặt
                    title = "🎉 Chúc mừng! Đơn ứng tuyển được duyệt";
                    message = "Nhà tuyển dụng đã duyệt CV của bạn cho vị trí [" + jobTitle + "]. Vui lòng kiểm tra email để xem lịch phỏng vấn.";
                    break;
                case "REJECTED":
                    title = "Cập nhật trạng thái ứng tuyển";
                    message = "Rất tiếc, đơn ứng tuyển vị trí [" + jobTitle + "] của bạn chưa phù hợp ở thời điểm hiện tại.";
                    break;
                case "REVIEWING":
                    title = "CV đang được xem xét";
                    message = "Nhà tuyển dụng đang xem xét CV của bạn cho vị trí [" + jobTitle + "].";
                    break;
            }

            // 3. Gọi hàm sendToUser (Nhớ import ERole nếu file này chưa có)
            notificationService.sendToUser(
                    candidate,
                    title,
                    message,
                    "APPLICATION_STATUS_UPDATED",
                    updatedApp.getId(),
                    ERole.CANDIDATE // 👉 Ép thẳng cho ROLE USER (Ứng viên)
            );
        } catch (Exception e) {
            // In lỗi ra console nhưng không làm gián đoạn việc cập nhật trạng thái
            System.err.println("Lỗi khi gửi thông báo: " + e.getMessage());
        }
        // =========================================================
        // KẾT THÚC LOGIC BẮN THÔNG BÁO
        return convertToResUpdateAppDTO(updatedApp);
    }

    // CẬP NHẬT ĐƠN ỨNG TUYỂN (DÀNH CHO ỨNG VIÊN)
    @Transactional
    public ResApplicationDTO handleUpdateAppByCandidate(long id, ReqUpdateAppByCandidateDTO reqDTO) throws Exception {

        // 1. Lấy đơn ứng tuyển (Hàm này đã tự chặn nếu user cố tình sửa đơn của người khác)
        Application app = getAppAndCheckAccess(id);

        String email = SecurityUtil.getCurrentUser().get();
        User currentUser = userRepository.findByEmail(email);

        // 2. CHECK QUYỀN SỞ HỮU (Đảm bảo người gọi API là Chủ đơn, không phải HR)
        if (app.getResume().getCandidate().getId() != currentUser.getId()) {
            throw new Exception("You do not have the right to edit this application!");
        }

        // 3. LUẬT KIM CƯƠNG: Chỉ được sửa khi HR chưa xem (PENDING)
        if (!app.getStatus().name().equals("PENDING")) {
            throw new Exception("Your application has been received by the employer. You can no longer change your information!");
        }

        boolean isResumeChanged = false;

        // 4. Nếu ứng viên muốn đổi CV khác
        if (reqDTO.getResumeId() != null && reqDTO.getResumeId() != app.getResume().getId()) {
            Resume newResume = resumeRepository.findById(reqDTO.getResumeId())
                    .orElseThrow(() -> new IdInvalidException("CV mới không tồn tại!"));

            // Check bảo mật: CV mới này có đúng là của ứng viên này không?
            if (newResume.getCandidate().getId() != currentUser.getId()) {
                throw new Exception("Bạn không thể dùng CV của người khác!");
            }

            app.setResume(newResume);
            isResumeChanged = true;

            // RESET ĐIỂM AI
            app.setMatchScore(null);
        }

        // 5. Nếu ứng viên muốn đổi Thư ứng tuyển
        if (reqDTO.getCoverLetter() != null) {
            app.setCoverLetter(reqDTO.getCoverLetter());
        }

        Application updatedApp = applicationRepository.save(app);

        // ==========================================
        // 6. NẾU ĐỔI CV, GỌI LẠI AI PYTHON ĐỂ CHẤM ĐIỂM
        if (isResumeChanged) {
            // String jobText = app.getJob().getDescription() + " " + app.getJob().getRequirements();
            // String cvText = newResume.getParsedText();
            // aiService.calculateMatchingScoreAsync(updatedApp.getId(), jobText, cvText);
        }
        // ==========================================

        return convertToResAppDTO(updatedApp);
    }


    // HÀM MAPPER (Convert sang DTO)
    private ResApplicationDTO convertToResAppDTO(Application app) {
        ResApplicationDTO dto = new ResApplicationDTO();
        dto.setId(app.getId());
        dto.setStatus(app.getStatus());
        dto.setMatchScore(app.getMatchScore());
        dto.setCoverLetter(app.getCoverLetter());
        dto.setHasCoverLetter(app.getCoverLetter() != null && !app.getCoverLetter().trim().isEmpty());
        dto.setAppliedAt(app.getAppliedAt());

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
        resumeInfo.setFileUrl(app.getResume().getFileUrl()); // <--- ĐÂY LÀ CHỖ HR LẤY LINK ĐỂ XEM CV
        dto.setResume(resumeInfo);

        ResApplicationDTO.CandidateInfo candInfo = new ResApplicationDTO.CandidateInfo();
        candInfo.setId(app.getResume().getCandidate().getId());
        candInfo.setName(app.getResume().getCandidate().getName());
        candInfo.setEmail(app.getResume().getCandidate().getEmail());
        dto.setCandidate(candInfo);

        return dto;
    }

    private ResUpdateApplicationDTO convertToResUpdateAppDTO(Application app) {
        ResUpdateApplicationDTO dto = new ResUpdateApplicationDTO();
        dto.setId(app.getId());
        dto.setStatus(app.getStatus());
        return dto;
    }

    // Nhớ import org.springframework.security.access.AccessDeniedException;

    public ResultPaginationDTO handleGetApplicationsForHrAndAdmin(Specification<Application> spec, Pageable pageable, String status) throws Exception {

        // 1. LẤY THÔNG TIN USER ĐANG ĐĂNG NHẬP
        String email = SecurityUtil.getCurrentUser()
                .orElseThrow(() -> new IdInvalidException("Login please!"));
        User currentUser = userRepository.findByEmail(email);

        boolean isAdmin = currentUser.getRoles().stream()
                .anyMatch(r -> r.getName().name().equals("ROLE_ADMIN") || r.getName().name().equals("ADMIN"));

        boolean isEmployer = currentUser.getRoles().stream()
                .anyMatch(r -> r.getName().name().equals("ROLE_EMPLOYER") || r.getName().name().equals("EMPLOYER"));

        if (!isAdmin && !isEmployer) {
            throw new AccessDeniedException("You do not have permission to view this list!");
        }

        Specification<Application> finalSpec = spec;

        if (status != null && !status.trim().isEmpty()) {
            com.dtn.apply_job.util.constant.enums.ApplicationStatus statusEnum = parseStatus(status);
            Specification<Application> statusSpec = (root, query, cb) -> cb.equal(root.get("status"), statusEnum);
            finalSpec = finalSpec == null ? statusSpec : finalSpec.and(statusSpec);
        }

        // 2. NẾU LÀ HR -> ÉP ĐIỀU KIỆN CHỈ LẤY ĐƠN CỦA CÔNG TY MÌNH
        if (isEmployer && !isAdmin) {
            if (currentUser.getCompany() == null) {
                throw new IdInvalidException("You haven't joined any company yet!");
            }
            long companyId = currentUser.getCompany().getId();

            // SQL: WHERE application.job.company.id = ?
            Specification<Application> securitySpec = (root, query, cb) ->
                    cb.equal(root.get("job").get("company").get("id"), companyId);

            // Nối "lớp giáp bảo mật" vào bộ lọc của Frontend
            finalSpec = (finalSpec == null) ? securitySpec : finalSpec.and(securitySpec);
        }

        // 3. THỰC THI TRUY VẤN CÓ PHÂN TRANG (Kèm bộ lọc)
        Page<Application> pageApp = applicationRepository.findAll(finalSpec, pageable);

        // 4. CHUYỂN ĐỔI SANG DTO (Sử dụng hàm convertToResAppDTO đã viết ở các bài trước)
        List<ResApplicationDTO> listAppDTO = pageApp.getContent().stream()
                .map(this::convertToResAppDTO)
                .collect(Collectors.toList());

        // 5. ĐÓNG GÓI RESULT PAGINATION
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
            throw new IdInvalidException("Invalid status: " + status);
        }
    }
}
