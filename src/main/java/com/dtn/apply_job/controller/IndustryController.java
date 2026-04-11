package com.dtn.apply_job.controller;

import com.dtn.apply_job.common.annotation.ApiMessage;
import com.dtn.apply_job.domain.Industry;
import com.dtn.apply_job.domain.response.user.ResultPaginationDTO;
import com.dtn.apply_job.exception.IdInvalidException;
import com.dtn.apply_job.service.IndustryService;
import com.turkraft.springfilter.boot.Filter;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class IndustryController {

    private final IndustryService industryService;

    @PostMapping("/industries")
    @ApiMessage("Create industry")
    public ResponseEntity<Industry> createIndustry(@Valid @RequestBody Industry industry) {
        Industry newIndustry = this.industryService.handleCreateIndustry(industry);
        return ResponseEntity.status(HttpStatus.CREATED).body(newIndustry);
    }

    @PutMapping("/industries/{id}")
    @ApiMessage("Update industry")
    public ResponseEntity<Industry> updateIndustry(@PathVariable long id, @Valid @RequestBody Industry industry)
            throws IdInvalidException {
        Industry updatedIndustry = this.industryService.handleUpdateIndustry(id, industry);
        return ResponseEntity.status(HttpStatus.OK).body(updatedIndustry);
    }

    @GetMapping("/industries/{id}")
    @ApiMessage("Fetch industry by Id")
    public ResponseEntity<Industry> getIndustry(@PathVariable long id) throws IdInvalidException {
        Industry industry = this.industryService.handleGetIndustryById(id);
        return ResponseEntity.status(HttpStatus.OK).body(industry);
    }

    @GetMapping("/industries")
    @ApiMessage("Fetch all industries")
    public ResponseEntity<ResultPaginationDTO> getAllIndustries(
            @Filter Specification<Industry> spec,
            Pageable pageable
    ) {
        ResultPaginationDTO result = this.industryService.handleGetAllIndustries(spec, pageable);
        return ResponseEntity.status(HttpStatus.OK).body(result);
    }

    @DeleteMapping("/industries/{id}")
    @ApiMessage("Delete industry")
    public ResponseEntity<Void> deleteIndustry(@PathVariable long id) throws IdInvalidException {
        this.industryService.handleDeleteIndustry(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}

