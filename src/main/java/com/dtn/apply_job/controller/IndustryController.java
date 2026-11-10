package com.dtn.apply_job.controller;

import com.dtn.apply_job.common.annotation.ApiMessage;
import com.dtn.apply_job.common.response.ResultPaginationDTO;
import com.dtn.apply_job.domain.Industry;
import com.dtn.apply_job.exception.IdInvalidException;
import com.dtn.apply_job.service.IndustryService;
import com.turkraft.springfilter.boot.Filter;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class IndustryController {

    private final IndustryService industryService;

    @PostMapping("/industries")
    @PreAuthorize("hasRole('ADMIN')")
    @ApiMessage("Industry created successfully")
    public ResponseEntity<Industry> createIndustry(@Valid @RequestBody Industry industry) {
        Industry newIndustry = this.industryService.handleCreateIndustry(industry);
        return ResponseEntity.status(HttpStatus.CREATED).body(newIndustry);
    }

    @PutMapping("/industries/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @ApiMessage("Industry updated successfully")
    public ResponseEntity<Industry> updateIndustry(@PathVariable UUID id, @Valid @RequestBody Industry industry)
            throws IdInvalidException {
        Industry updatedIndustry = this.industryService.handleUpdateIndustry(id, industry);
        return ResponseEntity.status(HttpStatus.OK).body(updatedIndustry);
    }

    @DeleteMapping("/industries/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @ApiMessage("Industry deleted successfully")
    public ResponseEntity<Void> deleteIndustry(@PathVariable UUID id) throws IdInvalidException {
        this.industryService.handleDeleteIndustry(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }


    @GetMapping("/industries/{id}")
    @ApiMessage("Industry details retrieved successfully")
    public ResponseEntity<Industry> getIndustry(@PathVariable UUID id) throws IdInvalidException {
        Industry industry = this.industryService.handleGetIndustryById(id);
        return ResponseEntity.status(HttpStatus.OK).body(industry);
    }

    @GetMapping("/industries")
    @ApiMessage("Industries retrieved successfully")
    public ResponseEntity<ResultPaginationDTO> getAllIndustries(
            @Filter Specification<Industry> spec,
            Pageable pageable,
            @RequestParam(required = false) String name
    ) {
        ResultPaginationDTO result = this.industryService.handleGetAllIndustries(spec, pageable, name);
        return ResponseEntity.status(HttpStatus.OK).body(result);
    }
}