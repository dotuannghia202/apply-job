package com.dtn.apply_job.repository;

import com.dtn.apply_job.domain.Application;
import com.dtn.apply_job.domain.InterviewSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface InterviewSessionRepository extends JpaRepository<InterviewSession, UUID> {
    Optional<InterviewSession> findByApplication(Application application);
    Optional<InterviewSession> findByApplicationId(UUID applicationId);
}
