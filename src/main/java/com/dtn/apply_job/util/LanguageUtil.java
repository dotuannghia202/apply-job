package com.dtn.apply_job.util;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Collections;
import java.util.List;

public class LanguageUtil {

    private LanguageUtil() {}

    // Tự động lấy ngôn ngữ từ request, fallback là English ("en")
    public static String getCurrentLanguage() {
        RequestAttributes attribs = RequestContextHolder.getRequestAttributes();
        if (attribs instanceof ServletRequestAttributes servletAttributes) {
            HttpServletRequest request = servletAttributes.getRequest();
            String langParam = request.getParameter("lang");
            if (langParam != null && !langParam.isBlank()) {
                return langParam.trim().toLowerCase().startsWith("vi") ? "vi" : "en";
            }
            String acceptLanguage = request.getHeader("Accept-Language");
            if (acceptLanguage != null && !acceptLanguage.isBlank()) {
                return acceptLanguage.trim().toLowerCase().startsWith("vi") ? "vi" : "en";
            }
        }
        return "en";
    }

    public static boolean isEnglish() {
        return "en".equalsIgnoreCase(getCurrentLanguage());
    }

    public static String resolve(String textVi, String textEn) {
        if (!isEnglish()) {
            return (textVi != null && !textVi.isBlank()) ? textVi : (textEn != null ? textEn : "");
        }
        return (textEn != null && !textEn.isBlank()) ? textEn : (textVi != null ? textVi : "");
    }

    public static List<String> resolve(List<String> listVi, List<String> listEn) {
        if (!isEnglish()) {
            return (listVi != null && !listVi.isEmpty()) ? listVi : (listEn != null ? listEn : Collections.emptyList());
        }
        return (listEn != null && !listEn.isEmpty()) ? listEn : (listVi != null ? listVi : Collections.emptyList());
    }
}
