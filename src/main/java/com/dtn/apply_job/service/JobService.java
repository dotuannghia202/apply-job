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
import com.dtn.apply_job.mapper.JobMapper;
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
import java.util.function.Consumer;
import java.util.stream.Collectors;

@Service
public class JobService {
    private final JobRepository jobRepository;
    private final CompanyRepository companyRepository;
    private final SpecializationRepository specializationRepository;
    private final SkillRepository skillRepository;
    private final UserRepository userRepository;
    private final ApplicationRepository applicationRepository;
    private final JobMapper jobMapper;
    private final AiPythonService aiPythonService;

    public JobService(JobRepository jobRepository, CompanyRepository companyRepository,
                      SpecializationRepository specializationRepository, SkillRepository skillRepository,
                      UserRepository userRepository, ApplicationRepository applicationRepository,
                      JobMapper jobMapper, AiPythonService aiPythonService) {
        this.jobRepository = jobRepository;
        this.companyRepository = companyRepository;
        this.specializationRepository = specializationRepository;
        this.skillRepository = skillRepository;
        this.userRepository = userRepository;
        this.applicationRepository = applicationRepository;
        this.jobMapper = jobMapper;
        this.aiPythonService = aiPythonService;
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

        Job job = jobMapper.toJob(reqDTO);
        job.setCompany(company);
        job.setSpecialization(spec);
        if (skills != null) {
            job.setSkills(skills);
        }

        applyBilingualJobFields(job,
                reqDTO.getName(), reqDTO.getNameVi(), reqDTO.getNameEn(),
                reqDTO.getDescription(), reqDTO.getDescriptionVi(), reqDTO.getDescriptionEn(),
                reqDTO.getRequirements(), reqDTO.getRequirementsVi(), reqDTO.getRequirementsEn(),
                reqDTO.getBenefits(), reqDTO.getBenefitsVi(), reqDTO.getBenefitsEn(),
                reqDTO.getWorkingHours(), reqDTO.getWorkingHoursVi(), reqDTO.getWorkingHoursEn()
        );

        Job savedJob = jobRepository.save(job);

        return convertToResJobDTO(savedJob, Collections.emptySet(), Collections.emptySet());
    }

    public ResultPaginationDTO handleGetAllJobs(Specification<Job> spec, Pageable pageable) {
        Pageable effectivePageable = applyCreatedAtSort(pageable, null);
        Page<Job> pageJob = jobRepository.findAll(spec, effectivePageable);
        Set<UUID> savedJobIds = getSavedJobIdsForCurrentUser();
        Set<UUID> appliedJobIds = getAppliedJobIdsForCurrentUser();
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
            UUID specializationId,
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

        Specification<Job> combinedSpec = spec == null
                ? filterSpec
                : spec.and(filterSpec);

        Pageable effectivePageable = applyCreatedAtSort(pageable, sortCreatedAt);
        Page<Job> pageJob = jobRepository.findAll(combinedSpec, effectivePageable);
        Set<UUID> savedJobIds = getSavedJobIdsForCurrentUser();
        Set<UUID> appliedJobIds = getAppliedJobIdsForCurrentUser();
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

    private void validateSalaryFilter(Double minSalary, Double maxSalary) throws IdInvalidException {
        if (minSalary != null && minSalary < 0) {
            throw new IdInvalidException("Lương tối thiểu phải lớn hơn hoặc bằng 0");
        }
        if (maxSalary != null && maxSalary < 0) {
            throw new IdInvalidException("Lương tối đa phải lớn hơn hoặc bằng 0");
        }
        if (minSalary != null && maxSalary != null && maxSalary < minSalary) {
            throw new IdInvalidException("Lương tối đa phải lớn hơn hoặc bằng lương tối thiểu");
        }
    }

    public ResJobDTO handleGetJobById(UUID id) throws IdInvalidException {
        Job job = jobRepository.findById(id)
                .orElseThrow(() -> new IdInvalidException("Job doesn't exist!"));
        Set<UUID> savedJobIds = getSavedJobIdsForCurrentUser();
        Set<UUID> appliedJobIds = getAppliedJobIdsForCurrentUser();
        return convertToResJobDTO(job, savedJobIds, appliedJobIds);
    }

    public ResUpdateJobDTO handleUpdateJob(UUID id, ReqUpdateJobDTO reqDTO) throws Exception {

        Job currentJob = jobRepository.findById(id)
                .orElseThrow(() -> new IdInvalidException("Job doesn't exist!"));

        checkJobOwnership(currentJob);

        DateRangeValidator.validate(reqDTO.getStartDate(), reqDTO.getEndDate());

        jobMapper.updateJobFromDTO(reqDTO, currentJob);

        applyBilingualJobFields(currentJob,
                reqDTO.getName(), reqDTO.getNameVi(), reqDTO.getNameEn(),
                reqDTO.getDescription(), reqDTO.getDescriptionVi(), reqDTO.getDescriptionEn(),
                reqDTO.getRequirements(), reqDTO.getRequirementsVi(), reqDTO.getRequirementsEn(),
                reqDTO.getBenefits(), reqDTO.getBenefitsVi(), reqDTO.getBenefitsEn(),
                reqDTO.getWorkingHours(), reqDTO.getWorkingHoursVi(), reqDTO.getWorkingHoursEn()
        );

        if (reqDTO.getCompanyId() != null && (currentJob.getCompany() == null || !Objects.equals(currentJob.getCompany().getId(), reqDTO.getCompanyId()))) {
            Company company = companyRepository.findById(reqDTO.getCompanyId())
                    .orElseThrow(() -> new IdInvalidException("Company doesn't exist!"));
            currentJob.setCompany(company);
        }

        UUID currentSpecializationId = currentJob.getSpecialization() != null
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

        return jobMapper.toResUpdateJobDTO(updatedJob);
    }

    public void handleDeleteJob(UUID id) throws Exception {
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
            } catch (Exception ex) {
                throw new IdInvalidException("Dòng " + (i + 1) + ": " + ex.getMessage());
            }
        }

        return results;
    }

    private ResJobDTO convertToResJobDTO(Job job, Set<UUID> savedJobIds, Set<UUID> appliedJobIds) {
        ResJobDTO dto = jobMapper.toResJobDTO(job);
        dto.setIsSaved(savedJobIds != null && savedJobIds.contains(job.getId()));
        dto.setIsApplied(appliedJobIds != null && appliedJobIds.contains(job.getId()));
        dto.setApplicantCount(applicationRepository.countByJob_Id(job.getId()));
        return dto;
    }

    private ResUpdateJobDTO convertToResUpdateJobDTO(Job job) {
        return jobMapper.toResUpdateJobDTO(job);
    }

    private Specification<Job> buildJobFilterSpec(
            String location,
            Set<LevelEnum> levels,
            UUID specializationId,
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
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("nameVi")), "%" + name.trim().toLowerCase() + "%"),
                        cb.like(cb.lower(root.get("nameEn")), "%" + name.trim().toLowerCase() + "%")
                ));
            }

            if (hasText(companyName)) {
                predicates.add(cb.like(
                        cb.lower(root.get("company").get("name")),
                        "%" + companyName.trim().toLowerCase() + "%"
                ));
            }

            if (specializationId != null) {
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
                predicates.add(cb.or(
                        cb.like(cb.lower(skillJoin.get("nameVi")), "%" + skill.trim().toLowerCase() + "%"),
                        cb.like(cb.lower(skillJoin.get("nameEn")), "%" + skill.trim().toLowerCase() + "%")
                ));
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
    public boolean toggleSavedJob(UUID jobId) throws Exception {

        String email = SecurityUtil.getCurrentUser()
                .orElseThrow(() -> new IdInvalidException("Login please!"));
        User candidate = userRepository.findByEmail(email);
        if (candidate == null) {
            throw new IdInvalidException("User not found!");
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

    private Set<UUID> getSavedJobIdsForCurrentUser() {
        try {
            String email = SecurityUtil.getCurrentUser().orElse(null);
            if (email == null) {
                return Collections.emptySet();
            }
            User user = userRepository.findByEmail(email);
            if (user == null || user.getSavedJobs() == null) {
                return Collections.emptySet();
            }
            return user.getSavedJobs().stream().map(Job::getId).collect(Collectors.toSet());
        } catch (Exception e) {
            return Collections.emptySet();
        }
    }

    private boolean hasText(String str) {
        return str != null && !str.trim().isEmpty();
    }

    public ResultPaginationDTO handleGetSavedJobs(Pageable pageable) throws Exception {
        String email = SecurityUtil.getCurrentUser()
                .orElseThrow(() -> new IdInvalidException("Login to continue!"));

        Page<Job> pageJob = jobRepository.findSavedJobsByUserEmail(email, pageable);

        Set<UUID> appliedJobIds = getAppliedJobIdsForCurrentUser();
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

    private java.util.Set<UUID> getAppliedJobIdsForCurrentUser() {
        java.util.Set<UUID> appliedJobIds = new java.util.HashSet<>();
        try {
            String email = SecurityUtil.getCurrentUser().orElse("");
            if (!email.isBlank()) {
                User user = userRepository.findByEmail(email);
                if (user != null) {
                    appliedJobIds = new HashSet<>(applicationRepository.findAppliedJobIdsByCandidate(user));
                }
            }
        } catch (Exception e) {
            return Collections.emptySet();
        }
        return appliedJobIds;
    }

    private void checkJobOwnership(Job job) throws AccessDeniedException, IdInvalidException {
        String email = SecurityUtil.getCurrentUser().orElseThrow(() -> new IdInvalidException("Login please!"));
        User currentUser = userRepository.findByEmail(email);

        boolean isAdmin = currentUser.getRoles().stream()
                .anyMatch(r -> r.getName().name().equals("ROLE_ADMIN") || r.getName().name().equals("ADMIN"));

        if (!isAdmin) {
            boolean isEmployer = currentUser.getRoles().stream()
                    .anyMatch(r -> r.getName().name().equals("ROLE_EMPLOYER") || r.getName().name().equals("EMPLOYER"));

            if (!isEmployer) {
                throw new AccessDeniedException("Bạn không có quyền thực hiện hành động này!");
            }
            if (currentUser.getCompany() == null) {
                throw new AccessDeniedException("Bạn chưa tham gia vào công ty nào!");
            }
            if (!job.getCompany().getId().equals(currentUser.getCompany().getId())) {
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

        UUID companyId = currentHr.getCompany().getId();
        Specification<Job> companySpec = (root, query, cb) -> cb.equal(root.get("company").get("id"), companyId);
        Specification<Job> finalSpec = spec == null ? companySpec : spec.and(companySpec);

        return handleGetAllJobs(finalSpec, pageable);
    }

    public ResultPaginationDTO handleGetJobsByCurrentHrWithFilters(
            Specification<Job> spec,
            Pageable pageable,
            String location,
            List<String> levels,
            UUID specializationId,
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
                .orElseThrow(() -> new IdInvalidException("Please log in to continue!"));
        User currentHr = userRepository.findByEmail(email);

        if (currentHr.getCompany() == null) {
            throw new IdInvalidException("Bạn chưa gia nhập công ty nào!");
        }
        UUID companyId = currentHr.getCompany().getId();

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

    private void applyBilingualJobFields(Job job,
                                         String rawName, String nameVi, String nameEn,
                                         String rawDesc, String descVi, String descEn,
                                         List<String> rawReqs, List<String> reqsVi, List<String> reqsEn,
                                         List<String> rawBens, List<String> bensVi, List<String> bensEn,
                                         String rawWh, String whVi, String whEn) {
        translateAndSetTextField(rawName, nameVi, nameEn, job::setNameVi, job::setNameEn);
        translateAndSetTextField(rawDesc, descVi, descEn, job::setDescriptionVi, job::setDescriptionEn);
        translateAndSetTextField(rawWh, whVi, whEn, job::setWorkingHoursVi, job::setWorkingHoursEn);
        translateAndSetBatchField(rawReqs, reqsVi, reqsEn, job::setRequirementsVi, job::setRequirementsEn);
        translateAndSetBatchField(rawBens, bensVi, bensEn, job::setBenefitsVi, job::setBenefitsEn);
    }

    private void translateAndSetTextField(String raw, String vi, String en, Consumer<String> setVi, Consumer<String> setEn) {
        boolean hasVi = vi != null && !vi.isBlank();
        boolean hasEn = en != null && !en.isBlank();
        if (hasVi && !hasEn) {
            var res = aiPythonService.autoDetectAndTranslate(vi);
            setVi.accept(res.textVi());
            setEn.accept(res.textEn());
        } else if (!hasVi && hasEn) {
            var res = aiPythonService.autoDetectAndTranslate(en);
            setVi.accept(res.textVi());
            setEn.accept(res.textEn());
        } else if (!hasVi && !hasEn && raw != null && !raw.isBlank()) {
            var res = aiPythonService.autoDetectAndTranslate(raw);
            setVi.accept(res.textVi());
            setEn.accept(res.textEn());
        }
    }

    private void translateAndSetBatchField(List<String> raw, List<String> vi, List<String> en, Consumer<List<String>> setVi, Consumer<List<String>> setEn) {
        boolean hasVi = vi != null && !vi.isEmpty();
        boolean hasEn = en != null && !en.isEmpty();
        if (hasVi && !hasEn) {
            var res = aiPythonService.autoDetectAndTranslateBatch(vi);
            setVi.accept(res.textsVi());
            setEn.accept(res.textsEn());
        } else if (!hasVi && hasEn) {
            var res = aiPythonService.autoDetectAndTranslateBatch(en);
            setVi.accept(res.textsVi());
            setEn.accept(res.textsEn());
        } else if (!hasVi && !hasEn && raw != null && !raw.isEmpty()) {
            var res = aiPythonService.autoDetectAndTranslateBatch(raw);
            setVi.accept(res.textsVi());
            setEn.accept(res.textsEn());
        }
    }
}

