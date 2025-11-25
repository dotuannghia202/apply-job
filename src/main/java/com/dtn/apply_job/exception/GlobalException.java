package com.dtn.apply_job.exception;

import com.dtn.apply_job.domain.User;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalException {
    @ExceptionHandler(IdInvalidException.class)
    public ResponseEntity<String> handleIdInvalidException(IdInvalidException ex) {
        return  ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }


}
