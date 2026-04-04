package com.dtn.apply_job.exception;

import com.dtn.apply_job.domain.response.user.RestRespon;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@ControllerAdvice
public class GlobalException {

    @ExceptionHandler(value = {
            BadCredentialsException.class,
            EmailExistedException.class,
            UsernameNotFoundException.class,
            NameExistedException.class,
    })
    public ResponseEntity<RestRespon<Object>> handleBadRequestException(Exception ex) {
        RestRespon<Object> res = new RestRespon<>();
        res.setStatusCode(HttpStatus.BAD_REQUEST.value());
        res.setMessage(ex.getMessage());
        res.setError(ex.getClass().getSimpleName());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(res);
    }


    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<RestRespon<Object>> validationError(MethodArgumentNotValidException ex) {

        //Lấy ra lỗi sử dụng đối tượng BindingResult
        BindingResult result = ex.getBindingResult();
        final List<FieldError> fieldErrors = result.getFieldErrors();

        RestRespon<Object> res = new RestRespon<>();
        res.setStatusCode(HttpStatus.BAD_REQUEST.value());
        res.setMessage(ex.getBody().getDetail());

        List<String> errors = fieldErrors.stream().map(f -> f.getDefaultMessage()).collect(Collectors.toList());
        res.setMessage(errors.size() > 1 ? errors : errors.get(0));

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(res);

    }

    @ExceptionHandler(value = {
            FileUploadException.class
    })
    public ResponseEntity<RestRespon<Object>> handleFileUploadException(Exception ex) {
        RestRespon<Object> res = new RestRespon<Object>();
        res.setStatusCode(HttpStatus.BAD_REQUEST.value());
        res.setError(ex.getClass().getSimpleName());
        String message = ex.getMessage();
        res.setMessage(
                (message != null && !message.isBlank())
                        ? message
                        : "Upload file failed!"
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(res);
    }


    @ExceptionHandler(value = {
            NoResourceFoundException.class,
            IdInvalidException.class,
    })
    public ResponseEntity<RestRespon<Object>> handleNotFoundException(Exception ex) {
        RestRespon<Object> res = new RestRespon<>();
        res.setStatusCode(HttpStatus.NOT_FOUND.value());
        res.setError(ex.getClass().getSimpleName());
        res.setMessage(ex instanceof NoResourceFoundException ? "URL is not found" : ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(res);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<RestRespon<Object>> handleHttpMessageNotReadableException(HttpMessageNotReadableException ex) {
        RestRespon<Object> res = new RestRespon<>();
        res.setStatusCode(HttpStatus.BAD_REQUEST.value());
        res.setError(ex.getClass().getSimpleName());

        // Khởi tạo câu báo lỗi mặc định
        String errorMessage = "Dữ liệu đầu vào không hợp lệ (Lỗi định dạng JSON).";

        // Đào sâu để lấy nguyên nhân gốc (Bắt lỗi sai giá trị Enum)
        Throwable cause = ex.getCause();
        if (cause instanceof InvalidFormatException invalidFormatException) {
            // Kiểm tra xem trường bị lỗi có phải là Enum hay không
            if (invalidFormatException.getTargetType() != null && invalidFormatException.getTargetType().isEnum()) {
                // Lấy tên trường bị lỗi (VD: "name" hoặc "role")
                String fieldName = invalidFormatException.getPath().get(0).getFieldName();
                // Lấy danh sách giá trị hợp lệ (VD:[ROLE_ADMIN, ROLE_EMPLOYER])
                String allowedValues = Arrays.toString(invalidFormatException.getTargetType().getEnumConstants());

                // Format lại câu báo lỗi tiếng Việt thật dễ hiểu
                errorMessage = String.format("Giá trị không hợp lệ cho trường '%s'. Chỉ chấp nhận các giá trị: %s",
                        fieldName, allowedValues);
            }
        }

        // Gán câu báo lỗi vào RestRespon của bạn
        res.setMessage(errorMessage);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(res);
    }
}
