package com.dtn.apply_job.repository;

import com.dtn.apply_job.domain.Specialization;
import com.dtn.apply_job.domain.response.specialization.ResSpecializationByIndustryId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.lang.Nullable;

import java.util.List;
import java.util.Optional;

public interface SpecializationRepository extends JpaRepository<Specialization, Long>, JpaSpecificationExecutor<Specialization> {
    boolean existsByIndustryId(long industryId);

    boolean existsByNameAndIndustryId(String name, long industryId);

    @EntityGraph(attributePaths = {"industry"})
    Optional<Specialization> findById(Long id);

    // @EntityGraph để lấy data của các bảng có quan hệ
    @Override
    @EntityGraph(attributePaths = {"industry"})
    Page<Specialization> findAll(@Nullable Specification<Specialization> spec, Pageable pageable);

    @Query("""
            SELECT new com.dtn.apply_job.domain.response.specialization.ResSpecializationByIndustryId(
                s.id, s.name
            )
            FROM Specialization s
            WHERE s.industry.id = :industryId
            ORDER BY s.name ASC
            """)
    List<ResSpecializationByIndustryId> findAllByIndustryIdProjected(@Param("industryId") long industryId);
}

