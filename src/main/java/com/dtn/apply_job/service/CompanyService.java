package com.dtn.apply_job.service;

import com.dtn.apply_job.common.response.ResultPaginationDTO;
import com.dtn.apply_job.domain.Company;
import com.dtn.apply_job.domain.User;
import com.dtn.apply_job.domain.request.company.ReqCreateCompanyDTO;
import com.dtn.apply_job.domain.request.company.ReqUpdateCompanyDTO;
import com.dtn.apply_job.domain.response.company.ResCompanyDTO;
import com.dtn.apply_job.domain.response.company.ResCompanyStatsDTO;
import com.dtn.apply_job.domain.response.company.ResCreateCompanyDTO;
import com.dtn.apply_job.exception.IdInvalidException;
import com.dtn.apply_job.repository.CompanyRepository;
import com.dtn.apply_job.repository.UserRepository;
import com.dtn.apply_job.security.SecurityUtil;
import com.dtn.apply_job.util.constant.enums.CompanyStatus;
import jakarta.persistence.criteria.Predicate;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Setter
@Getter
@Service
public class CompanyService {
    private final CompanyRepository companyRepository;
    private final UserRepository userRepository;

    private final NotificationService notificationService;
    private final EmailService emailService;

    public CompanyService(CompanyRepository companyRepository, UserRepository userRepository, NotificationService notificationService, EmailService emailService) {
        this.companyRepository = companyRepository;
        this.userRepository = userRepository;
        this.notificationService = notificationService;
        this.emailService = emailService;
    }

    @Transactional
    public ResCreateCompanyDTO handleCreateCompany(ReqCreateCompanyDTO reqDTO) throws Exception {

        // 1. LẤY USER ĐANG ĐĂNG NHẬP (Chặn luôn từ đầu nếu không login, giải quyết lỗi Scope)
        String currentUserEmail = SecurityUtil.getCurrentUser()
                .orElseThrow(() -> new IdInvalidException("Vui lòng đăng nhập để tạo công ty!"));

        User currentUser = this.userRepository.findByEmail(currentUserEmail);
        if (currentUser == null) {
            throw new IdInvalidException("Tài khoản không tồn tại!");
        }

        // 2. LƯU COMPANY VÀO DB VỚI TRẠNG THÁI PENDING
        Company company = new Company();
        company.setStatus(CompanyStatus.PENDING);
        company.setName(reqDTO.getName());
        company.setDescription(reqDTO.getDescription());
        company.setAddress(reqDTO.getAddress());
        company.setLogo(reqDTO.getLogo());

        Company savedCompany = this.companyRepository.save(company);

        // 3. GÁN CÔNG TY CHO USER VÀ CẤP THÊM QUYỀN "EMPLOYER"
        currentUser.setCompany(savedCompany);


        this.userRepository.save(currentUser);

        // 4. GỬI THÔNG BÁO CHO TẤT CẢ ADMIN (Lúc này biến currentUser gọi thoải mái)
        notificationService.sendToAllAdmins(
                "Yêu cầu duyệt công ty mới",
                "Nhà tuyển dụng " + currentUser.getName() + " vừa tạo hồ sơ công ty: " + savedCompany.getName()
        );

        // 5. MAP SANG DTO TRẢ VỀ
        ResCreateCompanyDTO res = new ResCreateCompanyDTO();
        res.setId(savedCompany.getId());
        res.setName(savedCompany.getName());
        res.setDescription(savedCompany.getDescription());
        res.setAddress(savedCompany.getAddress());
        res.setLogo(savedCompany.getLogo());
        res.setCreatedAt(savedCompany.getCreatedAt());
        res.setCreatedBy(savedCompany.getCreatedBy());

        return res;
    }

    public List<Company> handleCreateCompanies(List<Company> companies) {
        if (companies == null || companies.isEmpty()) {
            return List.of();
        }
        return this.companyRepository.saveAll(companies);
    }

    @Transactional
    public ResCompanyDTO handleUpdateCompany(long id, ReqUpdateCompanyDTO reqDTO) throws Exception {
        // 1. Kiểm tra User đang đăng nhập
        String email = SecurityUtil.getCurrentUser()
                .orElseThrow(() -> new IdInvalidException("Vui lòng đăng nhập!"));
        User currentUser = userRepository.findByEmail(email);

        // 2. 🚨 BẢO MẬT IDOR: User này có Công ty không? Và ID công ty có khớp với ID trên URL không?
        if (currentUser.getCompany() == null || currentUser.getCompany().getId() != id) {
            throw new AccessDeniedException("Lỗi bảo mật: Bạn không có quyền sửa thông tin công ty của người khác!");
        }

        // 3. Tiến hành cập nhật
        Company currentCompany = currentUser.getCompany();
        if (reqDTO.getName() != null) currentCompany.setName(reqDTO.getName());
        if (reqDTO.getAddress() != null) currentCompany.setAddress(reqDTO.getAddress());
        if (reqDTO.getDescription() != null) currentCompany.setDescription(reqDTO.getDescription());
        if (reqDTO.getLogo() != null) currentCompany.setLogo(reqDTO.getLogo());

        // 💡 NGHIỆP VỤ PRO (Tùy chọn): Nếu HR sửa thông tin, đổi trạng thái về PENDING để Admin duyệt lại
        // currentCompany.setStatus(CompanyStatus.PENDING);

        Company updatedCompany = companyRepository.save(currentCompany);
        return convertToResCompanyDTO(updatedCompany);
    }

    public void handleDeleteCompany(long id) {
        Optional<Company> optionalCompany = this.companyRepository.findById(id);
        if (optionalCompany.isPresent()) {
            Company company = optionalCompany.get();
            Optional<List<User>> optionalUsers = this.userRepository.findByCompany(company);
            if (optionalUsers.isPresent()) {
                List<User> users = optionalUsers.get();
                this.userRepository.deleteAll(users);
            }
        }
        this.companyRepository.deleteById(id);
    }

    // LẤY CHI TIẾT CÔNG TY (Theo ID)
    public ResCompanyDTO handleGetCompanyById(long id) throws Exception {
        Company company = companyRepository.findById(id)
                .orElseThrow(() -> new IdInvalidException("Công ty không tồn tại!"));

        String email = SecurityUtil.getCurrentUser().orElse("");
        boolean isAdmin = false;
        boolean isOwner = false;

        if (!email.isBlank()) {
            User currentUser = userRepository.findByEmail(email);
            isAdmin = currentUser.getRoles().stream()
                    .anyMatch(r -> r.getName().name().equals("ROLE_ADMIN") || r.getName().name().equals("ADMIN"));

            if (currentUser.getCompany() != null && currentUser.getCompany().getId() == id) {
                isOwner = true;
            }
        }

        // 🚨 CHỐNG RÒ RỈ DỮ LIỆU: Nếu công ty chưa duyệt, chỉ Admin và Chủ mới được xem!
        if (!company.getStatus().name().equals("APPROVED")) {
            if (!isAdmin && !isOwner) {
                // Ném 404 để ứng viên tưởng công ty không tồn tại
                throw new IdInvalidException("Công ty không tồn tại hoặc chưa được phê duyệt!");
            }
        }

        return convertToResCompanyDTO(company); // (Bạn dùng hàm mapper của bạn nhé)
    }

    @Transactional
    public void approveCompany(long companyId, boolean isApproved) throws Exception {
        // 1. Tìm công ty
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new IdInvalidException("Công ty không tồn tại!"));

        // 2. Thay đổi trạng thái
        if (isApproved) {
            company.setStatus(CompanyStatus.APPROVED);
        } else {
            company.setStatus(CompanyStatus.REJECTED);
        }
        companyRepository.save(company);

        // 3. Tìm HR của công ty này để báo tin
        // Tùy thiết kế của bạn, có thể dùng userRepository.findByCompany(company)
        Optional<List<User>> optionalUsers = userRepository.findByCompany(company);

        List<User> employers = optionalUsers.get();
        if (!employers.isEmpty()) {
            User primaryEmployer = employers.get(0); // Lấy người tạo chính

            String title = isApproved ? "Hồ sơ Doanh nghiệp đã được duyệt!" : "Hồ sơ Doanh nghiệp bị từ chối";
            String message = isApproved
                    ? "Công ty " + company.getName() + " của bạn đã được phê duyệt. Bạn có thể bắt đầu đăng tin tuyển dụng ngay bây giờ."
                    : "Rất tiếc, hồ sơ công ty " + company.getName() + " của bạn không hợp lệ. Vui lòng kiểm tra lại thông tin.";

            // 4. Gửi Notification (Lưu DB + Bắn WebSocket)
            notificationService.sendToUser(primaryEmployer, title, message);

            // 5. Gửi Email (Chạy ngầm để không làm chậm Admin)
            new Thread(() -> {
                if (isApproved) {
                    emailService.sendCompanyApprovedEmail(primaryEmployer.getEmail(), company.getName());
                } else {
                    emailService.sendCompanyRejectedEmail(primaryEmployer.getEmail(), company.getName());
                }
            }).start();
        }
    }

    private Specification<Company> buildCompanyFilterSpec(
            String name,
            CompanyStatus status,
            Instant startDate,
            Instant endDate) {


        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // 1. Lọc theo Tên công ty (Tìm kiếm tương đối LIKE)
            if (name != null && !name.trim().isEmpty()) {
                predicates.add(cb.like(cb.lower(root.get("name")), "%" + name.trim().toLowerCase() + "%"));
            }

            // 2. Lọc theo Trạng thái (PENDING, APPROVED, REJECTED)
            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }

            // 3. Lọc khoảng thời gian tạo (Từ ngày startDate -> Đến ngày endDate)
            if (startDate != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"), startDate));
            }
            if (endDate != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("createdAt"), endDate));
            }

            return predicates.isEmpty() ? cb.conjunction() : cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    public ResultPaginationDTO handleGetAllCompanyWithFilters(
            Specification<Company> spec,
            Pageable pageable,
            String name,
            CompanyStatus status,
            Instant startDate,
            Instant endDate) {

        // 1. Gộp điều kiện lọc
        Specification<Company> filterSpec = buildCompanyFilterSpec(name, status, startDate, endDate);
        Specification<Company> finalSpec = spec == null ? filterSpec : spec.and(filterSpec);

        // 2. Chọc xuống DB lấy dữ liệu phân trang
        Page<Company> companyPage = this.companyRepository.findAll(finalSpec, pageable);

        // 3. Chuyển Entity sang DTO (Kèm theo logic lấy thông tin người tạo)
        List<ResCompanyDTO> listDTO = companyPage.getContent().stream().map(company -> {
            ResCompanyDTO dto = new ResCompanyDTO();
            dto.setId(company.getId());
            dto.setName(company.getName());
            dto.setLogo(company.getLogo());
            dto.setCreatedAt(company.getCreatedAt());
            dto.setStatus(company.getStatus());

            // Lấy email người tạo (Từ @PrePersist) để chọc sang bảng User lấy Tên
            String creatorEmail = company.getCreatedBy();
            if (creatorEmail != null && !creatorEmail.isBlank()) {
                User creator = userRepository.findByEmail(creatorEmail);
                if (creator != null) {
                    dto.setEmployerName(creator.getName());
                    dto.setEmployerEmail(creator.getEmail());
                }
            }
            return dto;
        }).toList();

        // 4. Đóng gói Pagination
        ResultPaginationDTO resultPaginationDTO = new ResultPaginationDTO();
        ResultPaginationDTO.Meta meta = new ResultPaginationDTO.Meta();
        meta.setPage(companyPage.getNumber() + 1);
        meta.setPageSize(companyPage.getSize());
        meta.setPages(companyPage.getTotalPages());
        meta.setTotal(companyPage.getTotalElements());

        resultPaginationDTO.setMeta(meta);
        resultPaginationDTO.setResult(listDTO);

        return resultPaginationDTO;
    }

    public ResCompanyStatsDTO getCompanyDashboardStats() {
        ResCompanyStatsDTO stats = new ResCompanyStatsDTO();

        stats.setTotalCompanies(companyRepository.count());
        stats.setPendingApproval(companyRepository.countByStatus(CompanyStatus.PENDING));
        stats.setApproved(companyRepository.countByStatus(CompanyStatus.APPROVED));

        return stats;
    }

    public ResCompanyDTO handleGetMyCompany() throws Exception {
        String email = SecurityUtil.getCurrentUser()
                .orElseThrow(() -> new IdInvalidException("Vui lòng đăng nhập!"));
        User currentUser = userRepository.findByEmail(email);

        if (currentUser.getCompany() == null) {
            throw new IdInvalidException("Bạn chưa có công ty nào!");
        }

        return convertToResCompanyDTO(currentUser.getCompany());
    }

    // =======================================================
    // HÀM MAPPER: CONVERT ENTITY -> DTO
    // =======================================================
    private ResCompanyDTO convertToResCompanyDTO(Company company) {
        ResCompanyDTO dto = new ResCompanyDTO();
        dto.setId(company.getId());
        dto.setName(company.getName());
        dto.setLogo(company.getLogo());
        dto.setAddress(company.getAddress());
        dto.setDescription(company.getDescription());
        dto.setCreatedAt(company.getCreatedAt());
        dto.setStatus(company.getStatus());

        dto.setUpdatedAt(company.getUpdatedAt());
        dto.setUpdatedBy(company.getUpdatedBy());

        // Lấy Email người tạo công ty từ Audit Log (@PrePersist đã tự lưu)
        String creatorEmail = company.getCreatedBy();

        if (creatorEmail != null && !creatorEmail.isBlank()) {
            // Chọc sang UserRepository để tìm tên của người HR này
            User creator = userRepository.findByEmail(creatorEmail);
            if (creator != null) {
                dto.setEmployerName(creator.getName());
                dto.setEmployerEmail(creator.getEmail());
            }
        }

        return dto;
    }

    @Transactional
    public void toggleSuspendCompany(long companyId, boolean isSuspended) throws Exception {
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new IdInvalidException("Công ty không tồn tại!"));

        // Không cho phép đình chỉ công ty chưa được duyệt
        if (company.getStatus() == CompanyStatus.PENDING || company.getStatus() == CompanyStatus.REJECTED) {
            throw new Exception("Chỉ có thể đình chỉ các công ty đã được phê duyệt!");
        }

        // Cập nhật trạng thái
        if (isSuspended) {
            company.setStatus(CompanyStatus.SUSPENDED);
        } else {
            company.setStatus(CompanyStatus.APPROVED);
        }
        companyRepository.save(company);

        // Gửi thông báo cho Chủ công ty
        Optional<List<User>> optionalUsers = userRepository.findByCompany(company);
        if (optionalUsers.isPresent() && !optionalUsers.get().isEmpty()) {
            User primaryEmployer = optionalUsers.get().get(0);

            String title = isSuspended ? "CẢNH BÁO: Đình chỉ hoạt động doanh nghiệp" : "Thông báo: Phục hồi hoạt động doanh nghiệp";
            String message = isSuspended
                    ? "Tài khoản công ty " + company.getName() + " của bạn đã bị đình chỉ do vi phạm chính sách nền tảng. Các tin tuyển dụng của bạn sẽ bị ẩn. Vui lòng liên hệ Admin để được hỗ trợ."
                    : "Tài khoản công ty " + company.getName() + " đã được mở khóa. Bạn có thể tiếp tục hoạt động bình thường.";

            // Bắn Notification (Lưu DB + WebSocket)
            notificationService.sendToUser(primaryEmployer, title, message);

            // Gửi Email (Có thể tự tạo thêm 2 hàm sendCompanySuspendedEmail và sendCompanyRestoredEmail trong EmailService)
            // new Thread(() -> {
            //      if (isSuspended) emailService.sendCompanySuspendedEmail(...);
            //      else emailService.sendCompanyRestoredEmail(...);
            // }).start();
        }
    }
}
