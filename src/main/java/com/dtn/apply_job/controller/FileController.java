package com.dtn.apply_job.controller;

import com.dtn.apply_job.domain.response.file.ResUploadFileDTO;
import com.dtn.apply_job.exception.FileUploadException;
import com.dtn.apply_job.service.FileService;
import com.dtn.apply_job.util.annotation.ApiMessage;
import org.springframework.beans.factory.annotation.Value;
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
public class FileController {

    @Value("${devgay.upload-file.base-uri}")
    private String baseUri;

    private final FileService fileService;

    public FileController(FileService fileService) {
        this.fileService = fileService;
    }

    @PostMapping("/files")
    @ApiMessage("Upload single file")
    public ResponseEntity<ResUploadFileDTO> upload(@RequestParam("file") MultipartFile file, @RequestParam("folder") String folder) throws URISyntaxException, IOException, FileUploadException {

        //validate
        if (file.isEmpty()) {
            throw new FileUploadException("File is empty, please try again!");
        }
        //create file if is not existed
        this.fileService.createDirectory(baseUri + folder);
        String fileName = file.getOriginalFilename();
        List<String> allowedExtensions = Arrays.asList("jpg", "jpeg", "png", "gif", "pdf");
        boolean isValid = allowedExtensions.stream().anyMatch(i -> fileName.toLowerCase().endsWith(i));
        if (!isValid) {
            throw new FileUploadException("Invalid file extension, please try again!");
        }
        //store file
        String uploadedFile = this.fileService.store(file, folder);

        ResUploadFileDTO resUploadFileDTO = new ResUploadFileDTO(uploadedFile, Instant.now());
        return ResponseEntity.ok().body(resUploadFileDTO);
    }
}
