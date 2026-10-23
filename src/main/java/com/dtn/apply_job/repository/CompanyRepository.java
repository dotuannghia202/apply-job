package com.dtn.apply_job.repository;

import com.dtn.apply_job.domain.Company;
import com.dtn.apply_job.util.constant.enums.CompanyStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface CompanyRepository extends JpaRepository<Company, UUID>, JpaSpecificationExecutor<Company> {
    long count();

    long countByStatus(CompanyStatus status);
}
