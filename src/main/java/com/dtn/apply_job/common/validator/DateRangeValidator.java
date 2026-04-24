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
                "Start date must be before or equal to end date!"
        );
    }

    public static void validate(Instant startDate, Instant endDate, String message) {
        if (startDate != null && endDate != null && startDate.isAfter(endDate)) {
            throw new InvalidDateRangeException(message);
        }
    }
}