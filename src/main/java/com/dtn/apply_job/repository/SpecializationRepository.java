package com.dtn.apply_job.repository;

import com.dtn.apply_job.domain.Specialization;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.lang.Nullable;

import java.util.Optional;

public interface SpecializationRepository extends JpaRepository<Specialization, Long>, JpaSpecificationExecutor<Specialization> {
    boolean existsByIndustryId(long industryId);

    boolean existsByNameAndIndustryId(String name, long industryId);

    @EntityGraph(attributePaths = {"industry"})
    Optional<Specialization> findById(Long id);

    @Override
    @EntityGraph(attributePaths = {"industry"})
    Page<Specialization> findAll(@Nullable Specification<Specialization> spec, Pageable pageable);
}

