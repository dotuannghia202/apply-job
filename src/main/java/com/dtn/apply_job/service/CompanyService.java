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
import com.dtn.apply_job.util.constant.enums.ERole;
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

        
        String currentUserEmail = SecurityUtil.getCurrentUser()
                .orElseThrow(() -> new IdInvalidException("Vui lòng đăng nhập để tạo công ty!"));

        User currentUser = this.userRepository.findByEmail(currentUserEmail);
        if (currentUser == null) {
            throw new IdInvalidException("Tài khoản không tồn tại!");
        }

        
        Company company = new Company();
        company.setStatus(CompanyStatus.PENDING);
        company.setName(reqDTO.getName());
        company.setDescription(reqDTO.getDescription());
        company.setAddress(reqDTO.getAddress());
        company.setLogo(reqDTO.getLogo());

        Company savedCompany = this.companyRepository.save(company);

        
        currentUser.setCompany(savedCompany);
        this.userRepository.save(currentUser);

        
        notificationService.sendToAllAdmins(
                "Yêu cầu duyệt công ty mới",
                "Nhà tuyển dụng " + currentUser.getName() + " vừa tạo hồ sơ công ty: " + savedCompany.getName(),
                "NEW_COMPANY_REQUEST"
        );

        
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
        
        String email = SecurityUtil.getCurrentUser()
                .orElseThrow(() -> new IdInvalidException("Vui lòng đăng nhập!"));
        User currentUser = userRepository.findByEmail(email);

        
        if (currentUser.getCompany() == null || currentUser.getCompany().getId() != id) {
            throw new AccessDeniedException("Lỗi bảo mật: Bạn không có quyền sửa thông tin công ty của người khác!");
        }

        
        Company currentCompany = currentUser.getCompany();
        if (reqDTO.getName() != null) currentCompany.setName(reqDTO.getName());
        if (reqDTO.getAddress() != null) currentCompany.setAddress(reqDTO.getAddress());
        if (reqDTO.getDescription() != null) currentCompany.setDescription(reqDTO.getDescription());
        if (reqDTO.getLogo() != null) currentCompany.setLogo(reqDTO.getLogo());

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

        
        if (!company.getStatus().name().equals("APPROVED")) {
            if (!isAdmin && !isOwner) {
                
                throw new IdInvalidException("Công ty không tồn tại hoặc chưa được phê duyệt!");
            }
        }

        return convertToResCompanyDTO(company);
    }

    @Transactional
    public void approveCompany(long companyId, boolean isApproved) throws Exception {
        
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new IdInvalidException("Công ty không tồn tại!"));

        
        if (isApproved) {
            company.setStatus(CompanyStatus.APPROVED);
        } else {
            company.setStatus(CompanyStatus.REJECTED);
        }
        companyRepository.save(company);

        
        Optional<List<User>> optionalUsers = userRepository.findByCompany(company);
        if (optionalUsers.isPresent() && !optionalUsers.get().isEmpty()) {
            User primaryEmployer = optionalUsers.get().get(0); 

            String title = isApproved ? "Hồ sơ Doanh nghiệp đã được duyệt!" : "Hồ sơ Doanh nghiệp bị từ chối";
            String message = isApproved
                    ? "Công ty " + company.getName() + " của bạn đã được phê duyệt. Bạn có thể bắt đầu đăng tin tuyển dụng ngay bây giờ."
                    : "Rất tiếc, hồ sơ công ty " + company.getName() + " của bạn không hợp lệ. Vui lòng kiểm tra lại thông tin.";

            
            String notifType = isApproved ? "COMPANY_APPROVED" : "COMPANY_REJECTED";

            
            notificationService.sendToUser(
                    primaryEmployer,    
                    title,              
                    message,            
                    notifType,          
                    company.getId(),    
                    ERole.EMPLOYER      
            );

            
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

            
            if (name != null && !name.trim().isEmpty()) {
                predicates.add(cb.like(cb.lower(root.get("name")), "%" + name.trim().toLowerCase() + "%"));
            }

            
            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }

            
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

        Specification<Company> filterSpec = buildCompanyFilterSpec(name, status, startDate, endDate);
        Specification<Company> finalSpec = spec == null ? filterSpec : spec.and(filterSpec);

        Page<Company> companyPage = this.companyRepository.findAll(finalSpec, pageable);

        List<ResCompanyDTO> listDTO = companyPage.getContent().stream().map(company -> {
            ResCompanyDTO dto = new ResCompanyDTO();
            dto.setId(company.getId());
            dto.setName(company.getName());
            dto.setLogo(company.getLogo());
            dto.setCreatedAt(company.getCreatedAt());
            dto.setStatus(company.getStatus());

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

        String creatorEmail = company.getCreatedBy();
        if (creatorEmail != null && !creatorEmail.isBlank()) {
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

        if (company.getStatus() == CompanyStatus.PENDING || company.getStatus() == CompanyStatus.REJECTED) {
            throw new Exception("Chỉ có thể đình chỉ các công ty đã được phê duyệt!");
        }

        
        if (isSuspended) {
            company.setStatus(CompanyStatus.SUSPENDED);
        } else {
            company.setStatus(CompanyStatus.APPROVED);
        }
        companyRepository.save(company);

        
        Optional<List<User>> optionalUsers = userRepository.findByCompany(company);
        if (optionalUsers.isPresent() && !optionalUsers.get().isEmpty()) {
            User primaryEmployer = optionalUsers.get().get(0);

            String title = isSuspended ? "CẢNH BÁO: Đình chỉ hoạt động doanh nghiệp" : "Thông báo: Phục hồi hoạt động doanh nghiệp";
            String message = isSuspended
                    ? "Tài khoản công ty " + company.getName() + " của bạn đã bị đình chỉ do vi phạm chính sách nền tảng. Các tin tuyển dụng của bạn sẽ bị ẩn. Vui lòng liên hệ Admin để được hỗ trợ."
                    : "Tài khoản công ty " + company.getName() + " đã được mở khóa. Bạn có thể tiếp tục hoạt động bình thường.";

            
            String notifType = isSuspended ? "COMPANY_SUSPENDED" : "COMPANY_RESTORED";

            
            notificationService.sendToUser(
                    primaryEmployer,
                    title,
                    message,
                    notifType,          
                    company.getId(),
                    ERole.EMPLOYER
            );
        }
    }
}