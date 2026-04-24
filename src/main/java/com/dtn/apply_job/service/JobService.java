package com.dtn.apply_job.service;

import com.dtn.apply_job.common.validator.DateRangeValidator;
import com.dtn.apply_job.domain.Company;
import com.dtn.apply_job.domain.Job;
import com.dtn.apply_job.domain.Skill;
import com.dtn.apply_job.domain.Specialization;
import com.dtn.apply_job.domain.request.job.ReqCreateJobDTO;
import com.dtn.apply_job.domain.request.job.ReqUpdateJobDTO;
import com.dtn.apply_job.domain.response.job.ResJobDTO;
import com.dtn.apply_job.domain.response.job.ResUpdateJobDTO;
import com.dtn.apply_job.domain.response.user.ResultPaginationDTO;
import com.dtn.apply_job.exception.IdInvalidException;
import com.dtn.apply_job.exception.InvalidDateRangeException;
import com.dtn.apply_job.repository.CompanyRepository;
import com.dtn.apply_job.repository.JobRepository;
import com.dtn.apply_job.repository.SkillRepository;
import com.dtn.apply_job.repository.SpecializationRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class JobService {
    private final JobRepository jobRepository;
    private final CompanyRepository companyRepository;
    private final SpecializationRepository specializationRepository;
    private final SkillRepository skillRepository;

    public JobService(JobRepository jobRepository, CompanyRepository companyRepository,
                      SpecializationRepository specializationRepository, SkillRepository skillRepository) {
        this.jobRepository = jobRepository;
        this.companyRepository = companyRepository;
        this.specializationRepository = specializationRepository;
        this.skillRepository = skillRepository;
    }

    public ResJobDTO handleCreateJob(ReqCreateJobDTO reqDTO) throws IdInvalidException, InvalidDateRangeException {
        // 1. KIỂM TRA SỰ TỒN TẠI CỦA COMPANY
        Company company = companyRepository.findById(reqDTO.getCompanyId())
                .orElseThrow(() -> new IdInvalidException("Company doesn't exist!"));

        // 2. KIỂM TRA SỰ TỒN TẠI CỦA SPECIALIZATION
        Specialization spec = specializationRepository.findById(reqDTO.getSpecializationId())
                .orElseThrow(() -> new IdInvalidException("Specialization doesn't exist!"));

        // 3. KIỂM TRA CÁC SKILLS CÓ HỢP LỆ KHÔNG
        List<Skill> skills = null;
        if (reqDTO.getSkillIds() != null && !reqDTO.getSkillIds().isEmpty()) {
            skills = skillRepository.findAllById(reqDTO.getSkillIds());
            if (skills.size() != reqDTO.getSkillIds().size()) {
                throw new IdInvalidException("There are skills that don't exist in the system!");
            }
        }

        DateRangeValidator.validate(reqDTO.getStartDate(), reqDTO.getEndDate());

        // 4. Chuyển đổi DTO -> Entity
        Job job = new Job();
        job.setName(reqDTO.getName());
        job.setLocation(reqDTO.getLocation());
        job.setSalary(reqDTO.getSalary());
        job.setQuantity(reqDTO.getQuantity());
        job.setLevels(reqDTO.getLevels());
        job.setDescription(reqDTO.getDescription());
        job.setStartDate(reqDTO.getStartDate());
        job.setEndDate(reqDTO.getEndDate());


        // Gắn quan hệ
        job.setCompany(company);
        job.setSpecialization(spec);
        if (skills != null) {
            job.setSkills(skills); // Ép sang HashSet nếu Entity bạn khai báo là Set
        }

        // Lưu vào DB
        Job savedJob = jobRepository.save(job);

        // 5. Trả về Response DTO
        return convertToResJobDTO(savedJob);
    }

    public ResultPaginationDTO handleGetAllJobs(Specification<Job> spec, Pageable pageable) {
        Page<Job> pageJob = jobRepository.findAll(spec, pageable);

        List<ResJobDTO> listJobDTO = pageJob.getContent().stream()
                .map(this::convertToResJobDTO)
                .collect(Collectors.toList());

        ResultPaginationDTO rs = new ResultPaginationDTO();
        ResultPaginationDTO.Meta meta = new ResultPaginationDTO.Meta();
        meta.setPage(pageJob.getNumber() + 1);
        meta.setPageSize(pageJob.getSize());
        meta.setPages(pageJob.getTotalPages());
        meta.setTotal(pageJob.getTotalElements());

        rs.setMeta(meta);
        rs.setResult(listJobDTO);

        return rs;
    }

    public ResJobDTO handleGetJobById(long id) throws IdInvalidException {
        Job job = jobRepository.findById(id)
                .orElseThrow(() -> new IdInvalidException("Job doesn't exist!"));
        return convertToResJobDTO(job);
    }

    public ResUpdateJobDTO handleUpdateJob(long id, ReqUpdateJobDTO reqDTO) throws IdInvalidException {
        // 1. Tìm Job hiện tại trong DB
        Job currentJob = jobRepository.findById(id)
                .orElseThrow(() -> new IdInvalidException("Job doesn't exist!"));

        // 2. GHI ĐÈ TRỰC TIẾP CÁC TRƯỜNG THÔNG THƯỜNG (Theo chuẩn PUT)
        DateRangeValidator.validate(reqDTO.getStartDate(), reqDTO.getEndDate());
        currentJob.setName(reqDTO.getName());
        currentJob.setLocation(reqDTO.getLocation());
        currentJob.setSalary(reqDTO.getSalary());
        currentJob.setQuantity(reqDTO.getQuantity());
        currentJob.setLevels(reqDTO.getLevels()); // Sửa thành level (số ít)
        currentJob.setDescription(reqDTO.getDescription());

        currentJob.setStartDate(reqDTO.getStartDate());
        currentJob.setEndDate(reqDTO.getEndDate());
        currentJob.setActive(reqDTO.getIsActive());

        // 3. KIỂM TRA VÀ CẬP NHẬT COMPANY
        // Dùng == giúp so sánh ID nhanh chóng, nếu ID không đổi thì không cần chọc xuống DB tìm lại
        if (currentJob.getCompany().getId() != reqDTO.getCompanyId()) {
            Company company = companyRepository.findById(reqDTO.getCompanyId())
                    .orElseThrow(() -> new IdInvalidException("Company doesn't exist!"));
            currentJob.setCompany(company);
        }

        // 4. KIỂM TRA VÀ CẬP NHẬT SPECIALIZATION
        if (currentJob.getSpecialization().getId() != reqDTO.getSpecializationId()) {
            Specialization specialization = specializationRepository.findById(reqDTO.getSpecializationId())
                    .orElseThrow(() -> new IdInvalidException("Specialization doesn't exist!"));
            currentJob.setSpecialization(specialization);
        }

        // 5. CẬP NHẬT DANH SÁCH SKILL
        if (reqDTO.getSkillIds() != null) {
            List<Skill> skills = skillRepository.findAllById(reqDTO.getSkillIds());
            if (skills.size() != reqDTO.getSkillIds().size()) {
                throw new IdInvalidException("There are skills that don't exist in the system!");
            }
            currentJob.setSkills(skills);
        } else {
            // Nếu Frontend gửi mảng rỗng hoặc null, nghĩa là muốn xóa hết skill của Job này
            currentJob.getSkills().clear();
        }

        // 6. LƯU VÀO DB
        Job updatedJob = jobRepository.save(currentJob);

        return convertToResUpdateJobDTO(updatedJob);
    }

    public void handleDeleteJob(long id) throws IdInvalidException {
        Job currentJob = jobRepository.findById(id)
                .orElseThrow(() -> new IdInvalidException("Job doesn't exist!"));
        jobRepository.delete(currentJob);
    }

    // Hàm Converter Dùng Chung (Giúp code cực kỳ Clean)
    private ResJobDTO convertToResJobDTO(Job job) {
        ResJobDTO dto = new ResJobDTO();
        dto.setId(job.getId());
        dto.setName(job.getName());
        dto.setLocation(job.getLocation());
        dto.setSalary(job.getSalary());
        dto.setQuantity(job.getQuantity());
        dto.setLevels(job.getLevels());
        dto.setDescription(job.getDescription());
        dto.setStartDate(job.getStartDate());
        dto.setEndDate(job.getEndDate());
        dto.setActive(job.getActive());
        dto.setCreatedAt(job.getCreatedAt());
        dto.setCreatedBy(job.getCreatedBy());

        // Lấy danh sách kỹ năng dạng String
        if (job.getSkills() != null) {
            List<String> skillNames = job.getSkills().stream()
                    .map(Skill::getName)
                    .collect(Collectors.toList());
            dto.setSkills(skillNames);
        }

        // Lấy thông tin Company rút gọn
        if (job.getCompany() != null) {
            ResJobDTO.CompanyInfo comInfo = new ResJobDTO.CompanyInfo();
            comInfo.setId(job.getCompany().getId());
            comInfo.setName(job.getCompany().getName());
            comInfo.setLogo(job.getCompany().getLogo());
            dto.setCompany(comInfo);
        }

        // Lấy thông tin Specialization rút gọn
        if (job.getSpecialization() != null) {
            ResJobDTO.SpecializationInfo specInfo = new ResJobDTO.SpecializationInfo();
            specInfo.setId(job.getSpecialization().getId());
            specInfo.setName(job.getSpecialization().getName());
            dto.setSpecialization(specInfo);
        }

        return dto;
    }

    private ResUpdateJobDTO convertToResUpdateJobDTO(Job job) {
        ResUpdateJobDTO dto = new ResUpdateJobDTO();
        dto.setId(job.getId());
        dto.setName(job.getName());
        dto.setLocation(job.getLocation());
        dto.setSalary(job.getSalary());
        dto.setQuantity(job.getQuantity());
        dto.setLevels(job.getLevels());
        dto.setDescription(job.getDescription());
        dto.setStartDate(job.getStartDate());
        dto.setEndDate(job.getEndDate());
        dto.setActive(Boolean.TRUE.equals(job.getActive()));
        dto.setUpdatedAt(job.getUpdatedAt());
        dto.setUpdatedBy(job.getUpdatedBy());

        if (job.getCompany() != null) {
            dto.setCompanyName(job.getCompany().getName());
        }
        if (job.getSpecialization() != null) {
            dto.setSpecializationName(job.getSpecialization().getName());
        }
        if (job.getSkills() != null) {
            dto.setSkills(job.getSkills().stream().map(Skill::getName).collect(Collectors.toList()));
        }

        return dto;
    }
}