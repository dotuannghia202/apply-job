package com.dtn.apply_job.service;

import com.dtn.apply_job.common.response.ResultPaginationDTO;
import com.dtn.apply_job.common.validator.DateRangeValidator;
import com.dtn.apply_job.domain.*;
import com.dtn.apply_job.domain.request.job.ReqCreateJobDTO;
import com.dtn.apply_job.domain.request.job.ReqUpdateJobDTO;
import com.dtn.apply_job.domain.response.job.ResJobDTO;
import com.dtn.apply_job.domain.response.job.ResUpdateJobDTO;
import com.dtn.apply_job.exception.IdInvalidException;
import com.dtn.apply_job.exception.InvalidDateRangeException;
import com.dtn.apply_job.repository.*;
import com.dtn.apply_job.security.SecurityUtil;
import com.dtn.apply_job.util.constant.enums.LevelEnum;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class JobService {
    private final JobRepository jobRepository;
    private final CompanyRepository companyRepository;
    private final SpecializationRepository specializationRepository;
    private final SkillRepository skillRepository;
    private final UserRepository userRepository;

    public JobService(JobRepository jobRepository, CompanyRepository companyRepository,
                      SpecializationRepository specializationRepository, SkillRepository skillRepository, UserRepository userRepository) {
        this.jobRepository = jobRepository;
        this.companyRepository = companyRepository;
        this.specializationRepository = specializationRepository;
        this.skillRepository = skillRepository;
        this.userRepository = userRepository;
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
        job.setBenefits(reqDTO.getBenefits());
        job.setWorkingHours(reqDTO.getWorkingHours());


        // Gắn quan hệ
        job.setCompany(company);
        job.setSpecialization(spec);
        if (skills != null) {
            job.setSkills(skills); // Ép sang HashSet nếu Entity bạn khai báo là Set
        }

        // Lưu vào DB
        Job savedJob = jobRepository.save(job);

        // 5. Trả về Response DTO

        return convertToResJobDTO(savedJob, Collections.emptySet());
    }

    public ResultPaginationDTO handleGetAllJobs(Specification<Job> spec, Pageable pageable) {
        Page<Job> pageJob = jobRepository.findAll(spec, pageable);
        Set<Long> savedJobIds = getSavedJobIdsForCurrentUser();
        List<ResJobDTO> listJobDTO = pageJob.getContent().stream()
                .map(job -> convertToResJobDTO(job, savedJobIds))
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

    public ResultPaginationDTO handleGetAllJobsWithFilters(
            Specification<Job> spec,
            Pageable pageable,
            String location,
            List<String> levels,
            Long specializationId,
            String companyName,
            Double maxSalary,
            String name,
            String keyword,
            String skill,
            Boolean active
    ) throws IdInvalidException {
        Set<LevelEnum> levelEnums = parseLevelEnums(levels);

        Specification<Job> filterSpec = buildJobFilterSpec(
                location, levelEnums, specializationId, companyName,
                maxSalary, name, keyword, skill, active
        );
        Specification<Job> combinedSpec = spec == null ? filterSpec : spec.and(filterSpec);

        Pageable effectivePageable = pageable;
        if (maxSalary != null && pageable.getSort().isUnsorted()) {
            effectivePageable = PageRequest.of(
                    pageable.getPageNumber(),
                    pageable.getPageSize(),
                    Sort.by(Sort.Direction.DESC, "salary")
            );
        }

        return handleGetAllJobs(combinedSpec, effectivePageable);
    }


    public ResJobDTO handleGetJobById(long id) throws IdInvalidException {
        Job job = jobRepository.findById(id)
                .orElseThrow(() -> new IdInvalidException("Job doesn't exist!"));
        Set<Long> savedJobIds = getSavedJobIdsForCurrentUser();
        return convertToResJobDTO(job, savedJobIds);
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
        currentJob.setBenefits(reqDTO.getBenefits());
        currentJob.setWorkingHours(reqDTO.getWorkingHours());

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

    public List<ResJobDTO> handleCreateJobs(List<ReqCreateJobDTO> reqDTOs) throws IdInvalidException, InvalidDateRangeException {
        if (reqDTOs == null || reqDTOs.isEmpty()) {
            throw new IdInvalidException("Job list must not be empty");
        }

        List<ResJobDTO> results = new ArrayList<>(reqDTOs.size());
        for (int i = 0; i < reqDTOs.size(); i++) {
            try {
                results.add(handleCreateJob(reqDTOs.get(i)));
            } catch (IdInvalidException | InvalidDateRangeException ex) {
                throw new IdInvalidException("Job at index " + i + " is invalid: " + ex.getMessage());
            }
        }

        return results;
    }

    // Hàm Converter Dùng Chung (Giúp code cực kỳ Clean)
    private ResJobDTO convertToResJobDTO(Job job, Set<Long> savedJobIds) {
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
        dto.setBenefits(job.getBenefits());
        dto.setWorkingHours(job.getWorkingHours());

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

        if (savedJobIds != null && savedJobIds.contains(job.getId())) {
            dto.setIsSaved(true);
        } else {
            dto.setIsSaved(false);
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
        dto.setBenefits(job.getBenefits());
        dto.setWorkingHours(job.getWorkingHours());

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

    private Specification<Job> buildJobFilterSpec(
            String location,
            Set<LevelEnum> levels,
            Long specializationId,
            String companyName,
            Double maxSalary,
            String name,
            String keyword,
            String skill,
            Boolean active
    ) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            boolean needsDistinct = false;

            if (hasText(location)) {
                predicates.add(cb.like(cb.lower(root.get("location")), "%" + location.trim().toLowerCase() + "%"));
            }
            if (hasText(name)) {
                predicates.add(cb.like(cb.lower(root.get("name")), "%" + name.trim().toLowerCase() + "%"));
            }
            if (hasText(companyName)) {
                predicates.add(cb.like(
                        cb.lower(root.get("company").get("name")),
                        "%" + companyName.trim().toLowerCase() + "%"
                ));
            }
            // Ô search chung: khớp tên job HOẶC tên công ty
            if (hasText(keyword)) {
                String pattern = "%" + keyword.trim().toLowerCase() + "%";
                Predicate byJobName = cb.like(cb.lower(root.get("name")), pattern);
                Predicate byCompanyName = cb.like(cb.lower(root.get("company").get("name")), pattern);
                predicates.add(cb.or(byJobName, byCompanyName));
            }
            if (specializationId != null && specializationId > 0) {
                predicates.add(cb.equal(root.get("specialization").get("id"), specializationId));
            }
            if (maxSalary != null && maxSalary > 0) {
                predicates.add(cb.lessThanOrEqualTo(root.get("salary"), maxSalary));
            }
            if (active != null) {
                predicates.add(cb.equal(root.get("active"), active));
            }
            if (levels != null && !levels.isEmpty()) {
                Join<Job, LevelEnum> levelJoin = root.join("levels", JoinType.LEFT);
                predicates.add(levelJoin.in(levels));
                needsDistinct = true;
            }
            if (hasText(skill)) {
                Join<Job, Skill> skillJoin = root.join("skills", JoinType.LEFT);
                predicates.add(cb.like(cb.lower(skillJoin.get("name")), "%" + skill.trim().toLowerCase() + "%"));
                needsDistinct = true;
            }

            if (needsDistinct) {
                query.distinct(true);
            }

            return predicates.isEmpty() ? cb.conjunction() : cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    private Set<LevelEnum> parseLevelEnums(List<String> levels) throws IdInvalidException {
        if (levels == null || levels.isEmpty()) {
            return new HashSet<>();
        }

        Set<LevelEnum> result = new HashSet<>();
        for (String rawLevel : levels) {
            if (!hasText(rawLevel)) {
                continue;
            }
            try {
                result.add(LevelEnum.valueOf(rawLevel.trim().toUpperCase()));
            } catch (IllegalArgumentException ex) {
                throw new IdInvalidException("Invalid level: " + rawLevel);
            }
        }

        return result;
    }

    @Transactional // Bắt buộc có khi xử lý Lazy Collection
    public boolean toggleSavedJob(Long jobId) throws Exception {
        // 1. Lấy user đang đăng nhập
        String email = SecurityUtil.getCurrentUser()
                .orElseThrow(() -> new IdInvalidException("Login please!"));
        User candidate = userRepository.findByEmail(email);
        if (candidate == null) {
            throw new IdInvalidException("User not found!");
        }
        // 2. Tìm Job
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new IdInvalidException("Job doesn't exist!"));

        // 3. Logic Toggle siêu sạch (Không cần query CSDL lần 2)
        boolean isAlreadySaved = candidate.getSavedJobs().contains(job);

        if (isAlreadySaved) {
            // Nếu đã có trong danh sách -> Xóa đi (Bỏ lưu)
            candidate.getSavedJobs().remove(job);
        } else {
            // Nếu chưa có -> Thêm vào danh sách (Lưu)
            candidate.getSavedJobs().add(job);
        }

        // 4. Lưu User lại (Hibernate sẽ tự động Insert/Delete ở bảng trung gian saved_jobs)
        userRepository.save(candidate);

        // Trả về trạng thái MỚI (True = Đã lưu, False = Đã bỏ lưu)
        return !isAlreadySaved;
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }

    private Set<Long> getSavedJobIdsForCurrentUser() {
        Set<Long> savedJobIds = new java.util.HashSet<>();

        try {
            // Lấy email người dùng đang đăng nhập
            String email = SecurityUtil.getCurrentUser().orElse("");

            if (!email.isBlank()) {
                User userOpt = userRepository.findByEmail(email);
                if (userOpt != null) {
                    // Trích xuất mảng ID từ danh sách SavedJobs của User
                    savedJobIds = userOpt.getSavedJobs().stream()
                            .map(Job::getId)
                            .collect(Collectors.toSet());
                }
            }
        } catch (Exception e) {
            // Bỏ qua nếu là khách vãng lai (Guest) chưa đăng nhập
        }

        return savedJobIds;
    }
}
