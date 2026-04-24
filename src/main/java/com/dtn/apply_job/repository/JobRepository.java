package com.dtn.apply_job.repository;

import com.dtn.apply_job.domain.Job;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface JobRepository extends JpaRepository<Job, Long>, JpaSpecificationExecutor<Job> {

    boolean existsBySpecializationId(Long specializationId);

    @EntityGraph(attributePaths = {"company", "specialization", "skills"})
    Optional<Job> findById(Long id);

    @Override
    @EntityGraph(attributePaths = {"company", "specialization", "skills"})
    Page<Job> findAll(@Nullable Specification<Job> spec, Pageable pageable);
}