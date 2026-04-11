package com.dtn.apply_job.repository;

import com.dtn.apply_job.domain.Specialization;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface SpecializationRepository extends JpaRepository<Specialization, Long>, JpaSpecificationExecutor<Specialization> {
    boolean existsByIndustryId(long industryId);

    boolean existsByNameAndIndustryId(String name, long industryId);
}

