package com.dtn.apply_job.controller;

import com.dtn.apply_job.common.response.ResultPaginationDTO;
import com.dtn.apply_job.domain.Company;
import com.dtn.apply_job.domain.request.company.ReqCreateCompanyDTO;
import com.dtn.apply_job.domain.response.company.ResCreateCompanyDTO;
import com.dtn.apply_job.service.CompanyService;
import com.turkraft.springfilter.boot.Filter;
import jakarta.validation.Valid;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

    @PostMapping("/companies")
    public ResponseEntity<ResCreateCompanyDTO> createCompany(@Valid @RequestBody ReqCreateCompanyDTO company) {
        ResCreateCompanyDTO newCompany = this.companyService.handleCreateCompany(company);
        return ResponseEntity.status(HttpStatus.CREATED).body(newCompany);
    }

    @PostMapping("/companies/batch")
    public ResponseEntity<List<Company>> createCompanies(@Valid @RequestBody List<Company> companies) {
        List<Company> newCompanies = this.companyService.handleCreateCompanies(companies);
        return ResponseEntity.status(HttpStatus.CREATED).body(newCompanies);
    }

    @GetMapping("/companies")
    public ResponseEntity<ResultPaginationDTO> getAllCompanies(@Filter Specification<Company> spec, Pageable pageable) {

        ResultPaginationDTO resultPaginationDTO = this.companyService.handleGetAllCompany(spec, pageable);

        return ResponseEntity.status(HttpStatus.OK).body(resultPaginationDTO);
    }

    @PutMapping("/companies/{id}")
    public ResponseEntity<Company> updateCompany(@PathVariable long id, @Valid @RequestBody Company company) {
        Company companyUpdated = this.companyService.handleUpdateCompany(id, company);
        return ResponseEntity.status(HttpStatus.OK).body(companyUpdated);
    }

    @DeleteMapping("/companies/{id}")
    public ResponseEntity<Void> deleteCompany(@PathVariable long id) {
        this.companyService.handleDeleteCompany(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(null);
    }


}
