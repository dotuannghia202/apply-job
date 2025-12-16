package com.dtn.apply_job.controller;

import com.dtn.apply_job.domain.Company;
import com.dtn.apply_job.domain.RestRespon;
import com.dtn.apply_job.service.CompanyService;
import jakarta.validation.Valid;
import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Setter
@Getter
@RestController
public class CompanyController {

    private CompanyService companyService;

    public CompanyController (CompanyService companyService) {
        this.companyService = companyService;
    }

    @PostMapping("/companies")
    public ResponseEntity<RestRespon<Company>> createCompany(@Valid @RequestBody Company company) {
        Company newCompany = this.companyService.handleCreateCompany(company);
        return ResponseEntity.status(HttpStatus.CREATED).body(null);
    }

    @GetMapping("/companies")
    public ResponseEntity<List<Company>> getAllCompanies() {
        List<Company> companyList = this.companyService.handleGetAllCompany();
        return ResponseEntity.status(HttpStatus.OK).body(companyList);
    }

    @PutMapping("/companies/{id}")
    public ResponseEntity<Company> updateCompany(@PathVariable long id, @Valid @RequestBody Company company) {
        Company companyUpdated = this.companyService.handleUpdateCompany(id,  company);
        return ResponseEntity.status(HttpStatus.OK).body(companyUpdated);
    }

    @DeleteMapping("/companies/{id}")
    public ResponseEntity<Void> deleteCompany(@PathVariable long id) {
       this.companyService.handleDeleteCompany(id);
       return ResponseEntity.status(HttpStatus.NO_CONTENT).body(null);
    }

}
