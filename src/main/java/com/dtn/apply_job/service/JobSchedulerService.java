package com.dtn.apply_job.service;

import com.dtn.apply_job.repository.JobRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class JobSchedulerService {
    private final JobRepository jobRepository;

    @Scheduled(cron = "0 0 0 * * ?", zone = "Asia/Ho_Chi_Minh")
    public void autoDeactivateExpiredJobs() {
        System.out.println(">>> [CRON JOB] Đang kiểm tra và khóa các công việc hết hạn...");

        Instant now = Instant.now();
        int expiredCount = jobRepository.deactivateExpiredJobs(now);

        if (expiredCount > 0) {
            System.out.println(">>> [CRON JOB] Đã tự động khóa " + expiredCount + " công việc hết hạn!");
        } else {
            System.out.println(">>> [CRON JOB] Không có công việc nào hết hạn hôm nay.");
        }
    }
}
