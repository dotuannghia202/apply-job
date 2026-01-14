package com.dtn.apply_job.exception;

public class NameExistedException extends RuntimeException {
    public NameExistedException(String message) {
        super(message);
    }
}
