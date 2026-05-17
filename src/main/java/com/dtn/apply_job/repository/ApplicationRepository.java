package com.dtn.apply_job.repository;

import com.dtn.apply_job.domain.Application;
import com.dtn.apply_job.domain.Job;
import com.dtn.apply_job.domain.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ApplicationRepository extends JpaRepository<Application, Long>, JpaSpecificationExecutor<Application> {
    Application save(Application application);

    boolean existsByResumeCandidateAndJob(User candidate, Job job);

    @Query("select distinct a.job.id from Application a where a.resume.candidate = :candidate")
    List<Long> findAppliedJobIdsByCandidate(@Param("candidate") User candidate);

    @Override
    @EntityGraph(attributePaths = {"job", "job.company", "resume", "resume.candidate"})
    Page<Application> findAll(@Nullable Specification<Application> spec, Pageable pageable);

    @EntityGraph(attributePaths = {"job", "job.company", "resume", "resume.candidate"})
    Optional<Application> findById(Long id);
}
