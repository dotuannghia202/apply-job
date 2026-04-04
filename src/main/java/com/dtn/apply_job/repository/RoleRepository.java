package com.dtn.apply_job.repository;

import com.dtn.apply_job.domain.Role;
import com.dtn.apply_job.util.constant.enums.ERole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long>, JpaSpecificationExecutor<Role> {
    boolean existsByName(ERole name);

    Optional<Role> findByName(ERole name);
}
