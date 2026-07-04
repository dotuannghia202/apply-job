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

@Service
@RequiredArgsConstructor
public class IndustryService {

    private final IndustryRepository industryRepository;
    private final SpecializationRepository specializationRepository;

    public Industry handleCreateIndustry(Industry industry) {
        if (industry.getName() != null && this.industryRepository.existsByName(industry.getName())) {
            throw new NameExistedException("Tên ngành nghề phải là duy nhất!");
        }
        return this.industryRepository.save(industry);
    }

    public Industry handleUpdateIndustry(long id, Industry reqIndustry) throws IdInvalidException {
        Industry currentIndustry = this.industryRepository.findById(id)
                .orElseThrow(() -> new IdInvalidException("Industry id not found"));

        if (reqIndustry.getName() != null) {
            boolean isNameChanged = !reqIndustry.getName().equals(currentIndustry.getName());
            if (isNameChanged && this.industryRepository.existsByName(reqIndustry.getName())) {
                throw new NameExistedException(reqIndustry.getName() + " đã tồn tại!");
            }
            currentIndustry.setName(reqIndustry.getName());
        }

        return this.industryRepository.save(currentIndustry);
    }

    public Industry handleGetIndustryById(long id) throws IdInvalidException {
        return this.industryRepository.findById(id)
                .orElseThrow(() -> new IdInvalidException("Không tìm thấy ngành này!"));
    }

    public ResultPaginationDTO handleGetAllIndustries(Specification<Industry> spec, Pageable pageable, String name) {
        Specification<Industry> nameSpec = null;
        if (name != null && !name.trim().isEmpty()) {
            nameSpec = (root, query, cb) ->
                    cb.like(cb.lower(root.get("name")), "%" + name.trim().toLowerCase() + "%");
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

    public void handleDeleteIndustry(long id) throws IdInvalidException {
        Industry currentIndustry = this.industryRepository.findById(id)
                .orElseThrow(() -> new IdInvalidException("Industry id not found!"));

        if (this.specializationRepository.existsByIndustryId(id)) {
            throw new NameExistedException("Không thể xóa ngành nghề vì vẫn còn các chuyên ngành trực thuộc");
        }

        this.industryRepository.delete(currentIndustry);
    }
}

