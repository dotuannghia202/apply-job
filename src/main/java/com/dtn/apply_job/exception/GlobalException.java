package com.dtn.apply_job.exception;

import com.dtn.apply_job.domain.RestRespon;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.List;
import java.util.stream.Collectors;

@ControllerAdvice
public class GlobalException {

    @ExceptionHandler(value = {
            BadCredentialsException.class,
            UsernameNotFoundException.class
    })


    public ResponseEntity<RestRespon<Object>> handleIdException(Exception ex) {
        RestRespon<Object> res = new  RestRespon<>();
        res.setStatusCode(HttpStatus.BAD_REQUEST.value());
        res.setMessage(ex.getMessage());
        res.setError(ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(res);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<RestRespon<Object>> validationError (MethodArgumentNotValidException ex) {

        //Lấy ra lỗi sử dụng đối tượng BindingResult
        BindingResult result = ex.getBindingResult();
        final List<FieldError> fieldErrors = result.getFieldErrors();

        RestRespon<Object> res = new  RestRespon<>();
        res.setStatusCode(HttpStatus.BAD_REQUEST.value());
        res.setMessage(ex.getBody().getDetail());

        List<String> errors = fieldErrors.stream().map(f -> f.getDefaultMessage()).collect(Collectors.toList());
        res.setMessage(errors.size() > 1 ? errors : errors.get(0));

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(res);

    }

}
