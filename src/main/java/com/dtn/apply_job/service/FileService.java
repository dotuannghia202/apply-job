package com.dtn.apply_job.service;

import com.cloudinary.Cloudinary;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
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


    public byte[] downloadFileBytes(String fileUrl) throws IOException {
        if (fileUrl == null || fileUrl.isBlank()) {
            throw new IllegalArgumentException("Đường dẫn file không hợp lệ!");
        }
        try (InputStream in = URI.create(fileUrl).toURL().openStream()) {
            return in.readAllBytes();
        }
    }
}