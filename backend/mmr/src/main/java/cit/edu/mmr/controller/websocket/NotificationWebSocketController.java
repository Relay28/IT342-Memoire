package cit.edu.mmr.controller.websocket;

import cit.edu.mmr.dto.websocket.NotificationDTO;
import cit.edu.mmr.entity.NotificationEntity;
import cit.edu.mmr.service.NotificationService;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
public class NotificationWebSocketController {
    private static final Logger logger = LoggerFactory.getLogger(NotificationWebSocketController.class);

    private final NotificationService notificationService;

    @Autowired
    public NotificationWebSocketController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @MessageMapping("/notifications/connect")
    @SendToUser("/topic/notifications/connect")
    public Map<String, Object> handleConnectionRequest(Authentication authentication) {
        logger.info("WebSocket connection established for user: {}", authentication.getName());

        // Get current notification count
        long unreadCount = notificationService.getUnreadNotificationCount(authentication);
        List<NotificationEntity> recentNotifications = notificationService.getUnreadNotifications(authentication);

        // Convert entities to DTOs
        List<NotificationDTO> notificationDTOs = recentNotifications.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        Map<String, Object> response = new HashMap<>();
        response.put("count", unreadCount);
        response.put("notifications", notificationDTOs);

        return response;
    }

    private NotificationDTO convertToDTO(NotificationEntity entity) {
        NotificationDTO dto = new NotificationDTO();
        dto.setId(entity.getId());
        dto.setType(entity.getType());
        dto.setText(entity.getText());
        dto.setRead(entity.isRead());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setItemType(entity.getItemType());
        dto.setRelatedItemId(entity.getRelatedItemId());
        return dto;
    }
}