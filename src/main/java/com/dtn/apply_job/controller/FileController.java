package com.dtn.apply_job.controller;

import com.dtn.apply_job.common.annotation.ApiMessage;
import com.dtn.apply_job.domain.response.file.ResDownloadFileDTO;
import com.dtn.apply_job.domain.response.file.ResUploadFileDTO;
import com.dtn.apply_job.exception.FileUploadException;
import com.dtn.apply_job.service.FileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
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


    @PostMapping("/files")
    @ApiMessage("Tải lên tập tin lên Cloudinary thành công")
    public ResponseEntity<ResUploadFileDTO> upload(
            @RequestParam("file") MultipartFile file,
            @RequestParam("folder") String folder) throws URISyntaxException, IOException, FileUploadException {

        if (file == null || file.isEmpty()) {
            throw new FileUploadException("Tệp tin bị trống, vui lòng thử lại!");
        }

        String fileName = file.getOriginalFilename();
        List<String> allowedExtensions = Arrays.asList("jpg", "jpeg", "png", "gif", "pdf", "svg", "webp", "avif");
        boolean isValid = allowedExtensions.stream().anyMatch(i -> fileName.toLowerCase().endsWith(i));

        if (!isValid) {
            throw new FileUploadException("Phần mở rộng của tệp tin không hợp lệ. Chỉ cho phép các định dạng: jpg, jpeg, png, gif, pdf, svg, webp, avif!");
        }

        String uploadedFileUrl = this.fileService.storeToCloudinary(file, folder);

        ResUploadFileDTO resUploadFileDTO = new ResUploadFileDTO(uploadedFileUrl, Instant.now());

        return ResponseEntity.ok().body(resUploadFileDTO);
    }

    @GetMapping("/files/download")
    @ApiMessage("Lấy đường link tải xuống thành công")
    public ResponseEntity<ResDownloadFileDTO> getDownloadUrl(
            @RequestParam("fileUrl") String fileUrl,
            @RequestParam(value = "fileName", required = false) String fileName
    ) {
        String downloadUrl = this.fileService.generateDownloadUrl(fileUrl, fileName);
        ResDownloadFileDTO res = new ResDownloadFileDTO(downloadUrl, fileName);
        return ResponseEntity.ok().body(res);
    }
}