package com.dtn.apply_job.service;

import com.cloudinary.Cloudinary;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@Service
public class FileService {

    private final Cloudinary cloudinary;

    public FileService(Cloudinary cloudinary) {
        this.cloudinary = cloudinary;
    }

    public String storeToCloudinary(MultipartFile file, String folder) throws IOException {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("Tệp tin không được trống!");
        }

        String originalFilename = file.getOriginalFilename();
        String extension = "";
        String nameWithoutExt = "file";

        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf(".") + 1).toLowerCase();
            nameWithoutExt = originalFilename.substring(0, originalFilename.lastIndexOf("."));
        }

        Map<String, Object> uploadOptions = new HashMap<>();
        uploadOptions.put("folder", "apply_job/" + folder);


        if ("pdf".equals(extension)) {


            uploadOptions.put("resource_type", "raw");

            uploadOptions.put("public_id", nameWithoutExt + "_" + System.currentTimeMillis() + ".pdf");
        } else {


            uploadOptions.put("resource_type", "auto");

            uploadOptions.put("public_id", nameWithoutExt + "_" + System.currentTimeMillis());
        }

        Map uploadResult = cloudinary.uploader().upload(file.getBytes(), uploadOptions);
        return uploadResult.get("secure_url").toString();
    }

    public String generateDownloadUrl(String fileUrl, String customFilename) {
        if (fileUrl == null || fileUrl.isBlank()) {
            throw new IllegalArgumentException("Đường dẫn tệp tin không hợp lệ!");
        }

        // 1. Chuẩn hóa tên file tải về và loại bỏ kí tự lạ, thêm đuôi pdf nếu thiếu)
        String safeName = (customFilename != null && !customFilename.isBlank()) ?
                customFilename.trim().replaceAll("[^a-zA-Z0-9_.-]", "_")
                : "CV_Resume.pdf";
        if (!safeName.toLowerCase().endsWith(".pdf")) {
            safeName += ".pdf";
        }

        // Encode tên file để tránh lỗi URL
        String encodedFileName = URLEncoder.encode(safeName, StandardCharsets.UTF_8).replace("+", "%20");

        // 2. Chèn 'fl_attachment:Ten_File' vào ngay sau '/upload/' trong link Cloudinary
        if (fileUrl.contains("/upload/")) {
            return fileUrl.replace("/upload/", "/upload/fl_attachment:" + encodedFileName + "/");
        }

        return fileUrl;

    }
}