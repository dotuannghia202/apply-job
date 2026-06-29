package com.dtn.apply_job.repository;

import com.dtn.apply_job.domain.Resume;
import com.dtn.apply_job.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface ResumeRepository extends JpaRepository<Resume, Long>, JpaSpecificationExecutor<Resume> {
    boolean existsBySpecializationId(Long specializationId);

    List<Resume> findByCandidateAndActiveTrue(User candidate);

    List<Resume> findByCandidate(User candidate);
}
