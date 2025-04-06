package cit.edu.mmr.controller.websocket;

import cit.edu.mmr.dto.websocket.CommentUpdateDTO;
import cit.edu.mmr.entity.CommentEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
public class CommentWebSocketController {

    private final SimpMessagingTemplate messagingTemplate;

    public CommentWebSocketController(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    // This method can be called from services to broadcast updates
    public void broadcastCommentUpdate(CommentEntity comment, String action) {
        messagingTemplate.convertAndSend(
                "/topic/capsule/" + comment.getTimeCapsule().getId() + "/comments",
                new CommentUpdateDTO(comment, action)
        );
    }
}