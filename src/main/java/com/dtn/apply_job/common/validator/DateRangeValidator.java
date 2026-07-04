package com.dtn.apply_job.common.validator;

import com.dtn.apply_job.exception.InvalidDateRangeException;

import java.time.Instant;

public final class DateRangeValidator {

    private DateRangeValidator() {
    }

    public static void validate(Instant startDate, Instant endDate) {
        validate(
                startDate,
                endDate,
                "Ngày bắt đầu phải trước hoặc bằng ngày kết thúc!"
        );
    }

    public static void validate(Instant startDate, Instant endDate, String message) {
        if (startDate != null && endDate != null && startDate.isAfter(endDate)) {
            throw new InvalidDateRangeException(message);
        }
    }
}