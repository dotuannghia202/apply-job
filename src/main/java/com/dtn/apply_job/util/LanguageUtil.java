package com.dtn.apply_job.util;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Collections;
import java.util.List;

public class LanguageUtil {

    private LanguageUtil() {} //ngăn không cho tạo đối tượng bên ngoài vì bên trong toàn là biến static

    // Tự động lấy ngôn ngữ từ request
    public static String getCurrentLanguage() {
        RequestAttributes attribs = RequestContextHolder.getRequestAttributes();
        if (attribs instanceof ServletRequestAttributes servletAttributes) {
            HttpServletRequest request = servletAttributes.getRequest();
            String langParam = request.getParameter("lang");
            if (langParam != null && !langParam.isBlank()) {
                return langParam.trim().toLowerCase().startsWith("en") ? "en" : "vi";
            }
            String acceptLanguage = request.getHeader("Accept-Language");
            if (acceptLanguage != null && !acceptLanguage.isBlank()) {
                return acceptLanguage.trim().toLowerCase().startsWith("en") ? "en" : "vi";
            }
        }
        return "vi";
    }

    public static boolean isEnglish() {
        return "en".equalsIgnoreCase(getCurrentLanguage());
    }

    public static String resolve(String textVi, String textEn) {
        if (isEnglish()) {
            return (textEn != null && !textEn.isBlank()) ? textEn : (textVi != null ? textVi : "");
        }
        return (textVi != null && !textVi.isBlank()) ? textVi : (textEn != null ? textEn : "");
    }

    public static List<String> resolve(List<String> listVi, List<String> listEn) {
        if (isEnglish()) {
            return (listEn != null && !listEn.isEmpty()) ? listEn : (listVi != null ? listVi : Collections.emptyList());
        }
        return (listVi != null && !listVi.isEmpty()) ? listVi : (listEn != null ? listEn : Collections.emptyList());
    }
}
