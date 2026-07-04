package com.dtn.apply_job.service;

import com.dtn.apply_job.common.response.ResultPaginationDTO;
import com.dtn.apply_job.domain.Notification;
import com.dtn.apply_job.domain.User;
import com.dtn.apply_job.domain.response.notification.ResNotificationDTO;
import com.dtn.apply_job.exception.IdInvalidException;
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
import java.util.stream.Collectors;

@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final SimpMessagingTemplate messagingTemplate; 
    private final UserRepository userRepository;

    public NotificationService(NotificationRepository notificationRepository,
                               SimpMessagingTemplate messagingTemplate,
                               UserRepository userRepository) {
        this.notificationRepository = notificationRepository;
        this.messagingTemplate = messagingTemplate;
        this.userRepository = userRepository;
    }

    
    public void sendToUser(User user, String title, String message, String type, Long refId, ERole targetRole) {
        Notification notif = new Notification();
        notif.setRecipient(user);
        notif.setTitle(title);
        notif.setMessage(message);
        notif.setType(type);
        notif.setReferenceId(refId);

        notif.setTargetRole(targetRole); 

        notificationRepository.save(notif);

        
        ResNotificationDTO dto = convertToDTO(notif);
        messagingTemplate.convertAndSend("/topic/user/" + user.getId(), dto);
    }

    
    public void sendToAllAdmins(String title, String message, String type) {
        
        List<User> admins = userRepository.findByRoles_Name(ERole.ADMIN);

        for (User admin : admins) {
            
            Notification notif = new Notification();
            notif.setRecipient(admin);
            notif.setTitle(title);
            notif.setMessage(message);
            notif.setType(type); 
            notif.setTargetRole(ERole.ADMIN); 
            notificationRepository.save(notif);
        }

        
        Notification wsNotif = new Notification();
        wsNotif.setTitle(title);
        wsNotif.setMessage(message);
        wsNotif.setType(type); 
        messagingTemplate.convertAndSend("/topic/admin", wsNotif);
    }

    

    public ResultPaginationDTO getMyNotifications(Boolean isRead, Pageable pageable, ERole role) throws Exception {
        String email = SecurityUtil.getCurrentUser()
                .orElseThrow(() -> new IdInvalidException("Vui lòng đăng nhập!"));
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
                .map(this::convertToDTO)
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

    private ResNotificationDTO convertToDTO(Notification notification) {
        ResNotificationDTO dto = new ResNotificationDTO();
        dto.setId(notification.getId());
        dto.setTitle(notification.getTitle());
        dto.setMessage(notification.getMessage());
        dto.setRead(notification.isRead());
        dto.setCreatedAt(notification.getCreatedAt());

        dto.setType(notification.getType());
        dto.setReferenceId(notification.getReferenceId());
        return dto;
    }

    
    
    @Transactional
    public void markAsRead(Long notifId) throws Exception {
        
        Notification notif = notificationRepository.findById(notifId)
                .orElseThrow(() -> new IdInvalidException("Thông báo không tồn tại!"));

        
        String email = SecurityUtil.getCurrentUser()
                .orElseThrow(() -> new IdInvalidException("Vui lòng đăng nhập!"));

        
        if (!notif.getRecipient().getEmail().equals(email)) {
            throw new AccessDeniedException("Lỗi bảo mật: Bạn không có quyền thay đổi thông báo của người khác!");
        }

        
        notif.setRead(true);
        notificationRepository.save(notif);
    }

    
    
    

    @Transactional
    public void markAllAsRead(ERole role) throws Exception {
        String email = SecurityUtil.getCurrentUser()
                .orElseThrow(() -> new IdInvalidException("Vui lòng đăng nhập!"));
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