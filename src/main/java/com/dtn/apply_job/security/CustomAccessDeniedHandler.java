package com.dtn.apply_job.security;

import com.dtn.apply_job.common.response.RestRespon;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CustomAccessDeniedHandler implements AccessDeniedHandler {

    ObjectMapper objectMapper;

    @Override
    public void handle(
            HttpServletRequest request,
            HttpServletResponse response,
            AccessDeniedException accessDeniedException
    ) throws IOException, ServletException {

        RestRespon<Object> res = new RestRespon<>();
        res.setStatusCode(HttpStatus.FORBIDDEN.value());
        res.setError("FORBIDDEN");

        String exMessage = accessDeniedException.getMessage();
        String finalMessage = "You do not have permission to access this resource!";

        if (exMessage != null && !exMessage.isBlank() && !exMessage.equalsIgnoreCase("Access is denied")) {
            finalMessage = exMessage;
        }

        res.setMessage(finalMessage);
        res.setData(null);

        response.setStatus(HttpStatus.FORBIDDEN.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());

        objectMapper.writeValue(response.getWriter(), res);
    }
}