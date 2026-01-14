package com.dtn.apply_job.service;

import com.dtn.apply_job.domain.Skill;
import com.dtn.apply_job.domain.response.user.ResultPaginationDTO;
import com.dtn.apply_job.domain.skill.ResUpdateDTO;
import com.dtn.apply_job.exception.IdInvalidException;
import com.dtn.apply_job.exception.NameExistedException;
import com.dtn.apply_job.repository.SkillRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class SkillService {
    private final SkillRepository skillRepository;

    public SkillService(SkillRepository skillRepository) {
        this.skillRepository = skillRepository;
    }

    public Skill handleCreateSkill(Skill skill) {
        if (this.skillRepository.existsByName(skill.getName().toString())) {
            throw new NameExistedException("Skill name is unique!");
        }
        return this.skillRepository.save(skill);
    }

    public ResUpdateDTO handleUpdateSkill(long id, Skill skill) throws IdInvalidException {
        Optional<Skill> skillOptional = this.skillRepository.findById(id);
        if (!skillOptional.isPresent()) {
            throw new IdInvalidException("Skill id not found");
        }
        Skill currentSkill = skillOptional.get();
        currentSkill.setName(skill.getName());
        Skill newSkill = this.skillRepository.save(currentSkill);

        ResUpdateDTO resUpdateDTO = new ResUpdateDTO();
        resUpdateDTO.setName(newSkill.getName().toString());
        resUpdateDTO.setUpdatedAt(newSkill.getUpdatedAt());
        resUpdateDTO.setUpdatedBy(newSkill.getUpdatedBy());
        return resUpdateDTO;
    }

    public Skill handleGetSkillById(long id) throws IdInvalidException {
        if (!this.skillRepository.existsById(id)) {
            throw new IdInvalidException("Skill id not found!");
        }
        return this.skillRepository.findById(id).get();
    }

    public ResultPaginationDTO handleGetAllSkills(Specification<Skill> spec, Pageable pageable) {

        Page<Skill> skillPage = this.skillRepository.findAll(spec, pageable);

        ResultPaginationDTO resultPaginationDTO = new ResultPaginationDTO();
        ResultPaginationDTO.Meta meta = new ResultPaginationDTO.Meta();
        meta.setPage(skillPage.getNumber() + 1);
        meta.setPageSize(skillPage.getSize());
        meta.setPages(skillPage.getTotalPages());
        meta.setTotal(skillPage.getTotalElements());

        resultPaginationDTO.setMeta(meta);
        resultPaginationDTO.setResult(skillPage.getContent());

        return resultPaginationDTO;

    }
}
