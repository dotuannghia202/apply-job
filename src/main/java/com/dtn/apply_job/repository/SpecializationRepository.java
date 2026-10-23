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
import java.util.UUID;

public interface SpecializationRepository extends JpaRepository<Specialization, UUID>, JpaSpecificationExecutor<Specialization> {
    boolean existsByIndustryId(UUID industryId);

    boolean existsByNameViAndIndustryId(String nameVi, UUID industryId);

    boolean existsByNameEnAndIndustryId(String nameEn, UUID industryId);

    @Query("SELECT CASE WHEN COUNT(s) > 0 THEN true ELSE false END FROM Specialization s WHERE s.industry.id = :industryId AND (LOWER(s.nameVi) = LOWER(:name) OR LOWER(s.nameEn) = LOWER(:name))")
    boolean existsByNameAndIndustryId(@Param("name") String name, @Param("industryId") UUID industryId);

    @EntityGraph(attributePaths = {"industry"})
    Optional<Specialization> findById(UUID id);

    // @EntityGraph để lấy data của các bảng có quan hệ
    @Override
    @EntityGraph(attributePaths = {"industry"})
    Page<Specialization> findAll(@Nullable Specification<Specialization> spec, Pageable pageable);

    @Query("""
            SELECT new com.dtn.apply_job.domain.response.specialization.ResSpecializationByIndustryId(
                s.id, s.nameVi, s.nameEn
            )
            FROM Specialization s
            WHERE s.industry.id = :industryId
            ORDER BY s.nameVi ASC
            """)
    List<ResSpecializationByIndustryId> findAllByIndustryIdProjected(@Param("industryId") UUID industryId);
}
