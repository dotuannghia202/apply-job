package com.dtn.apply_job.repository;

import com.dtn.apply_job.domain.Industry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface IndustryRepository extends JpaRepository<Industry, Long>, JpaSpecificationExecutor<Industry> {
    boolean existsByName(String name);
}

