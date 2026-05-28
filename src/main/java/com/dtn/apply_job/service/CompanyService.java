package com.dtn.apply_job.service;

import com.dtn.apply_job.common.response.ResultPaginationDTO;
import com.dtn.apply_job.domain.Company;
import com.dtn.apply_job.domain.User;
import com.dtn.apply_job.domain.request.company.ReqCreateCompanyDTO;
import com.dtn.apply_job.domain.response.company.ResCompanyDTO;
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

    public Company handleUpdateCompany(long id, Company company) {
        Optional<Company> optionalCompany = this.companyRepository.findById(id);
        if (optionalCompany.isPresent()) {
            Company updatedCompany = optionalCompany.get();
            updatedCompany.setName(company.getName());
            updatedCompany.setAddress(company.getAddress());
            updatedCompany.setLogo(company.getLogo());
            updatedCompany.setDescription(company.getDescription());
            return this.companyRepository.save(updatedCompany);
        }
        return null;
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

    public Optional<Company> handleGetCompanyById(long id) {
        return this.companyRepository.findById(id);
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
}
