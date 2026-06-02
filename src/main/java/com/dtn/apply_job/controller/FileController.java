package com.dtn.apply_job.controller;

import com.dtn.apply_job.common.annotation.ApiMessage;
import com.dtn.apply_job.domain.response.file.ResUploadFileDTO;
import com.dtn.apply_job.exception.FileUploadException;
import com.dtn.apply_job.service.FileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URISyntaxException;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class FileController {

    private final FileService fileService;


    // =========================================================
    // API UPLOAD LÊN CLOUDINARY (DÙNG CHO LUỒNG CHÍNH HIỆN TẠI)
    // =========================================================
    @PostMapping("/files")
    @ApiMessage("Upload single file to Cloudinary")
    public ResponseEntity<ResUploadFileDTO> upload(
            @RequestParam("file") MultipartFile file,
            @RequestParam("folder") String folder) throws URISyntaxException, IOException, FileUploadException {

        // 1. Validate file rỗng
        if (file == null || file.isEmpty()) {
            throw new FileUploadException("File is empty, please try again!");
        }

        // 2. Validate đuôi file (Chỉ cho phép Ảnh và PDF)
        String fileName = file.getOriginalFilename();
        List<String> allowedExtensions = Arrays.asList("jpg", "jpeg", "png", "gif", "pdf", "svg", "webp", "avif");
        boolean isValid = allowedExtensions.stream().anyMatch(i -> fileName.toLowerCase().endsWith(i));

        if (!isValid) {
            throw new FileUploadException("Invalid file extension. Only allow: jpg, jpeg, png, gif, pdf!");
        }

        // 3. Upload thẳng lên Cloudinary (Không cần hàm createDirectory vì Cloudinary tự sinh folder)
        String uploadedFileUrl = this.fileService.storeToCloudinary(file, folder);

        // 4. Trả về DTO chứa Link URL trực tiếp của Cloudinary
        ResUploadFileDTO resUploadFileDTO = new ResUploadFileDTO(uploadedFileUrl, Instant.now());

        return ResponseEntity.ok().body(resUploadFileDTO);
    }


}