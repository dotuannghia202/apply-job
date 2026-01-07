package com.dtn.apply_job.repository;

import com.dtn.apply_job.domain.Company;
import com.dtn.apply_job.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long>, JpaSpecificationExecutor<User> {
    User findByEmail(String email);

    Boolean existsByEmail(String email);

    User findByRefreshTokenAndEmail(String refresh_token, String email);

    Optional<List<User>> findByCompany_Id(long companyId);

    Optional<List<User>> findByCompany(Company company);
}
