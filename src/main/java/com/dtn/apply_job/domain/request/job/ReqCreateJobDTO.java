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
public class ReqCreateJobDTO {

    @NotBlank(message = "Tiêu đề công việc không được để trống!")
    @Size(max = 255, message = "Tiêu đề công việc tối đa 255 ký tự!")
    private String name;

    @NotBlank(message = "Địa điểm làm việc không được để trống!")
    private String location;

    @NotNull(message = "Lương tối thiểu không được để trống!")
    @PositiveOrZero(message = "Lương tối thiểu phải lớn hơn hoặc bằng 0!")
    private Double minSalary;

    @NotNull(message = "Lương tối đa không được để trống!")
    @PositiveOrZero(message = "Lương tối đa phải lớn hơn hoặc bằng 0!")
    private Double maxSalary;

    @NotNull(message = "Số lượng tuyển dụng không được để trống!")
    @Positive(message = "Số lượng tuyển dụng phải lớn hơn 0!")
    private Integer quantity;

    @NotBlank(message = "Mô tả công việc không được để trống!")
    private String description;

    @NotEmpty(message = "Yêu cầu công việc không được để trống!")
    private List<@NotBlank(message = "Yêu cầu không được để trống!") String> requirements;

    @NotEmpty(message = "Cấp bậc không được để trống!")
    private Set<@NotNull(message = "Cấp bậc không được để trống!") LevelEnum> levels;

    @NotNull(message = "Ngày bắt đầu không được để trống!")
    private Instant startDate;

    @NotNull(message = "Hạn nhận hồ sơ không được để trống!")
    private Instant endDate;

    private Boolean active;

    private List<String> benefits;

    @NotBlank(message = "Thời gian làm việc không được để trống!")
    private String workingHours;

    @NotNull(message = "Mã công ty không được để trống!")
    private Long companyId;

    @NotNull(message = "Mã chuyên ngành không được để trống!")
    private Long specializationId;

    @NotEmpty(message = "Mã kỹ năng không được để trống!")
    private List<@NotNull(message = "Mã kỹ năng không được để trống!")
    @Positive(message = "ID kỹ năng phải lớn hơn 0!")
            Long> skillIds;

    @AssertTrue(message = "Lương tối đa phải lớn hơn hoặc bằng lương tối thiểu!")
    public boolean isSalaryRangeValid() {
        if (minSalary == null || maxSalary == null) {
            return true;
        }
        return maxSalary >= minSalary;
    }
}