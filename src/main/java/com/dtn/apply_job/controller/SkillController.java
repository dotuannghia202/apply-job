package com.dtn.apply_job.controller;

import com.dtn.apply_job.common.annotation.ApiMessage;
import com.dtn.apply_job.common.response.ResultPaginationDTO;
import com.dtn.apply_job.domain.Skill;
import com.dtn.apply_job.domain.response.skill.ResUpdateDTO;
import com.dtn.apply_job.exception.IdInvalidException;
import com.dtn.apply_job.exception.NameExistedException;
import com.dtn.apply_job.service.SkillService;
import com.turkraft.springfilter.boot.Filter;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
public class SkillController {
    private final SkillService skillService;

    public SkillController(SkillService skillService) {
        this.skillService = skillService;
    }

    @PostMapping("/skills")
    @ApiMessage("Skill created successfully")
    public ResponseEntity<Skill> createSkill(@RequestBody Skill skill) throws NameExistedException {
        Skill newSkill = this.skillService.handleCreateSkill(skill);
        return ResponseEntity.status(HttpStatus.CREATED).body(newSkill);
    }

    @PutMapping("/skills/{id}")
    @ApiMessage("Skill updated successfully")
    public ResponseEntity<ResUpdateDTO> updateSkill(@PathVariable UUID id, @RequestBody Skill skill) throws IdInvalidException, NameExistedException {
        ResUpdateDTO result = this.skillService.handleUpdateSkill(id, skill);
        return ResponseEntity.status(HttpStatus.OK).body(result);
    }

    @GetMapping("/skills/{id}")
    @ApiMessage("Skill details retrieved successfully")
    public ResponseEntity<Skill> getSkill(@PathVariable UUID id) throws IdInvalidException {
        Skill skill = this.skillService.handleGetSkillById(id);
        return ResponseEntity.status(HttpStatus.OK).body(skill);
    }

    @GetMapping("/skills")
    @ApiMessage("Skills retrieved successfully")
    public ResponseEntity<ResultPaginationDTO> getAllSkills(
            @Filter Specification<Skill> spec,
            Pageable pageable,
            @RequestParam(required = false) String name
    ) {
        ResultPaginationDTO result = this.skillService.handleGetAllSkills(spec, pageable, name);
        return ResponseEntity.status(HttpStatus.OK).body(result);
    }
}
