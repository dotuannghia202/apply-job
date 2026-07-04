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
import com.dtn.apply_job.util.constant.enums.CompanyStatus;
import com.dtn.apply_job.util.constant.enums.LevelEnum;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;
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
    private final ApplicationRepository applicationRepository;

    public JobService(JobRepository jobRepository, CompanyRepository companyRepository,
                      SpecializationRepository specializationRepository, SkillRepository skillRepository,
                      UserRepository userRepository, ApplicationRepository applicationRepository) {
        this.jobRepository = jobRepository;
        this.companyRepository = companyRepository;
        this.specializationRepository = specializationRepository;
        this.skillRepository = skillRepository;
        this.userRepository = userRepository;
        this.applicationRepository = applicationRepository;
    }

    public ResJobDTO handleCreateJob(ReqCreateJobDTO reqDTO) throws IdInvalidException, InvalidDateRangeException, AccessDeniedException {
        
        Company company = companyRepository.findById(reqDTO.getCompanyId())
                .orElseThrow(() -> new IdInvalidException("Company doesn't exist!"));

        if (!company.getStatus().equals(CompanyStatus.APPROVED)) {
            throw new AccessDeniedException("Hồ sơ Công ty của bạn chưa được Admin phê duyệt hoặc bị đình chỉ hoạt động. Vui lòng liên hệ ban quản trị!");
        }

        
        Specialization spec = specializationRepository.findById(reqDTO.getSpecializationId())
                .orElseThrow(() -> new IdInvalidException("Specialization doesn't exist!"));

        
        List<Skill> skills = null;
        if (reqDTO.getSkillIds() != null && !reqDTO.getSkillIds().isEmpty()) {
            skills = skillRepository.findAllById(reqDTO.getSkillIds());
            if (skills.size() != reqDTO.getSkillIds().size()) {
                throw new IdInvalidException("Có những kỹ năng không tồn tại trong hệ thống!");
            }
        }

        DateRangeValidator.validate(reqDTO.getStartDate(), reqDTO.getEndDate());

        
        Job job = new Job();
        job.setName(reqDTO.getName());
        job.setLocation(reqDTO.getLocation());
        job.setMinSalary(reqDTO.getMinSalary());
        job.setMaxSalary(reqDTO.getMaxSalary());
        job.setQuantity(reqDTO.getQuantity());
        job.setLevels(reqDTO.getLevels());
        job.setDescription(reqDTO.getDescription());
        job.setRequirements(reqDTO.getRequirements());
        job.setStartDate(reqDTO.getStartDate());
        job.setEndDate(reqDTO.getEndDate());
        job.setBenefits(reqDTO.getBenefits());
        job.setWorkingHours(reqDTO.getWorkingHours());


        
        job.setCompany(company);
        job.setSpecialization(spec);
        if (skills != null) {
            job.setSkills(skills); 
        }

        
        Job savedJob = jobRepository.save(job);

        

        return convertToResJobDTO(savedJob, Collections.emptySet(), Collections.emptySet());
    }

    public ResultPaginationDTO handleGetAllJobs(Specification<Job> spec, Pageable pageable) {
        Pageable effectivePageable = applyCreatedAtSort(pageable, null);
        Page<Job> pageJob = jobRepository.findAll(spec, effectivePageable);
        Set<Long> savedJobIds = getSavedJobIdsForCurrentUser();
        Set<Long> appliedJobIds = getAppliedJobIdsForCurrentUser();
        List<ResJobDTO> listJobDTO = pageJob.getContent().stream()
                .map(job -> convertToResJobDTO(job, savedJobIds, appliedJobIds))
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
            Double minSalary,
            Double maxSalary,
            String name,

            String skill,
            Boolean active,
            Integer sortCreatedAt
    ) throws IdInvalidException {
        Set<LevelEnum> levelEnums = parseLevelEnums(levels);

        validateSalaryFilter(minSalary, maxSalary);

        Specification<Job> filterSpec = buildJobFilterSpec(
                location, levelEnums, specializationId, companyName,
                minSalary, maxSalary, name, skill, active
        );
        Specification<Job> combinedSpec = spec == null ? filterSpec : spec.and(filterSpec);

        Pageable effectivePageable = applyCreatedAtSort(pageable, sortCreatedAt);
        if ((minSalary != null || maxSalary != null) && effectivePageable.getSort().isUnsorted()) {
            effectivePageable = PageRequest.of(
                    effectivePageable.getPageNumber(),
                    effectivePageable.getPageSize(),
                    Sort.by(Sort.Direction.DESC, "maxSalary")
            );
        }

        return handleGetAllJobs(combinedSpec, effectivePageable);
    }


    public ResJobDTO handleGetJobById(long id) throws IdInvalidException {
        Job job = jobRepository.findById(id)
                .orElseThrow(() -> new IdInvalidException("Job doesn't exist!"));
        Set<Long> savedJobIds = getSavedJobIdsForCurrentUser();
        Set<Long> appliedJobIds = getAppliedJobIdsForCurrentUser();
        return convertToResJobDTO(job, savedJobIds, appliedJobIds);
    }

    public ResUpdateJobDTO handleUpdateJob(long id, ReqUpdateJobDTO reqDTO) throws Exception {
        
        Job currentJob = jobRepository.findById(id)
                .orElseThrow(() -> new IdInvalidException("Job doesn't exist!"));

        checkJobOwnership(currentJob);

        
        DateRangeValidator.validate(reqDTO.getStartDate(), reqDTO.getEndDate());
        currentJob.setName(reqDTO.getName());
        currentJob.setLocation(reqDTO.getLocation());
        currentJob.setMinSalary(reqDTO.getMinSalary());
        currentJob.setMaxSalary(reqDTO.getMaxSalary());
        currentJob.setQuantity(reqDTO.getQuantity());
        currentJob.setLevels(reqDTO.getLevels()); 
        currentJob.setDescription(reqDTO.getDescription());
        currentJob.setRequirements(reqDTO.getRequirements());

        currentJob.setStartDate(reqDTO.getStartDate());
        currentJob.setEndDate(reqDTO.getEndDate());

        currentJob.setBenefits(reqDTO.getBenefits());
        currentJob.setWorkingHours(reqDTO.getWorkingHours());

        
        
        if (reqDTO.getCompanyId() != null && !Objects.equals(currentJob.getCompany().getId(), reqDTO.getCompanyId())) {
            Company company = companyRepository.findById(reqDTO.getCompanyId())
                    .orElseThrow(() -> new IdInvalidException("Company doesn't exist!"));
            currentJob.setCompany(company);
        }

        
        Long currentSpecializationId = currentJob.getSpecialization() != null
                ? currentJob.getSpecialization().getId()
                : null;
        if (reqDTO.getSpecializationId() != null && !Objects.equals(currentSpecializationId, reqDTO.getSpecializationId())) {
            Specialization specialization = specializationRepository.findById(reqDTO.getSpecializationId())
                    .orElseThrow(() -> new IdInvalidException("Specialization doesn't exist!"));
            currentJob.setSpecialization(specialization);
        }

        
        if (reqDTO.getSkillIds() != null) {
            List<Skill> skills = skillRepository.findAllById(reqDTO.getSkillIds());
            if (skills.size() != reqDTO.getSkillIds().size()) {
                throw new IdInvalidException("Có những kỹ năng không tồn tại trong hệ thống!");
            }
            currentJob.setSkills(skills);
        } else {
            
            currentJob.getSkills().clear();
        }

        
        Job updatedJob = jobRepository.save(currentJob);

        return convertToResUpdateJobDTO(updatedJob);
    }

    public void handleDeleteJob(long id) throws Exception {
        Job currentJob = jobRepository.findById(id)
                .orElseThrow(() -> new IdInvalidException("Job doesn't exist!"));
        checkJobOwnership(currentJob);
        currentJob.setActive(false);
        jobRepository.save(currentJob);
    }

    public List<ResJobDTO> handleCreateJobs(List<ReqCreateJobDTO> reqDTOs) throws IdInvalidException, InvalidDateRangeException {
        if (reqDTOs == null || reqDTOs.isEmpty()) {
            throw new IdInvalidException("Danh sách công việc không được để trống");
        }

        List<ResJobDTO> results = new ArrayList<>(reqDTOs.size());
        for (int i = 0; i < reqDTOs.size(); i++) {
            try {
                results.add(handleCreateJob(reqDTOs.get(i)));
            } catch (IdInvalidException | InvalidDateRangeException ex) {
                throw new IdInvalidException("Công việc tại chỉ mục " + i + " không hợp lệ: " + ex.getMessage());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        return results;
    }

    
    private ResJobDTO convertToResJobDTO(Job job, Set<Long> savedJobIds, Set<Long> appliedJobIds) {
        ResJobDTO dto = new ResJobDTO();
        dto.setId(job.getId());
        dto.setName(job.getName());
        dto.setLocation(job.getLocation());
        dto.setMinSalary(job.getMinSalary());
        dto.setMaxSalary(job.getMaxSalary());
        dto.setQuantity(job.getQuantity());
        dto.setLevels(job.getLevels());
        dto.setDescription(job.getDescription());
        dto.setRequirements(job.getRequirements());
        dto.setStartDate(job.getStartDate());
        dto.setEndDate(job.getEndDate());
        dto.setActive(job.getActive());
        dto.setCreatedAt(job.getCreatedAt());
        dto.setCreatedBy(job.getCreatedBy());
        dto.setBenefits(job.getBenefits());
        dto.setWorkingHours(job.getWorkingHours());

        
        if (job.getSkills() != null) {
            List<String> skillNames = job.getSkills().stream()
                    .map(Skill::getName)
                    .collect(Collectors.toList());
            dto.setSkills(skillNames);
        }

        
        if (job.getCompany() != null) {
            ResJobDTO.CompanyInfo comInfo = new ResJobDTO.CompanyInfo();
            comInfo.setId(job.getCompany().getId());
            comInfo.setName(job.getCompany().getName());
            comInfo.setLogo(job.getCompany().getLogo());
            dto.setCompany(comInfo);
        }

        
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

        
        dto.setIsApplied(appliedJobIds != null && appliedJobIds.contains(job.getId()));

        long applicants = applicationRepository.countByJob_Id(job.getId());
        dto.setApplicantCount(applicants);

        return dto;
    }

    private ResUpdateJobDTO convertToResUpdateJobDTO(Job job) {
        ResUpdateJobDTO dto = new ResUpdateJobDTO();
        dto.setId(job.getId());
        dto.setName(job.getName());
        dto.setLocation(job.getLocation());
        dto.setMinSalary(job.getMinSalary());
        dto.setMaxSalary(job.getMaxSalary());
        dto.setQuantity(job.getQuantity());
        dto.setLevels(job.getLevels());
        dto.setDescription(job.getDescription());
        dto.setRequirements(job.getRequirements());
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
            Double minSalary,
            Double maxSalary,
            String name,
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

            

            if (specializationId != null && specializationId > 0) {
                predicates.add(cb.equal(root.get("specialization").get("id"), specializationId));
            }

            if (minSalary != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("minSalary"), minSalary));
            }
            if (maxSalary != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("maxSalary"), maxSalary));
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
                throw new IdInvalidException("Cấp bậc không hợp lệ: " + rawLevel);
            }
        }

        return result;
    }

    @Transactional 
    public boolean toggleSavedJob(Long jobId) throws Exception {
        
        String email = SecurityUtil.getCurrentUser()
                .orElseThrow(() -> new IdInvalidException("Login please!"));
        User candidate = userRepository.findByEmail(email);
        if (candidate == null) {
            throw new IdInvalidException("Không tìm thấy người dùng!");
        }
        
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new IdInvalidException("Job doesn't exist!"));

        
        boolean isAlreadySaved = candidate.getSavedJobs().contains(job);

        if (isAlreadySaved) {
            
            candidate.getSavedJobs().remove(job);
        } else {
            
            candidate.getSavedJobs().add(job);
        }

        
        userRepository.save(candidate);

        
        return !isAlreadySaved;
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }

    private Set<Long> getSavedJobIdsForCurrentUser() {
        Set<Long> savedJobIds = new java.util.HashSet<>();

        try {
            
            String email = SecurityUtil.getCurrentUser().orElse("");

            if (!email.isBlank()) {
                User userOpt = userRepository.findByEmail(email);
                if (userOpt != null) {
                    
                    savedJobIds = userOpt.getSavedJobs().stream()
                            .map(Job::getId)
                            .collect(Collectors.toSet());
                }
            }
        } catch (Exception e) {
            
        }

        return savedJobIds;
    }

    public ResultPaginationDTO handleGetSavedJobs(Pageable pageable) throws IdInvalidException {
        
        String email = SecurityUtil.getCurrentUser()
                .orElseThrow(() -> new IdInvalidException("Login to continue!"));

        
        Page<Job> pageJob = jobRepository.findSavedJobsByUserEmail(email, pageable);

        
        
        
        Set<Long> appliedJobIds = getAppliedJobIdsForCurrentUser();
        List<ResJobDTO> listJobDTO = pageJob.getContent().stream()
                .map(job -> {
                    ResJobDTO dto = convertToResJobDTO(job, Collections.emptySet(), appliedJobIds);
                    dto.setIsSaved(true); 
                    return dto;
                })
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

    
    private java.util.Set<Long> getAppliedJobIdsForCurrentUser() {
        java.util.Set<Long> appliedJobIds = new java.util.HashSet<>();
        try {
            String email = SecurityUtil.getCurrentUser().orElse("");
            if (!email.isBlank()) {
                User user = userRepository.findByEmail(email);
                if (user != null) {
                    appliedJobIds = new HashSet<>(applicationRepository.findAppliedJobIdsByCandidate(user));
                }
            }
        } catch (Exception e) {
        }

        return appliedJobIds;
    }

    private void validateSalaryFilter(Double minSalary, Double maxSalary) throws InputMismatchException {
        if (minSalary != null && minSalary < 0) {
            throw new InputMismatchException("Lương tối thiểu phải lớn hơn hoặc bằng 0!");
        }

        if (maxSalary != null && maxSalary < 0) {
            throw new InputMismatchException("Lương tối đa phải lớn hơn hoặc bằng 0!");
        }

        if (minSalary != null && maxSalary != null && maxSalary < minSalary) {
            throw new InputMismatchException("Lương tối đa phải lớn hơn hoặc bằng lương tối thiểu!");
        }
    }

    

    private void checkJobOwnership(Job job) throws Exception {
        
        String email = SecurityUtil.getCurrentUser().orElseThrow(() -> new IdInvalidException("Login please!"));
        User currentUser = userRepository.findByEmail(email);

        
        boolean isAdmin = currentUser.getRoles().stream()
                .anyMatch(r -> r.getName().name().equals("ROLE_ADMIN") || r.getName().name().equals("ADMIN"));
        if (isAdmin) return;

        
        boolean isEmployer = currentUser.getRoles().stream()
                .anyMatch(r -> r.getName().name().equals("ROLE_EMPLOYER") || r.getName().name().equals("EMPLOYER"));

        if (isEmployer) {
            if (currentUser.getCompany() == null) {
                throw new AccessDeniedException("Bạn chưa tham gia vào công ty nào!");
            }
            if (job.getCompany().getId() != currentUser.getCompany().getId()) {
                throw new AccessDeniedException("Lỗi bảo mật: Bạn không có quyền chỉnh sửa/xóa tin tuyển dụng của công ty khác!");
            }
        }
    }

    public ResultPaginationDTO handleGetJobsByCurrentHr(Specification<Job> spec, Pageable pageable) throws Exception {
        
        String email = SecurityUtil.getCurrentUser().orElseThrow(() -> new IdInvalidException("Login please!"));
        User currentHr = userRepository.findByEmail(email);

        if (currentHr.getCompany() == null) {
            throw new IdInvalidException("Bạn chưa tham gia vào công ty nào!");
        }

        
        long companyId = currentHr.getCompany().getId();

        
        Specification<Job> companySpec = (root, query, cb) -> cb.equal(root.get("company").get("id"), companyId);

        
        Specification<Job> finalSpec = spec == null ? companySpec : spec.and(companySpec);

        
        return handleGetAllJobs(finalSpec, pageable);
    }

    
    public ResultPaginationDTO handleGetJobsByCurrentHrWithFilters(
            Specification<Job> spec,
            Pageable pageable,
            String location,
            List<String> levels,
            Long specializationId,
            String companyName,
            Double minSalary,
            Double maxSalary,
            String name,
            String keyword,
            String skill,
            Boolean active,
            Integer sortCreatedAt
    ) throws Exception {

        
        String email = SecurityUtil.getCurrentUser()
                .orElseThrow(() -> new IdInvalidException("Vui lòng đăng nhập!"));
        User currentHr = userRepository.findByEmail(email);

        if (currentHr.getCompany() == null) {
            throw new IdInvalidException("Bạn chưa gia nhập công ty nào!");
        }
        long companyId = currentHr.getCompany().getId();

        
        Specification<Job> securitySpec = (root, query, cb) ->
                cb.equal(root.get("company").get("id"), companyId);

        
        Specification<Job> securedSpec = (spec == null) ? securitySpec : spec.and(securitySpec);

        
        
        return handleGetAllJobsWithFilters(
                securedSpec, pageable, location, levels, specializationId,
                companyName, minSalary, maxSalary, name, skill, active, sortCreatedAt
        );
    }

    private Pageable applyCreatedAtSort(Pageable pageable, Integer sortCreatedAt) {
        boolean requestAsc = sortCreatedAt != null && sortCreatedAt > 0;
        if (sortCreatedAt == null && pageable.getSort().isSorted()) {
            return pageable;
        }
        Sort.Direction direction = requestAsc ? Sort.Direction.ASC : Sort.Direction.DESC;
        return PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), Sort.by(direction, "createdAt"));
    }
}
