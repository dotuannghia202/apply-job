package com.dtn.apply_job.service;

import com.dtn.apply_job.common.response.ResultPaginationDTO;
import com.dtn.apply_job.domain.Skill;
import com.dtn.apply_job.domain.response.skill.ResUpdateDTO;
import com.dtn.apply_job.exception.IdInvalidException;
import com.dtn.apply_job.exception.NameExistedException;
import com.dtn.apply_job.mapper.SkillMapper;
import com.dtn.apply_job.repository.SkillRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
public class SkillService {
    private final SkillRepository skillRepository;
    private final SkillMapper skillMapper;

    public SkillService(SkillRepository skillRepository, SkillMapper skillMapper) {
        this.skillRepository = skillRepository;
        this.skillMapper = skillMapper;
    }

    public Skill handleCreateSkill(Skill skill) {
        if (skill.getNameVi() != null && this.skillRepository.existsByNameVi(skill.getNameVi())) {
            throw new NameExistedException("Tên kỹ năng tiếng Việt phải là duy nhất!");
        }
        if (skill.getNameEn() != null && this.skillRepository.existsByNameEn(skill.getNameEn())) {
            throw new NameExistedException("Tên kỹ năng tiếng Anh phải là duy nhất!");
        }
        if (skill.getNameVi() == null && skill.getNameEn() == null && skill.getName() != null) {
            skill.setNameVi(skill.getName());
            skill.setNameEn(skill.getName());
        }
        return this.skillRepository.save(skill);
    }

    public ResUpdateDTO handleUpdateSkill(UUID id, Skill skill) throws IdInvalidException, NameExistedException {
        Skill currentSkill = this.skillRepository.findById(id)
                .orElseThrow(() -> new IdInvalidException("Không tìm thấy mã kỹ năng"));

        if (skill.getNameVi() != null) {
            boolean isNameChanged = !skill.getNameVi().equals(currentSkill.getNameVi());
            if (isNameChanged && this.skillRepository.existsByNameVi(skill.getNameVi())) {
                throw new NameExistedException("Tên kỹ năng tiếng Việt '" + skill.getNameVi() + "' đã tồn tại!");
            }
            currentSkill.setNameVi(skill.getNameVi());
        }

        if (skill.getNameEn() != null) {
            boolean isNameChanged = !skill.getNameEn().equals(currentSkill.getNameEn());
            if (isNameChanged && this.skillRepository.existsByNameEn(skill.getNameEn())) {
                throw new NameExistedException("Tên kỹ năng tiếng Anh '" + skill.getNameEn() + "' đã tồn tại!");
            }
            currentSkill.setNameEn(skill.getNameEn());
        }

        Skill newSkill = this.skillRepository.save(currentSkill);
        return this.skillMapper.toResUpdateDTO(newSkill);
    }

    public Skill handleGetSkillById(UUID id) throws IdInvalidException {
        return this.skillRepository.findById(id)
                .orElseThrow(() -> new IdInvalidException("Không tìm thấy mã kỹ năng!"));
    }

    public ResultPaginationDTO handleGetAllSkills(Specification<Skill> spec, Pageable pageable, String name) {
        Specification<Skill> combinedSpec = spec;
        if (hasText(name)) {
            Specification<Skill> nameSpec = (root, query, cb) ->
                    cb.or(
                            cb.like(cb.lower(root.get("nameVi")), "%" + name.trim().toLowerCase() + "%"),
                            cb.like(cb.lower(root.get("nameEn")), "%" + name.trim().toLowerCase() + "%")
                    );
            combinedSpec = (combinedSpec == null) ? nameSpec : combinedSpec.and(nameSpec);
        }

        Page<Skill> skillPage = this.skillRepository.findAll(combinedSpec, pageable);

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

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }

    public void handleDeleteSkill(UUID id) {
        Optional<Skill> skillOptional = this.skillRepository.findById(id);
        if (skillOptional.isPresent()) {
            Skill currentSkill = skillOptional.get();
            if (currentSkill.getJobs() != null) {
                currentSkill.getJobs().forEach(job -> job.getSkills().remove(currentSkill));
            }
            this.skillRepository.delete(currentSkill);
        }
    }
}
