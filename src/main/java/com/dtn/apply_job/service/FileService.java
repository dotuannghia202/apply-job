package com.dtn.apply_job.service;

import com.cloudinary.Cloudinary;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Map;

@Service
public class FileService {

    @Value("${devgay.upload-file.base-uri}")
    private String baseUri;

    private final Cloudinary cloudinary; // Tiêm Cloudinary vào đây

    public FileService(Cloudinary cloudinary) {
        this.cloudinary = cloudinary;
    }

    // =========================================================================
    // OPTION 1: UPLOAD LÊN CLOUD (CLOUDINARY) - KHUYÊN DÙNG CHO ĐỒ ÁN NÀY
    // =========================================================================

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

        Map<String, Object> uploadOptions = new java.util.HashMap<>();
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
    }    // =========================================================================
    // OPTION 2: UPLOAD VÀO LOCAL SERVER (CODE CŨ CỦA BẠN - GIỮ LẠI LÀM BACKUP)
    // =========================================================================

    public void createDirectory(String folder) throws URISyntaxException {
        URI uri = new URI(folder);
        Path path = Paths.get(uri);
        File tmpDir = new File(path.toString());
        if (!tmpDir.isDirectory()) {
            try {
                Files.createDirectory(tmpDir.toPath());
                System.out.println(">>> CREATE NEW DIRECTORY SUCCESSFUL, PATH = " + tmpDir.toPath());
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            System.out.println(">>> SKIP MAKING DIRECTORY, ALREADY EXISTS");
        }
    }

    public String storeLocal(MultipartFile file, String folder) throws URISyntaxException, IOException {
        // create unique filename
        String finalName = System.currentTimeMillis() + "-" + file.getOriginalFilename();

        URI uri = new URI(baseUri + folder + "/" + finalName);
        Path path = Paths.get(uri);
        try (InputStream inputStream = file.getInputStream()) {
            Files.copy(inputStream, path, StandardCopyOption.REPLACE_EXISTING);
        }
        return finalName;
    }

    public long getFileLength(String fileName, String folder) throws URISyntaxException {
        URI uri = new URI(baseUri + folder + "/" + fileName);
        Path path = Paths.get(uri);

        File tmpDir = new File(path.toString());

        if (!tmpDir.exists() || tmpDir.isDirectory()) {
            return 0;
        }
        return tmpDir.length();
    }

    public InputStreamResource getResource(String fileName, String folder) throws URISyntaxException, FileNotFoundException {
        URI uri = new URI(baseUri + folder + "/" + fileName);
        Path path = Paths.get(uri);

        File file = new File(path.toString());
        return new InputStreamResource(new FileInputStream(file));
    }
}