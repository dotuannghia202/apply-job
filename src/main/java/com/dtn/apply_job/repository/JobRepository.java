package com.dtn.apply_job.repository;

import com.dtn.apply_job.domain.Job;
import com.dtn.apply_job.domain.response.admin.IndustryStatProjection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface JobRepository extends JpaRepository<Job, UUID>, JpaSpecificationExecutor<Job> {

    boolean existsBySpecializationId(UUID specializationId);

    @EntityGraph(attributePaths = {"company", "specialization", "skills"})
    Optional<Job> findById(UUID id);

    @Override
    @EntityGraph(attributePaths = {"company", "specialization", "skills"})
    Page<Job> findAll(@Nullable Specification<Job> spec, Pageable pageable);

    @Query("SELECT j FROM User u JOIN u.savedJobs j WHERE u.email = :email")
    Page<Job> findSavedJobsByUserEmail(@Param("email") String email, Pageable pageable);

    long countByCompany_IdAndActiveTrue(UUID companyId);

    long countByActiveTrue();

    // Lấy thống kê cho biểu đồ tròn (Dùng Interface Projection)
    @Query("SELECT s.industry.nameVi AS industryNameVi, s.industry.nameEn AS industryNameEn, COUNT(j) AS jobCount " +
            "FROM Job j JOIN j.specialization s GROUP BY s.industry.nameVi, s.industry.nameEn")
    List<IndustryStatProjection> getJobCountByIndustry();

    @Modifying
    @Transactional
    @Query("UPDATE Job j SET j.active = false WHERE j.endDate < :now AND j.active = true")
    int deactivateExpiredJobs(@Param("now") Instant now);
}