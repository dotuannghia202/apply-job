package com.dtn.apply_job;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync

public class ApplyJobApplication {

    public static void main(String[] args) {
        SpringApplication.run(ApplyJobApplication.class, args);
    }

}
