package com.dtn.apply_job.mapper;

import com.dtn.apply_job.domain.Notification;
import com.dtn.apply_job.domain.response.notification.ResNotificationDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface NotificationMapper {

    @Mapping(target = "isRead", source = "read")
    ResNotificationDTO toResNotificationDTO(Notification notification);

    List<ResNotificationDTO> toResNotificationDTOList(List<Notification> notifications);
}
