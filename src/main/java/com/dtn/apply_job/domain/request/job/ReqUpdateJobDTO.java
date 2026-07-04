package com.dtn.apply_job.domain.request.job;

import com.dtn.apply_job.util.constant.enums.LevelEnum;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.List;
import java.util.Set;

@Getter
@Setter
public class ReqUpdateJobDTO {

    @Size(max = 255, message = "Tiêu đề công việc tối đa 255 ký tự!")
    private String name;

    private String location;

    @PositiveOrZero(message = "Lương tối thiểu phải lớn hơn hoặc bằng 0!")
    private Double minSalary;

    @PositiveOrZero(message = "Lương tối đa phải lớn hơn hoặc bằng 0!")
    private Double maxSalary;

    @Positive(message = "Số lượng tuyển dụng phải lớn hơn 0!")
    private Integer quantity;

    private String description;

    private List<String> requirements;

    private Set<LevelEnum> levels;

    private Instant startDate;

    @NotNull(message = "Hạn nhận hồ sơ không được để trống!")
    private Instant endDate;


    private List<String> benefits;

    @NotBlank(message = "Thời gian làm việc không được để trống!")
    private String workingHours;

    private Long companyId;

    private Long specializationId;

    private List<@Positive(message = "ID kỹ năng phải lớn hơn 0!") Long> skillIds;

    @AssertTrue(message = "Lương tối đa phải lớn hơn hoặc bằng lương tối thiểu!")
    public boolean isSalaryRangeValid() {
        if (minSalary == null || maxSalary == null) {
            return true;
        }
        return maxSalary >= minSalary;
    }
}