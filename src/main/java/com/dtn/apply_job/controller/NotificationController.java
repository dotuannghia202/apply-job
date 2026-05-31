package com.dtn.apply_job.controller;

import com.dtn.apply_job.common.annotation.ApiMessage;
import com.dtn.apply_job.common.response.ResultPaginationDTO;
import com.dtn.apply_job.service.NotificationService;
import com.dtn.apply_job.util.constant.enums.ERole;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    // 1. LẤY DANH SÁCH THÔNG BÁO (CÓ PHÂN TRANG & LỌC TRẠNG THÁI ĐÃ ĐỌC)
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @ApiMessage("Lấy danh sách thông báo theo Role")
    public ResponseEntity<ResultPaginationDTO> getMyNotifications(
            @RequestParam(required = false) Boolean isRead,
            @RequestParam("role") ERole role, // 👉 Bắt FE phải truyền Role lên
            Pageable pageable) throws Exception {

        ResultPaginationDTO result = notificationService.getMyNotifications(isRead, pageable, role);
        return ResponseEntity.ok(result);
    }

    // 2. ĐÁNH DẤU 1 THÔNG BÁO LÀ "ĐÃ ĐỌC" (Khi user click vào 1 dòng thông báo)
    @PutMapping("/{id:\\d+}/read")
    @PreAuthorize("isAuthenticated()")
    @ApiMessage("Đánh dấu thông báo đã đọc thành công")
    public ResponseEntity<Void> markAsRead(@PathVariable("id") Long id) throws Exception {

        notificationService.markAsRead(id);
        return ResponseEntity.ok().build();
    }

    // 3. ĐÁNH DẤU "ĐÃ ĐỌC TẤT CẢ" (Khi user click nút Mark All As Read)
    @PutMapping("/read-all")
    @PreAuthorize("isAuthenticated()")
    @ApiMessage("Đánh dấu tất cả thông báo đã đọc thành công")
    public ResponseEntity<Void> markAllAsRead(@RequestParam("role") ERole role) throws Exception {

        notificationService.markAllAsRead(role); // Truyền role xuống service
        return ResponseEntity.ok().build();
    }
}
