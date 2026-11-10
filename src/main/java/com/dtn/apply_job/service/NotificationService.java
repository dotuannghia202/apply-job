package com.dtn.apply_job.service;

import com.dtn.apply_job.common.response.ResultPaginationDTO;
import com.dtn.apply_job.domain.Notification;
import com.dtn.apply_job.domain.User;
import com.dtn.apply_job.domain.response.notification.ResNotificationDTO;
import com.dtn.apply_job.exception.IdInvalidException;
import com.dtn.apply_job.mapper.NotificationMapper;
import com.dtn.apply_job.repository.NotificationRepository;
import com.dtn.apply_job.repository.UserRepository;
import com.dtn.apply_job.security.SecurityUtil;
import com.dtn.apply_job.util.constant.enums.ERole;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final SimpMessagingTemplate messagingTemplate; 
    private final UserRepository userRepository;
    private final NotificationMapper notificationMapper;

    public NotificationService(NotificationRepository notificationRepository,
                               SimpMessagingTemplate messagingTemplate,
                               UserRepository userRepository,
                               NotificationMapper notificationMapper) {
        this.notificationRepository = notificationRepository;
        this.messagingTemplate = messagingTemplate;
        this.userRepository = userRepository;
        this.notificationMapper = notificationMapper;
    }

    
    public void sendToUser(User user, String title, String message, String type, UUID refId, ERole targetRole) {
        sendToUser(user, title, title, message, message, type, refId, targetRole);
    }

    public void sendToUser(User user, String titleVi, String titleEn, String messageVi, String messageEn, String type, UUID refId, ERole targetRole) {
        Notification notif = new Notification();
        notif.setRecipient(user);
        notif.setTitleVi(titleVi);
        notif.setTitleEn(titleEn);
        notif.setMessageVi(messageVi);
        notif.setMessageEn(messageEn);
        notif.setType(type);
        notif.setReferenceId(refId);
        notif.setTargetRole(targetRole);

        notificationRepository.save(notif);

        ResNotificationDTO dto = notificationMapper.toResNotificationDTO(notif);
        messagingTemplate.convertAndSend("/topic/user/" + user.getId(), dto);
    }

    public void sendToAllAdmins(String title, String message, String type) {
        sendToAllAdmins(title, title, message, message, type);
    }

    public void sendToAllAdmins(String titleVi, String titleEn, String messageVi, String messageEn, String type) {
        List<User> admins = userRepository.findByRoles_Name(ERole.ADMIN);

        for (User admin : admins) {
            Notification notif = new Notification();
            notif.setRecipient(admin);
            notif.setTitleVi(titleVi);
            notif.setTitleEn(titleEn);
            notif.setMessageVi(messageVi);
            notif.setMessageEn(messageEn);
            notif.setType(type);
            notif.setTargetRole(ERole.ADMIN);
            notificationRepository.save(notif);
        }

        Notification wsNotif = new Notification();
        wsNotif.setTitleVi(titleVi);
        wsNotif.setTitleEn(titleEn);
        wsNotif.setMessageVi(messageVi);
        wsNotif.setMessageEn(messageEn);
        wsNotif.setType(type);
        messagingTemplate.convertAndSend("/topic/admin", wsNotif);
    }

    public ResultPaginationDTO getMyNotifications(Boolean isRead, Pageable pageable, ERole role) throws Exception {
        String email = SecurityUtil.getCurrentUser()
                .orElseThrow(() -> new IdInvalidException("Please log in to continue!"));
        User currentUser = userRepository.findByEmail(email);

        Page<Notification> pageNotif;
        if (isRead == null) {
            pageNotif = notificationRepository.findByRecipient_IdAndTargetRoleOrderByCreatedAtDesc(
                    currentUser.getId(), role, pageable);
        } else {
            pageNotif = notificationRepository.findByRecipient_IdAndTargetRoleAndIsReadOrderByCreatedAtDesc(
                    currentUser.getId(), role, isRead, pageable);
        }

        List<ResNotificationDTO> listDTO = pageNotif.getContent().stream()
                .map(notificationMapper::toResNotificationDTO)
                .collect(Collectors.toList());

        ResultPaginationDTO resultPaginationDTO = new ResultPaginationDTO();
        ResultPaginationDTO.Meta meta = new ResultPaginationDTO.Meta();

        meta.setPage(pageNotif.getNumber() + 1);
        meta.setPageSize(pageNotif.getSize());
        meta.setPages(pageNotif.getTotalPages());
        meta.setTotal(pageNotif.getTotalElements());

        resultPaginationDTO.setMeta(meta);
        resultPaginationDTO.setResult(listDTO);

        return resultPaginationDTO;
    }

    
    
    @Transactional
    public void markAsRead(UUID notifId) throws Exception {
        
        Notification notif = notificationRepository.findById(notifId)
                .orElseThrow(() -> new IdInvalidException("Thông báo không tồn tại!"));

        
        String email = SecurityUtil.getCurrentUser()
                .orElseThrow(() -> new IdInvalidException("Please log in to continue!"));

        
        if (!notif.getRecipient().getEmail().equals(email)) {
            throw new AccessDeniedException("Lỗi bảo mật: Bạn không có quyền thay đổi thông báo của người khác!");
        }

        
        notif.setRead(true);
        notificationRepository.save(notif);
    }

    
    
    

    @Transactional
    public void markAllAsRead(ERole role) throws Exception {
        String email = SecurityUtil.getCurrentUser()
                .orElseThrow(() -> new IdInvalidException("Please log in to continue!"));
        User currentUser = userRepository.findByEmail(email);

        
        List<Notification> unreadNotifications = notificationRepository.findByRecipient_IdAndTargetRoleAndIsReadFalse(
                currentUser.getId(), role);

        if (!unreadNotifications.isEmpty()) {
            for (Notification n : unreadNotifications) {
                n.setRead(true);
            }
            notificationRepository.saveAll(unreadNotifications);
        }
    }
}