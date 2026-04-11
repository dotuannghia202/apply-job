package com.dtn.apply_job.repository;

import com.dtn.apply_job.domain.Resume;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface ResumeRepository extends JpaRepository<Resume, Long>, JpaSpecificationExecutor<Resume> {
    boolean existsBySpecializationId(Long specializationId);
}
