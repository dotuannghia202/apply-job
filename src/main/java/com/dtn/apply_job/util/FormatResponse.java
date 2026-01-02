package com.dtn.apply_job.util;

import com.dtn.apply_job.domain.response.RestRespon;
import com.dtn.apply_job.util.annotation.ApiMessage;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpResponse;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

@ControllerAdvice
public class FormatResponse implements ResponseBodyAdvice<Object> {
    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        return true;
    }

    @Override
    public Object beforeBodyWrite(Object body, MethodParameter returnType, MediaType selectedContentType, Class<? extends HttpMessageConverter<?>> selectedConverterType, ServerHttpRequest request, ServerHttpResponse response) {
        if (response instanceof ServletServerHttpResponse servletServerHttpResponse) {
            HttpServletResponse httpServletResponse = servletServerHttpResponse.getServletResponse();

            int statusCode = httpServletResponse.getStatus();
            RestRespon<Object> res = new RestRespon<>();
            res.setStatusCode(statusCode);

            if (body instanceof String) {
                return body;
            }

            if (statusCode >= 400) {
                return body;
            } else {
                res.setData(body);
                ApiMessage message = returnType.getMethodAnnotation(ApiMessage.class);
                res.setMessage(message != null ? message.value() : "Call Api Success");
            }
            return res;
        }
        return body;
    }
}
