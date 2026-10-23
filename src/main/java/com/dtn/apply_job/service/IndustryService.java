package com.dtn.apply_job.service;

import com.dtn.apply_job.common.response.ResultPaginationDTO;
import com.dtn.apply_job.domain.Industry;
import com.dtn.apply_job.exception.IdInvalidException;
import com.dtn.apply_job.exception.NameExistedException;
import com.dtn.apply_job.repository.IndustryRepository;
import com.dtn.apply_job.repository.SpecializationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class IndustryService {

    private final IndustryRepository industryRepository;
    private final SpecializationRepository specializationRepository;

    public Industry handleCreateIndustry(Industry industry) {
        if (industry.getNameVi() != null && this.industryRepository.existsByNameVi(industry.getNameVi())) {
            throw new NameExistedException("Tên ngành nghề tiếng Việt phải là duy nhất!");
        }
        if (industry.getNameEn() != null && this.industryRepository.existsByNameEn(industry.getNameEn())) {
            throw new NameExistedException("Tên ngành nghề tiếng Anh phải là duy nhất!");
        }
        if (industry.getNameVi() == null && industry.getNameEn() == null && industry.getName() != null) {
            industry.setNameVi(industry.getName());
            industry.setNameEn(industry.getName());
        }
        return this.industryRepository.save(industry);
    }

    public Industry handleUpdateIndustry(UUID id, Industry reqIndustry) throws IdInvalidException {
        Industry currentIndustry = this.industryRepository.findById(id)
                .orElseThrow(() -> new IdInvalidException("Industry id not found"));

        if (reqIndustry.getNameVi() != null) {
            boolean isNameChanged = !reqIndustry.getNameVi().equals(currentIndustry.getNameVi());
            if (isNameChanged && this.industryRepository.existsByNameVi(reqIndustry.getNameVi())) {
                throw new NameExistedException("Tên ngành tiếng Việt '" + reqIndustry.getNameVi() + "' đã tồn tại!");
            }
            currentIndustry.setNameVi(reqIndustry.getNameVi());
        }

        if (reqIndustry.getNameEn() != null) {
            boolean isNameChanged = !reqIndustry.getNameEn().equals(currentIndustry.getNameEn());
            if (isNameChanged && this.industryRepository.existsByNameEn(reqIndustry.getNameEn())) {
                throw new NameExistedException("Tên ngành tiếng Anh '" + reqIndustry.getNameEn() + "' đã tồn tại!");
            }
            currentIndustry.setNameEn(reqIndustry.getNameEn());
        }

        return this.industryRepository.save(currentIndustry);
    }

    public Industry handleGetIndustryById(UUID id) throws IdInvalidException {
        return this.industryRepository.findById(id)
                .orElseThrow(() -> new IdInvalidException("Không tìm thấy ngành này!"));
    }

    public ResultPaginationDTO handleGetAllIndustries(Specification<Industry> spec, Pageable pageable, String name) {
        Specification<Industry> nameSpec = null;
        if (name != null && !name.trim().isEmpty()) {
            nameSpec = (root, query, cb) ->
                    cb.or(
                            cb.like(cb.lower(root.get("nameVi")), "%" + name.trim().toLowerCase() + "%"),
                            cb.like(cb.lower(root.get("nameEn")), "%" + name.trim().toLowerCase() + "%")
                    );
        }

        Specification<Industry> finalSpec = spec;
        if (nameSpec != null) {
            finalSpec = (finalSpec == null) ? nameSpec : finalSpec.and(nameSpec);
        }

        Page<Industry> industryPage = this.industryRepository.findAll(finalSpec, pageable);

        ResultPaginationDTO resultPaginationDTO = new ResultPaginationDTO();
        ResultPaginationDTO.Meta meta = new ResultPaginationDTO.Meta();
        meta.setPage(industryPage.getNumber() + 1);
        meta.setPageSize(industryPage.getSize());
        meta.setPages(industryPage.getTotalPages());
        meta.setTotal(industryPage.getTotalElements());

        resultPaginationDTO.setMeta(meta);
        resultPaginationDTO.setResult(industryPage.getContent());

        return resultPaginationDTO;
    }

    public void handleDeleteIndustry(UUID id) throws IdInvalidException {
        Industry currentIndustry = this.industryRepository.findById(id)
                .orElseThrow(() -> new IdInvalidException("Industry id not found!"));

        if (this.specializationRepository.existsByIndustryId(id)) {
            throw new NameExistedException("Không thể xóa ngành nghề vì vẫn còn các chuyên ngành trực thuộc");
        }

        this.industryRepository.delete(currentIndustry);
    }
}
