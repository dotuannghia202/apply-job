package com.dtn.apply_job.service;

import com.dtn.apply_job.domain.Industry;
import com.dtn.apply_job.domain.Specialization;
import com.dtn.apply_job.domain.request.specialization.ReqCreateSpecializationDTO;
import com.dtn.apply_job.domain.response.specialization.ResSpecializationDTO;
import com.dtn.apply_job.domain.response.user.ResultPaginationDTO;
import com.dtn.apply_job.exception.IdInvalidException;
import com.dtn.apply_job.exception.NameExistedException;
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

@Slf4j
@Service
@RequiredArgsConstructor
public class SpecializationService {

    private final SpecializationRepository specializationRepository;
    private final IndustryRepository industryRepository;
    private final JobRepository jobRepository;
    private final ResumeRepository resumeRepository;

    public ResSpecializationDTO handleCreateSpecialization(ReqCreateSpecializationDTO payload) throws IdInvalidException {
        String reqName = payload.getName() != null ? payload.getName().trim() : null;
        Long industryId = payload.getIndustryId();

        if (industryId <= 0) {
            throw new IdInvalidException("Industry id is required for specialization");
        }

        Industry currentIndustry = this.industryRepository.findById(industryId)
                .orElseThrow(() -> new IdInvalidException("Industry id not found"));

        if (reqName != null && this.specializationRepository.existsByNameAndIndustryId(reqName, currentIndustry.getId())) {
            throw new NameExistedException("Specialization name already exists in this industry!");
        }

        Specialization specialization = new Specialization();
        specialization.setName(reqName);

        specialization.setIndustry(currentIndustry);
        this.specializationRepository.save(specialization);

        ResSpecializationDTO resSpecializationDTO = new ResSpecializationDTO();
        resSpecializationDTO.setId(specialization.getId());
        resSpecializationDTO.setName(specialization.getName());
        resSpecializationDTO.setCreatedAt(specialization.getCreatedAt());
        resSpecializationDTO.setCreatedBy(specialization.getCreatedBy());

        ResSpecializationDTO.IndustryInfo industryInfo = new ResSpecializationDTO.IndustryInfo();
        industryInfo.setId(currentIndustry.getId());
        industryInfo.setName(currentIndustry.getName());
        resSpecializationDTO.setIndustry(industryInfo);

        return resSpecializationDTO;
    }

    public Specialization handleUpdateSpecialization(long id, Specialization reqSpecialization) throws IdInvalidException {
        Specialization currentSpecialization = this.specializationRepository.findById(id)
                .orElseThrow(() -> new IdInvalidException("Specialization id not found"));

        String targetName = reqSpecialization.getName() != null
                ? reqSpecialization.getName().trim()
                : currentSpecialization.getName();
        Industry targetIndustry = currentSpecialization.getIndustry();

        if (reqSpecialization.getIndustry() != null && reqSpecialization.getIndustry().getId() > 0) {
            targetIndustry = this.industryRepository.findById(reqSpecialization.getIndustry().getId())
                    .orElseThrow(() -> new IdInvalidException("Industry id not found"));
        }

        boolean isNameChanged = !targetName.equals(currentSpecialization.getName());
        boolean isIndustryChanged = targetIndustry.getId() != currentSpecialization.getIndustry().getId();

        if ((isNameChanged || isIndustryChanged)
                && this.specializationRepository.existsByNameAndIndustryId(targetName, targetIndustry.getId())) {
            throw new NameExistedException("Specialization name already exists in this industry!");
        }

        currentSpecialization.setName(targetName);
        currentSpecialization.setIndustry(targetIndustry);

        return this.specializationRepository.save(currentSpecialization);
    }

    public ResSpecializationDTO handleGetSpecializationById(long id) throws IdInvalidException {
        Specialization specicalization = this.specializationRepository.findById(id)
                .orElseThrow(() -> new IdInvalidException("Specialization id not found!"));
        ResSpecializationDTO result = new ResSpecializationDTO();
        result.setId(specicalization.getId());
        result.setName(specicalization.getName());
        result.setCreatedAt(specicalization.getCreatedAt());
        result.setCreatedBy(specicalization.getCreatedBy());
        if (specicalization.getIndustry() != null) {
            ResSpecializationDTO.IndustryInfo industryInfo = new ResSpecializationDTO.IndustryInfo();
            industryInfo.setId(specicalization.getIndustry().getId());
            industryInfo.setName(specicalization.getIndustry().getName());
            result.setIndustry(industryInfo);
        }
        return result;
    }

    public ResultPaginationDTO handleGetAllSpecializations(Specification<Specialization> spec, Pageable pageable) {
        Page<Specialization> specializationPage = this.specializationRepository.findAll(spec, pageable);
        List<ResSpecializationDTO> specializationDTOs = specializationPage.getContent()
                .stream()
                .map(this::convertToResSpecializationDTO)
                .toList();

        // ================= ĐOẠN CODE ĐỂ LOG =================
        log.info("Kiểm tra kiểu trả về: {}", specializationPage);
        log.info(">>> KIỂM TRA DỮ LIỆU TRANG: ");
        log.info("- Tổng số bản ghi (Total Elements): {}", specializationPage.getTotalElements());
        log.info("- Tổng số trang (Total Pages): {}", specializationPage.getTotalPages());
        log.info("- Số bản ghi trên 1 trang (Size): {}", specializationPage.getSize());
        log.info("- Nội dung Data (Content): {}", specializationDTOs);
        // ====================================================
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

    private ResSpecializationDTO convertToResSpecializationDTO(Specialization specialization) {
        ResSpecializationDTO dto = new ResSpecializationDTO();
        dto.setId(specialization.getId());
        dto.setName(specialization.getName());
        dto.setCreatedAt(specialization.getCreatedAt());
        dto.setCreatedBy(specialization.getCreatedBy());

        if (specialization.getIndustry() != null) {
            ResSpecializationDTO.IndustryInfo industryInfo = new ResSpecializationDTO.IndustryInfo();
            industryInfo.setId(specialization.getIndustry().getId());
            industryInfo.setName(specialization.getIndustry().getName());
            dto.setIndustry(industryInfo);
        }

        return dto;
    }

    public void handleDeleteSpecialization(long id) throws IdInvalidException {
        Specialization currentSpecialization = this.specializationRepository.findById(id)
                .orElseThrow(() -> new IdInvalidException("Specialization id not found!"));

        if (this.jobRepository.existsBySpecializationId(id) || this.resumeRepository.existsBySpecializationId(id)) {
            throw new NameExistedException("Cannot delete specialization because it is being used by jobs or resumes");
        }

        this.specializationRepository.delete(currentSpecialization);
    }
}

