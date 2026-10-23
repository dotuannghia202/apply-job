package com.dtn.apply_job.service;

import com.dtn.apply_job.common.response.ResultPaginationDTO;
import com.dtn.apply_job.domain.Industry;
import com.dtn.apply_job.domain.Specialization;
import com.dtn.apply_job.domain.request.specialization.ReqCreateSpecializationDTO;
import com.dtn.apply_job.domain.response.specialization.ResSpecializationByIndustryId;
import com.dtn.apply_job.domain.response.specialization.ResSpecializationDTO;
import com.dtn.apply_job.exception.IdInvalidException;
import com.dtn.apply_job.exception.NameExistedException;
import com.dtn.apply_job.mapper.SpecializationMapper;
import com.dtn.apply_job.repository.IndustryRepository;
import com.dtn.apply_job.repository.JobRepository;
import com.dtn.apply_job.repository.ResumeRepository;
import com.dtn.apply_job.repository.SpecializationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class SpecializationService {

    private final SpecializationRepository specializationRepository;
    private final IndustryRepository industryRepository;
    private final JobRepository jobRepository;
    private final ResumeRepository resumeRepository;
    private final SpecializationMapper specializationMapper;

    public ResSpecializationDTO handleCreateSpecialization(ReqCreateSpecializationDTO payload) throws IdInvalidException {
        String nameVi = payload.getNameVi() != null ? payload.getNameVi().trim() : null;
        String nameEn = payload.getNameEn() != null ? payload.getNameEn().trim() : null;
        if (nameVi == null && nameEn != null) nameVi = nameEn;
        if (nameEn == null && nameVi != null) nameEn = nameVi;

        UUID industryId = payload.getIndustryId();
        if (industryId == null) {
            throw new IdInvalidException("Mã ngành nghề không được để trống cho chuyên ngành");
        }

        Industry currentIndustry = this.industryRepository.findById(industryId)
                .orElseThrow(() -> new IdInvalidException("Không tìm thấy mã ngành nghề"));

        if (nameVi != null && this.specializationRepository.existsByNameViAndIndustryId(nameVi, currentIndustry.getId())) {
            throw new NameExistedException("Tên chuyên ngành tiếng Việt đã tồn tại trong ngành nghề này!");
        }
        if (nameEn != null && this.specializationRepository.existsByNameEnAndIndustryId(nameEn, currentIndustry.getId())) {
            throw new NameExistedException("Tên chuyên ngành tiếng Anh đã tồn tại trong ngành nghề này!");
        }

        Specialization specialization = specializationMapper.toSpecialization(payload);
        specialization.setNameVi(nameVi);
        specialization.setNameEn(nameEn);
        specialization.setIndustry(currentIndustry);
        this.specializationRepository.save(specialization);

        return specializationMapper.toResSpecializationDTO(specialization);
    }

    public Specialization handleUpdateSpecialization(UUID id, Specialization reqSpecialization) throws IdInvalidException {
        Specialization currentSpecialization = this.specializationRepository.findById(id)
                .orElseThrow(() -> new IdInvalidException("Specialization id not found"));

        Industry targetIndustry = currentSpecialization.getIndustry();
        if (reqSpecialization.getIndustry() != null && reqSpecialization.getIndustry().getId() != null) {
            targetIndustry = this.industryRepository.findById(reqSpecialization.getIndustry().getId())
                    .orElseThrow(() -> new IdInvalidException("Không tìm thấy mã ngành nghề"));
        }

        String targetNameVi = reqSpecialization.getNameVi() != null ? reqSpecialization.getNameVi().trim() : currentSpecialization.getNameVi();
        String targetNameEn = reqSpecialization.getNameEn() != null ? reqSpecialization.getNameEn().trim() : currentSpecialization.getNameEn();

        if (reqSpecialization.getNameVi() != null) {
            boolean isNameChanged = !targetNameVi.equals(currentSpecialization.getNameVi());
            if (isNameChanged && this.specializationRepository.existsByNameViAndIndustryId(targetNameVi, targetIndustry.getId())) {
                throw new NameExistedException("Tên chuyên ngành tiếng Việt đã tồn tại trong ngành nghề này!");
            }
            currentSpecialization.setNameVi(targetNameVi);
        }

        if (reqSpecialization.getNameEn() != null) {
            boolean isNameChanged = !targetNameEn.equals(currentSpecialization.getNameEn());
            if (isNameChanged && this.specializationRepository.existsByNameEnAndIndustryId(targetNameEn, targetIndustry.getId())) {
                throw new NameExistedException("Tên chuyên ngành tiếng Anh đã tồn tại trong ngành nghề này!");
            }
            currentSpecialization.setNameEn(targetNameEn);
        }

        currentSpecialization.setIndustry(targetIndustry);
        return this.specializationRepository.save(currentSpecialization);
    }

    public ResSpecializationDTO handleGetSpecializationById(UUID id) throws IdInvalidException {
        Specialization specialization = this.specializationRepository.findById(id)
                .orElseThrow(() -> new IdInvalidException("Specialization id not found!"));
        return specializationMapper.toResSpecializationDTO(specialization);
    }

    public ResultPaginationDTO handleGetAllSpecializations(Specification<Specialization> spec, Pageable pageable, String name) {
        Specification<Specialization> finalSpec = spec;
        if (hasText(name)) {
            Specification<Specialization> nameSpec = (root, query, cb) ->
                    cb.or(
                            cb.like(cb.lower(root.get("nameVi")), "%" + name.trim().toLowerCase() + "%"),
                            cb.like(cb.lower(root.get("nameEn")), "%" + name.trim().toLowerCase() + "%")
                    );
            finalSpec = finalSpec == null ? nameSpec : finalSpec.and(nameSpec);
        }

        Page<Specialization> specializationPage = this.specializationRepository.findAll(finalSpec, pageable);
        List<ResSpecializationDTO> specializationDTOs = specializationPage.getContent()
                .stream()
                .map(specializationMapper::toResSpecializationDTO)
                .toList();

        ResultPaginationDTO resultPaginationDTO = new ResultPaginationDTO();
        ResultPaginationDTO.Meta meta = new ResultPaginationDTO.Meta();
        meta.setPage(specializationPage.getNumber() + 1);
        meta.setPageSize(specializationPage.getSize());
        meta.setPages(specializationPage.getTotalPages());
        meta.setTotal(specializationPage.getTotalElements());

        resultPaginationDTO.setMeta(meta);
        resultPaginationDTO.setResult(specializationDTOs);

        return resultPaginationDTO;
    }

    public List<ResSpecializationByIndustryId> handleGetSpecializationsByIndustryId(UUID industryId) throws IdInvalidException {
        if (industryId == null) {
            throw new IdInvalidException("Mã ngành nghề không hợp lệ");
        }

        if (!this.industryRepository.existsById(industryId)) {
            throw new IdInvalidException("Không tìm thấy mã ngành nghề");
        }

        return this.specializationRepository.findAllByIndustryIdProjected(industryId);
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }

    public void handleDeleteSpecialization(UUID id) throws IdInvalidException {
        Specialization currentSpecialization = this.specializationRepository.findById(id)
                .orElseThrow(() -> new IdInvalidException("Specialization id not found!"));

        if (this.jobRepository.existsBySpecializationId(id) || this.resumeRepository.existsBySpecializationId(id)) {
            throw new NameExistedException("Không thể xóa chuyên ngành vì đang được sử dụng bởi các công việc hoặc CV");
        }

        this.specializationRepository.delete(currentSpecialization);
    }
}
