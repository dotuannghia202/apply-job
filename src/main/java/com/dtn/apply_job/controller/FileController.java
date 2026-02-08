package com.dtn.apply_job.controller;

import com.dtn.apply_job.service.FileService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.net.URISyntaxException;


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
    public String upload(@RequestParam("file") MultipartFile file, @RequestParam("folder") String folder) throws URISyntaxException {

        //create file if is not existed
        this.fileService.createDirectory(baseUri + folder);
        //store file
        return file.getOriginalFilename() + folder;
    }
}
