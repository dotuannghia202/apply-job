package com.dtn.apply_job.controller;

import com.dtn.apply_job.domain.Skill;
import com.dtn.apply_job.domain.response.user.ResultPaginationDTO;
import com.dtn.apply_job.domain.skill.ResUpdateDTO;
import com.dtn.apply_job.exception.IdInvalidException;
import com.dtn.apply_job.exception.NameExistedException;
import com.dtn.apply_job.service.SkillService;
import com.dtn.apply_job.util.annotation.ApiMessage;
import com.turkraft.springfilter.boot.Filter;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
public class SkillController {
    private final SkillService skillService;

    public SkillController(SkillService skillService) {
        this.skillService = skillService;
    }

    @PostMapping("/skills")
    @ApiMessage("Create skill")
    public ResponseEntity<Skill> createSkill(@RequestBody Skill skill) throws NameExistedException {
        Skill newSkill = this.skillService.handleCreateSkill(skill);
        return ResponseEntity.status(HttpStatus.CREATED).body(newSkill);
    }

    @PutMapping("/skills/{id}")
    @ApiMessage("Update skill")
    public ResponseEntity<ResUpdateDTO> updateSkill(@PathVariable long id, @RequestBody Skill skill) throws IdInvalidException {
        ResUpdateDTO result = this.skillService.handleUpdateSkill(id, skill);
        return ResponseEntity.status(HttpStatus.OK).body(result);
    }

    @GetMapping("/skills/{id}")
    @ApiMessage("Fetch skill by Id")
    public ResponseEntity<Skill> getSkill(@PathVariable long id) throws IdInvalidException {
        Skill skill = this.skillService.handleGetSkillById(id);
        return ResponseEntity.status(HttpStatus.OK).body(skill);
    }

    @GetMapping("/skills")
    @ApiMessage("Fetch all skills")
    public ResponseEntity<ResultPaginationDTO> getAllSkills(
            @Filter Specification<Skill> spec,
            Pageable pageable
    ) {
        ResultPaginationDTO result = this.skillService.handleGetAllSkills(spec, pageable);
        return ResponseEntity.status(HttpStatus.OK).body(result);
    }
}
