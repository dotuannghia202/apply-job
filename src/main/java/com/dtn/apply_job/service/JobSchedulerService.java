package com.dtn.apply_job.service;

import com.dtn.apply_job.repository.JobRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class JobSchedulerService {

    private final JobRepository jobRepository;

    /**
     * TÌNH HUỐNG 1: Server chạy ổn định, tự động quét vào 0h00 đêm mỗi ngày
     */
    @Scheduled(cron = "0 0 0 * * ?", zone = "Asia/Ho_Chi_Minh")
    public void scheduledDeactivateJobs() {
        System.out.println(">>> [CRON JOB 0h00] Đang kiểm tra công việc hết hạn...");
        executeCleanup();
    }

    /**
     * TÌNH HUỐNG 2: Server vừa bị sập và được bật lại -> Quét BÙ NGAY LẬP TỨC
     * Sự kiện này tự động kích hoạt khi Spring Boot vừa khởi động xong 100%
     */
    @EventListener(ApplicationReadyEvent.class)
    public void runOnStartup() {
        System.out.println(">>> [SERVER STARTUP] Đang rà soát bù các công việc hết hạn trong lúc Server tắt...");
        executeCleanup();
    }

    /**
     * HÀM DÙNG CHUNG: Logic dọn dẹp Database
     */
    private void executeCleanup() {
        Instant now = Instant.now();
        int expiredCount = jobRepository.deactivateExpiredJobs(now);

        if (expiredCount > 0) {
            System.out.println(">>> [CLEANUP] Đã tự động khóa " + expiredCount + " công việc hết hạn!");
        } else {
            System.out.println(">>> [CLEANUP] Hệ thống sạch sẽ, không có công việc nào quá hạn.");
        }
    }
}
