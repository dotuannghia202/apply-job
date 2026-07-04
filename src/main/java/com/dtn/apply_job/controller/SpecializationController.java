package com.dtn.apply_job.controller;

import com.dtn.apply_job.common.annotation.ApiMessage;
import com.dtn.apply_job.common.response.ResultPaginationDTO;
import com.dtn.apply_job.domain.Specialization;
import com.dtn.apply_job.domain.request.specialization.ReqCreateSpecializationDTO;
import com.dtn.apply_job.domain.response.specialization.ResSpecializationByIndustryId;
import com.dtn.apply_job.domain.response.specialization.ResSpecializationDTO;
import com.dtn.apply_job.exception.IdInvalidException;
import com.dtn.apply_job.service.SpecializationService;
import com.turkraft.springfilter.boot.Filter;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class SpecializationController {

    private final SpecializationService specializationService;

    @PostMapping("/specializations")
    @PreAuthorize("hasRole('ADMIN')")
    @ApiMessage("Tạo chuyên ngành thành công")
    public ResponseEntity<ResSpecializationDTO> createSpecialization(@Valid @RequestBody ReqCreateSpecializationDTO reqCreateSpecializationDTO)
            throws IdInvalidException {
        ResSpecializationDTO newSpecialization = this.specializationService.handleCreateSpecialization(reqCreateSpecializationDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(newSpecialization);
    }

    @PutMapping("/specializations/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @ApiMessage("Cập nhật chuyên ngành thành công")
    public ResponseEntity<Specialization> updateSpecialization(
            @PathVariable long id,
            @Valid @RequestBody Specialization specialization
    ) throws IdInvalidException {
        Specialization updatedSpecialization = this.specializationService.handleUpdateSpecialization(id, specialization);
        return ResponseEntity.status(HttpStatus.OK).body(updatedSpecialization);
    }

    @DeleteMapping("/specializations/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @ApiMessage("Xóa chuyên ngành thành công")
    public ResponseEntity<Void> deleteSpecialization(@PathVariable long id) throws IdInvalidException {
        this.specializationService.handleDeleteSpecialization(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @GetMapping("/specializations/{id}")
    @ApiMessage("Lấy thông tin chuyên ngành thành công")
    public ResponseEntity<ResSpecializationDTO> getSpecialization(@PathVariable long id) throws IdInvalidException {
        ResSpecializationDTO specialization = this.specializationService.handleGetSpecializationById(id);
        return ResponseEntity.status(HttpStatus.OK).body(specialization);
    }

    @GetMapping("/specializations")
    @ApiMessage("Lấy danh sách chuyên ngành thành công")
    public ResponseEntity<ResultPaginationDTO> getAllSpecializations(
            @Filter Specification<Specialization> spec,
            Pageable pageable,
            @RequestParam(required = false) String name
    ) {
        ResultPaginationDTO result = this.specializationService.handleGetAllSpecializations(spec, pageable, name);
        return ResponseEntity.status(HttpStatus.OK).body(result);
    }

    @GetMapping("/specializations/by-industry/{industryId}")
    @ApiMessage("Lấy danh sách chuyên ngành theo mã ngành thành công")
    public ResponseEntity<List<ResSpecializationByIndustryId>> getSpecializationsByIndustryId(
            @PathVariable long industryId
    ) throws IdInvalidException {
        List<ResSpecializationByIndustryId> result = this.specializationService.handleGetSpecializationsByIndustryId(industryId);
        return ResponseEntity.status(HttpStatus.OK).body(result);
    }
}