package com.dtn.apply_job;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;

@SpringBootApplication
//@SpringBootApplication(exclude = { SecurityAutoConfiguration.class })
public class ApplyJobApplication {

	public static void main(String[] args) {
		SpringApplication.run(ApplyJobApplication.class, args);
	}

}
