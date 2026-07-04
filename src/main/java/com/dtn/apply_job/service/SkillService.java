package com.dtn.apply_job.service;

import com.dtn.apply_job.common.response.ResultPaginationDTO;
import com.dtn.apply_job.domain.Skill;
import com.dtn.apply_job.domain.response.skill.ResUpdateDTO;
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
        if (skill.getName() != null && this.skillRepository.existsByName(skill.getName())) {
            throw new NameExistedException("Tên kỹ năng phải là duy nhất!");
        }
        return this.skillRepository.save(skill);
    }

    public ResUpdateDTO handleUpdateSkill(long id, Skill skill) throws IdInvalidException, NameExistedException {
        Optional<Skill> skillOptional = this.skillRepository.findById(id);
        if (!skillOptional.isPresent()) {
            throw new IdInvalidException("Không tìm thấy mã kỹ năng");
        }
        if (skill.getName() != null && !this.skillRepository.existsByName(skill.getName())) {
            Skill currentSkill = skillOptional.get();
            currentSkill.setName(skill.getName());
            currentSkill.setUpdatedAt(currentSkill.getUpdatedAt());

            Skill newSkill = this.skillRepository.save(currentSkill);

            ResUpdateDTO resUpdateDTO = new ResUpdateDTO();
            resUpdateDTO.setName(newSkill.getName().toString());
            resUpdateDTO.setUpdatedAt(newSkill.getUpdatedAt());
            resUpdateDTO.setUpdatedBy(newSkill.getUpdatedBy());
            return resUpdateDTO;
        } else {
            throw new NameExistedException(skill.getName().toString() + " đã tồn tại!");
        }
    }

    public Skill handleGetSkillById(long id) throws IdInvalidException {
        if (!this.skillRepository.existsById(id)) {
            throw new IdInvalidException("Không tìm thấy mã kỹ năng!");
        }
        return this.skillRepository.findById(id).get();
    }

    public ResultPaginationDTO handleGetAllSkills(Specification<Skill> spec, Pageable pageable, String name) {

        Specification<Skill> combinedSpec = spec;
        if (hasText(name)) {
            Specification<Skill> nameSpec = (root, query, cb) ->
                    cb.like(cb.lower(root.get("name")), "%" + name.trim().toLowerCase() + "%");
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

    public void handleDeleteSkill(Long id) {
        Optional<Skill> skillOptional = this.skillRepository.findById(id);
        Skill currentSkill = skillOptional.get();
        currentSkill.getJobs().forEach(job -> job.getSkills().remove(currentSkill));

        this.skillRepository.delete(currentSkill);
    }

}
