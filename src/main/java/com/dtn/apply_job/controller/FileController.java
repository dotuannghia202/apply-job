package com.dtn.apply_job.controller;

import com.dtn.apply_job.domain.response.file.ResUploadFileDTO;
import com.dtn.apply_job.exception.FileUploadException;
import com.dtn.apply_job.service.FileService;
import com.dtn.apply_job.util.annotation.ApiMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileNotFoundException;
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

    @GetMapping("/files")
    @ApiMessage("Down load file")
    public ResponseEntity<Resource> download(
            @RequestParam(name = "fileName", required = false) String fileName,
            @RequestParam(name = "folder", required = false) String folder)
            throws FileUploadException, URISyntaxException, FileNotFoundException {
        if (fileName == null || folder == null) {
            throw new FileUploadException("File name or folder required!");
        }

        //check file exist (and not a directory)
        long fileLength = this.fileService.getFileLength(fileName, folder);
        if (fileLength == 0) {
            throw new FileUploadException("File with name = " + fileName + " not found!");
        }

        //download a file
        InputStreamResource resource = this.fileService.getResource(fileName, folder);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
                .contentLength(fileLength)
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(resource);
    }

}
