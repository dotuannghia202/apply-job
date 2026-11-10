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
import java.util.UUID;

@Setter
@Getter
@RestController
@RequestMapping("/api/v1")
public class CompanyController {

    private CompanyService companyService;

    public CompanyController(CompanyService companyService) {
        this.companyService = companyService;
    }

    @PreAuthorize("hasAnyRole('EMPLOYER', 'ADMIN')")
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
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYER')")
    @ApiMessage("Companies retrieved successfully")
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
    @ApiMessage("Company updated successfully")
    public ResponseEntity<ResCompanyDTO> updateCompany(
            @PathVariable UUID id,
            @Valid @RequestBody ReqUpdateCompanyDTO reqDTO) throws Exception {
        return ResponseEntity.ok(companyService.handleUpdateCompany(id, reqDTO));
    }



    @PutMapping("companies/{id}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    @ApiMessage("Company approval status updated successfully")
    public ResponseEntity<Void> approveCompany(
            @PathVariable("id") UUID companyId,
            @RequestParam("isApproved") boolean isApproved) throws Exception {

        companyService.approveCompany(companyId, isApproved);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/companies/dashboard-stats")
    @PreAuthorize("hasRole('ADMIN')")
    @ApiMessage("Company status statistics retrieved successfully")
    public ResponseEntity<ResCompanyStatsDTO> getCompanyStats() {
        return ResponseEntity.ok(companyService.getCompanyDashboardStats());
    }

    @GetMapping("/my-company")
    @PreAuthorize("hasRole('EMPLOYER')")
    @ApiMessage("My company profile retrieved successfully")
    public ResponseEntity<ResCompanyDTO> getMyCompany() throws Exception {
        return ResponseEntity.ok(companyService.handleGetMyCompany());
    }


    @PutMapping("companies/{id}/suspend")
    @PreAuthorize("hasRole('ADMIN')")
    @ApiMessage("Company suspension status updated successfully")
    public ResponseEntity<Void> toggleSuspendCompany(
            @PathVariable("id") UUID companyId,
            @RequestParam("isSuspended") boolean isSuspended) throws Exception {

        companyService.toggleSuspendCompany(companyId, isSuspended);
        return ResponseEntity.ok().build();
    }

    @GetMapping("companies/{id}")
    @ApiMessage("Company details retrieved successfully")
    public ResponseEntity<ResCompanyDTO> getCompanyById(@PathVariable("id") UUID id) throws Exception {
        return ResponseEntity.ok(companyService.handleGetCompanyById(id));
    }
}
