package com.dtn.apply_job.controller;

import com.dtn.apply_job.common.annotation.ApiMessage;
import com.dtn.apply_job.common.response.ResultPaginationDTO;
import com.dtn.apply_job.domain.Company;
import com.dtn.apply_job.domain.request.company.ReqCreateCompanyDTO;
import com.dtn.apply_job.domain.request.company.ReqUpdateCompanyDTO;
import com.dtn.apply_job.domain.response.company.ResCompanyDTO;
import com.dtn.apply_job.domain.response.company.ResCompanyStatsDTO;
import com.dtn.apply_job.domain.response.company.ResCreateCompanyDTO;
import com.dtn.apply_job.service.CompanyService;
import com.dtn.apply_job.util.constant.enums.CompanyStatus;
import com.turkraft.springfilter.boot.Filter;
import jakarta.validation.Valid;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;

@Setter
@Getter
@RestController
@RequestMapping("/api/v1")
public class CompanyController {

    private CompanyService companyService;

    public CompanyController(CompanyService companyService) {
        this.companyService = companyService;
    }

    @PreAuthorize("hasAnyRole('EMPLOYER', 'ADMIN')") // 🚨 CHỈ USER ĐƯỢC GỌI
    @PostMapping("/companies")
    public ResponseEntity<ResCreateCompanyDTO> createCompany(@Valid @RequestBody ReqCreateCompanyDTO company) throws Exception {
        ResCreateCompanyDTO newCompany = this.companyService.handleCreateCompany(company);
        return ResponseEntity.status(HttpStatus.CREATED).body(newCompany);
    }

    @PostMapping("/companies/batch")
    public ResponseEntity<List<Company>> createCompanies(@Valid @RequestBody List<Company> companies) {
        List<Company> newCompanies = this.companyService.handleCreateCompanies(companies);
        return ResponseEntity.status(HttpStatus.CREATED).body(newCompanies);
    }

    @GetMapping("/companies")
    @PreAuthorize("hasRole('ADMIN')") // Lưu ý: API này phục vụ Dashboard nên chỉ Admin được gọi
    @ApiMessage("Fetch all companies with filters")
    public ResponseEntity<ResultPaginationDTO> getAllCompanies(
            @Filter Specification<Company> spec,
            Pageable pageable,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) CompanyStatus status,
            @RequestParam(required = false) Instant startDate,
            @RequestParam(required = false) Instant endDate
    ) {

        ResultPaginationDTO result = this.companyService.handleGetAllCompanyWithFilters(
                spec, pageable, name, status, startDate, endDate);

        return ResponseEntity.ok(result);
    }

    @PutMapping("companies/{id}")
    @PreAuthorize("hasRole('EMPLOYER')")
    @ApiMessage("Cập nhật thông tin công ty thành công")
    public ResponseEntity<ResCompanyDTO> updateCompany(
            @PathVariable long id,
            @Valid @RequestBody ReqUpdateCompanyDTO reqDTO) throws Exception {
        return ResponseEntity.ok(companyService.handleUpdateCompany(id, reqDTO));
    }

//    @DeleteMapping("/companies/{id}")
//    public ResponseEntity<Void> deleteCompany(@PathVariable long id) {
//        this.companyService.handleDeleteCompany(id);
//        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(null);
//    }

    @PutMapping("/{id}/approve")
    @PreAuthorize("hasRole('ADMIN')") // 🚨 CHỈ ADMIN ĐƯỢC GỌI
    @ApiMessage("Cập nhật trạng thái duyệt công ty thành công")
    public ResponseEntity<Void> approveCompany(
            @PathVariable("id") long companyId,
            @RequestParam("isApproved") boolean isApproved) throws Exception {

        companyService.approveCompany(companyId, isApproved);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/companies/dashboard-stats")
    @PreAuthorize("hasRole('ADMIN')")
    @ApiMessage("Lấy thống kê trạng thái công ty")
    public ResponseEntity<ResCompanyStatsDTO> getCompanyStats() {
        return ResponseEntity.ok(companyService.getCompanyDashboardStats());
    }

    @GetMapping("/my-company")
    @PreAuthorize("hasRole('EMPLOYER')")
    @ApiMessage("Lấy hồ sơ công ty của tôi")
    public ResponseEntity<ResCompanyDTO> getMyCompany() throws Exception {
        return ResponseEntity.ok(companyService.handleGetMyCompany());
    }
    

    // 2. API Đình chỉ / Mở khóa công ty đang hoạt động
    @PutMapping("/{id}/suspend")
    @PreAuthorize("hasRole('ADMIN')")
    @ApiMessage("Cập nhật trạng thái đình chỉ công ty thành công")
    public ResponseEntity<Void> toggleSuspendCompany(
            @PathVariable("id") long companyId,
            @RequestParam("isSuspended") boolean isSuspended) throws Exception {

        companyService.toggleSuspendCompany(companyId, isSuspended);
        return ResponseEntity.ok().build();
    }
}
