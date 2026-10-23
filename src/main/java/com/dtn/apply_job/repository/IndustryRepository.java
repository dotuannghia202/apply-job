package com.dtn.apply_job.repository;

import com.dtn.apply_job.domain.Industry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

public interface IndustryRepository extends JpaRepository<Industry, UUID>, JpaSpecificationExecutor<Industry> {
    boolean existsByNameVi(String nameVi);
    boolean existsByNameEn(String nameEn);

    @Query("SELECT CASE WHEN COUNT(i) > 0 THEN true ELSE false END FROM Industry i WHERE LOWER(i.nameVi) = LOWER(:name) OR LOWER(i.nameEn) = LOWER(:name)")
    boolean existsByName(@Param("name") String name);
}
