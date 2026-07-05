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

@Repository
public interface JobRepository extends JpaRepository<Job, Long>, JpaSpecificationExecutor<Job> {

    boolean existsBySpecializationId(Long specializationId);

    @EntityGraph(attributePaths = {"company", "specialization", "skills"})
    Optional<Job> findById(Long id);

    @Override
    @EntityGraph(attributePaths = {"company", "specialization", "skills"})
    Page<Job> findAll(@Nullable Specification<Job> spec, Pageable pageable);

    @Query("SELECT j FROM User u JOIN u.savedJobs j WHERE u.email = :email")
    Page<Job> findSavedJobsByUserEmail(@Param("email") String email, Pageable pageable);

    long countByCompany_IdAndActiveTrue(Long companyId);

    long countByActiveTrue();

    // Lấy thống kê cho biểu đồ tròn (Dùng JPQL gom nhóm siêu đỉnh)
    // Lấy thống kê cho biểu đồ tròn (Dùng Interface Projection)
    @Query("SELECT s.industry.name AS industryName, COUNT(j) AS jobCount " +
            "FROM Job j JOIN j.specialization s GROUP BY s.industry.name")
    List<IndustryStatProjection> getJobCountByIndustry();

    @Modifying
    @Transactional
    @Query("UPDATE Job j SET j.active = false WHERE j.endDate < :now AND j.active = true")
    int deactivateExpiredJobs(@Param("now") Instant now);
}