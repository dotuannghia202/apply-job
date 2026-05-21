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


        // Gắn quan hệ
        job.setCompany(company);
        job.setSpecialization(spec);
        if (skills != null) {
            job.setSkills(skills); // Ép sang HashSet nếu Entity bạn khai báo là Set
        }

        // Lưu vào DB
        Job savedJob = jobRepository.save(job);

        // 5. Trả về Response DTO

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
            String keyword,
            String skill,
            Boolean active,
            Integer sortCreatedAt
    ) throws IdInvalidException {
        Set<LevelEnum> levelEnums = parseLevelEnums(levels);

        validateSalaryFilter(minSalary, maxSalary);

        Specification<Job> filterSpec = buildJobFilterSpec(
                location, levelEnums, specializationId, companyName,
                minSalary, maxSalary, name, keyword, skill, active
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
        // 1. Tìm Job hiện tại trong DB
        Job currentJob = jobRepository.findById(id)
                .orElseThrow(() -> new IdInvalidException("Job doesn't exist!"));

        checkJobOwnership(currentJob);

        // 2. GHI ĐÈ TRỰC TIẾP CÁC TRƯỜNG THÔNG THƯỜNG (Theo chuẩn PUT)
        DateRangeValidator.validate(reqDTO.getStartDate(), reqDTO.getEndDate());
        currentJob.setName(reqDTO.getName());
        currentJob.setLocation(reqDTO.getLocation());
        currentJob.setMinSalary(reqDTO.getMinSalary());
        currentJob.setMaxSalary(reqDTO.getMaxSalary());
        currentJob.setQuantity(reqDTO.getQuantity());
        currentJob.setLevels(reqDTO.getLevels()); // Sửa thành level (số ít)
        currentJob.setDescription(reqDTO.getDescription());
        currentJob.setRequirements(reqDTO.getRequirements());

        currentJob.setStartDate(reqDTO.getStartDate());
        currentJob.setEndDate(reqDTO.getEndDate());
        currentJob.setActive(reqDTO.getIsActive());
        currentJob.setBenefits(reqDTO.getBenefits());
        currentJob.setWorkingHours(reqDTO.getWorkingHours());

        // 3. KIỂM TRA VÀ CẬP NHẬT COMPANY
        // Dùng == giúp so sánh ID nhanh chóng, nếu ID không đổi thì không cần chọc xuống DB tìm lại
        if (reqDTO.getCompanyId() != null && !Objects.equals(currentJob.getCompany().getId(), reqDTO.getCompanyId())) {
            Company company = companyRepository.findById(reqDTO.getCompanyId())
                    .orElseThrow(() -> new IdInvalidException("Company doesn't exist!"));
            currentJob.setCompany(company);
        }

        // 4. KIỂM TRA VÀ CẬP NHẬT SPECIALIZATION
        Long currentSpecializationId = currentJob.getSpecialization() != null
                ? currentJob.getSpecialization().getId()
                : null;
        if (reqDTO.getSpecializationId() != null && !Objects.equals(currentSpecializationId, reqDTO.getSpecializationId())) {
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

    public void handleDeleteJob(long id) throws Exception {
        Job currentJob = jobRepository.findById(id)
                .orElseThrow(() -> new IdInvalidException("Job doesn't exist!"));
        checkJobOwnership(currentJob);
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

        // MAP IS_APPLIED
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
            // Lọc theo khoảng lương: Job phù hợp khi khoảng lương của job giao với khoảng filter
            // 1. Nếu chỉ truyền minSalary: Tìm các job có lương tối thiểu >= minSalary
            if (minSalary != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("minSalary"), minSalary));
            }

            // 2. Nếu chỉ truyền maxSalary: Tìm các job có lương tối đa <= maxSalary
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

    public ResultPaginationDTO handleGetSavedJobs(Pageable pageable) throws IdInvalidException {
        // 1. Lấy thông tin user hiện tại
        String email = SecurityUtil.getCurrentUser()
                .orElseThrow(() -> new IdInvalidException("Login to continue!"));

        // 2. Chọc xuống DB lấy danh sách Job phân trang
        Page<Job> pageJob = jobRepository.findSavedJobsByUserEmail(email, pageable);

        // 3. Đổ dữ liệu sang DTO
        // Vì đây là danh sách "Việc làm ĐÃ LƯU", nên ta không cần gọi hàm getSavedJobIdsForCurrentUser()
        // mà truyền thẳng 1 Set rỗng, sau đó ép cứng dto.setIsSaved(true) ở bên dưới.
        Set<Long> appliedJobIds = getAppliedJobIdsForCurrentUser();
        List<ResJobDTO> listJobDTO = pageJob.getContent().stream()
                .map(job -> {
                    ResJobDTO dto = convertToResJobDTO(job, Collections.emptySet(), appliedJobIds);
                    dto.setIsSaved(true); // Ép cứng luôn là true
                    return dto;
                })
                .collect(Collectors.toList());

        // 4. Build cục Pagination
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

    // Hàm hỗ trợ: Lấy danh sách ID các công việc mà user hiện tại ĐÃ ỨNG TUYỂN
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
            throw new InputMismatchException("Min salary must be greater than or equal to 0!");
        }

        if (maxSalary != null && maxSalary < 0) {
            throw new InputMismatchException("Max salary must be greater than or equal to 0!");
        }

        if (minSalary != null && maxSalary != null && maxSalary < minSalary) {
            throw new InputMismatchException("Max salary must be greater than or equal to min salary!");
        }
    }

    // Nhớ import: import org.springframework.security.access.AccessDeniedException;

    private void checkJobOwnership(Job job) throws Exception {
        // 1. Lấy user đang đăng nhập
        String email = SecurityUtil.getCurrentUser().orElseThrow(() -> new IdInvalidException("Login please!"));
        User currentUser = userRepository.findByEmail(email);

        // 2. Nếu là ADMIN -> Cho phép làm mọi thứ
        boolean isAdmin = currentUser.getRoles().stream()
                .anyMatch(r -> r.getName().name().equals("ROLE_ADMIN") || r.getName().name().equals("ADMIN"));
        if (isAdmin) return;

        // 3. Nếu là EMPLOYER -> Bắt buộc phải check xem Job này có thuộc Công ty của họ không?
        boolean isEmployer = currentUser.getRoles().stream()
                .anyMatch(r -> r.getName().name().equals("ROLE_EMPLOYER") || r.getName().name().equals("EMPLOYER"));

        if (isEmployer) {
            if (currentUser.getCompany() == null) {
                throw new AccessDeniedException("You haven't joined any company yet!");
            }
            if (job.getCompany().getId() != currentUser.getCompany().getId()) {
                throw new AccessDeniedException("Security Error: You do not have permission to edit/delete job postings from other companies!");
            }
        }
    }

    public ResultPaginationDTO handleGetJobsByCurrentHr(Specification<Job> spec, Pageable pageable) throws Exception {
        // Lấy HR đang đăng nhập
        String email = SecurityUtil.getCurrentUser().orElseThrow(() -> new IdInvalidException("Login please!"));
        User currentHr = userRepository.findByEmail(email);

        if (currentHr.getCompany() == null) {
            throw new IdInvalidException("You haven't joined any company yet!");
        }

        // Lấy ID công ty của HR này
        long companyId = currentHr.getCompany().getId();

        // TẠO CÂU LỆNH SQL ÉP BUỘC: WHERE company_id = ?
        Specification<Job> companySpec = (root, query, cb) -> cb.equal(root.get("company").get("id"), companyId);

        // Nối điều kiện của Công ty với các điều kiện Lọc (Tên, Lương...) mà HR muốn tìm
        Specification<Job> finalSpec = spec == null ? companySpec : spec.and(companySpec);

        // Tận dụng lại hàm GetAll của bạn (truyền cái finalSpec vào là xong)
        return handleGetAllJobs(finalSpec, pageable);
    }

    // Hàm mới: Dành riêng cho HR lọc danh sách job của công ty họ
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

        // 1. KIỂM TRA BẢO MẬT VÀ LẤY ID CÔNG TY
        String email = SecurityUtil.getCurrentUser()
                .orElseThrow(() -> new IdInvalidException("Vui lòng đăng nhập!"));
        User currentHr = userRepository.findByEmail(email);

        if (currentHr.getCompany() == null) {
            throw new IdInvalidException("Bạn chưa gia nhập công ty nào!");
        }
        long companyId = currentHr.getCompany().getId();

        // 2. TẠO LỚP GIÁP BẢO MẬT: Bắt buộc Job phải thuộc về công ty này
        Specification<Job> securitySpec = (root, query, cb) ->
                cb.equal(root.get("company").get("id"), companyId);

        // 3. NỐI LỚP GIÁP VÀO CÁI SPECIFICATION MÀ FRONTEND GỬI LÊN
        Specification<Job> securedSpec = (spec == null) ? securitySpec : spec.and(securitySpec);

        // 4. TÁI SỬ DỤNG LẠI HÀM LỌC "KHỔNG LỒ" CỦA BẠN (Tái sử dụng 100% code cũ)
        // Lưu ý: Truyền securedSpec vào thay vì spec ban đầu
        return handleGetAllJobsWithFilters(
                securedSpec, pageable, location, levels, specializationId,
                companyName, minSalary, maxSalary, name, keyword, skill, active, sortCreatedAt
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
