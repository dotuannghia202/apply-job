package com.dtn.apply_job.security;

import com.dtn.apply_job.common.response.RestRespon;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Component
public class CustomAccessDeniedHandler implements AccessDeniedHandler {

    private final ObjectMapper objectMapper;

    public CustomAccessDeniedHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void handle(
            HttpServletRequest request,
            HttpServletResponse response,
            AccessDeniedException accessDeniedException
    ) throws IOException, ServletException {

        RestRespon<Object> res = new RestRespon<>();
        res.setStatusCode(HttpStatus.FORBIDDEN.value());
        res.setError("FORBIDDEN");

        // 1. Lấy thông báo lỗi do Exception ném ra
        String exMessage = accessDeniedException.getMessage();

        // 2. Câu thông báo mặc định của hệ thống bạn
        String finalMessage = "You do not have permission to access this resource!";

        // 3. Nếu bạn CÓ truyền message custom VÀ message đó KHÔNG phải câu mặc định của Spring
        if (exMessage != null && !exMessage.isBlank() && !exMessage.equalsIgnoreCase("Access is denied")) {
            finalMessage = exMessage; // Lấy câu custom của bạn
        }

        res.setMessage(finalMessage);
        res.setData(null);

        response.setStatus(HttpStatus.FORBIDDEN.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());

        objectMapper.writeValue(response.getWriter(), res);
    }
}