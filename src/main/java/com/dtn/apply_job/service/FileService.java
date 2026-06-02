package com.dtn.apply_job.service;

import com.cloudinary.Cloudinary;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
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
            throw new IllegalArgumentException("File is not blank!");
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

        // Cấu hình linh hoạt cho từng loại file
        if ("pdf".equals(extension)) {
            // ĐỐI VỚI PDF:
            // 1. Phải khai báo là 'raw'
            uploadOptions.put("resource_type", "raw");
            // 2. PHẢI CÓ .pdf trong public_id để Cloudinary biết nó là PDF
            uploadOptions.put("public_id", nameWithoutExt + "_" + System.currentTimeMillis() + ".pdf");
        } else {
            // ĐỐI VỚI ẢNH:
            // 1. Dùng 'auto' (sẽ được Cloudinary hiểu là 'image')
            uploadOptions.put("resource_type", "auto");
            // 2. Không cần đuôi mở rộng trong public_id vì Cloudinary tự quản lý format
            uploadOptions.put("public_id", nameWithoutExt + "_" + System.currentTimeMillis());
        }

        Map uploadResult = cloudinary.uploader().upload(file.getBytes(), uploadOptions);
        return uploadResult.get("secure_url").toString();
    }
}