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

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @ApiMessage("Lấy danh sách thông báo theo Role")
    public ResponseEntity<ResultPaginationDTO> getMyNotifications(
            @RequestParam(required = false) Boolean isRead,
            @RequestParam("role") ERole role,
            Pageable pageable) throws Exception {

        ResultPaginationDTO result = notificationService.getMyNotifications(isRead, pageable, role);
        return ResponseEntity.ok(result);
    }

    @PutMapping("/{id:\\d+}/read")
    @PreAuthorize("isAuthenticated()")
    @ApiMessage("Đánh dấu thông báo đã đọc thành công")
    public ResponseEntity<Void> markAsRead(@PathVariable("id") Long id) throws Exception {

        notificationService.markAsRead(id);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/read-all")
    @PreAuthorize("isAuthenticated()")
    @ApiMessage("Đánh dấu tất cả thông báo đã đọc thành công")
    public ResponseEntity<Void> markAllAsRead(@RequestParam("role") ERole role) throws Exception {

        notificationService.markAllAsRead(role);
        return ResponseEntity.ok().build();
    }
}
