package com.dtn.apply_job.service;

import com.dtn.apply_job.domain.Notification;
import com.dtn.apply_job.domain.User;
import com.dtn.apply_job.repository.NotificationRepository;
import com.dtn.apply_job.repository.UserRepository;
import com.dtn.apply_job.util.constant.enums.ERole;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final SimpMessagingTemplate messagingTemplate; // Công cụ bắn WebSocket
    private final UserRepository userRepository;

    public NotificationService(NotificationRepository notificationRepository,
                               SimpMessagingTemplate messagingTemplate,
                               UserRepository userRepository) {
        this.notificationRepository = notificationRepository;
        this.messagingTemplate = messagingTemplate;
        this.userRepository = userRepository;
    }

    // 1. Bắn thông báo cho CÁ NHÂN (Employer)
    public void sendToUser(User user, String title, String message) {
        // Lưu DB
        Notification notif = new Notification();
        notif.setRecipient(user);
        notif.setTitle(title);
        notif.setMessage(message);
        notificationRepository.save(notif);

        // Bắn WebSocket vào kênh riêng của user này (VD: /topic/user/5)
        messagingTemplate.convertAndSend("/topic/user/" + user.getId(), notif);
    }

    // 2. Bắn thông báo cho TOÀN BỘ ADMIN
    public void sendToAllAdmins(String title, String message) {
        // Lấy danh sách tất cả Admin
        List<User> admins = userRepository.findByRoles_Name(ERole.ADMIN);

        for (User admin : admins) {
            // Lưu DB cho từng Admin
            Notification notif = new Notification();
            notif.setRecipient(admin);
            notif.setTitle(title);
            notif.setMessage(message);
            notificationRepository.save(notif);
        }

        // Bắn WebSocket vào kênh chung của Admin
        Notification wsNotif = new Notification();
        wsNotif.setTitle(title);
        wsNotif.setMessage(message);
        messagingTemplate.convertAndSend("/topic/admin", wsNotif);
    }
}