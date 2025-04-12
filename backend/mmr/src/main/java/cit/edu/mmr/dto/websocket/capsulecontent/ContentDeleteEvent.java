package cit.edu.mmr.dto.websocket.capsulecontent;

import java.util.UUID;

public class ContentDeleteEvent {
    private final Long capsuleId;
    private final Long contentId;

    private final String eventId;

    public ContentDeleteEvent(Long capsuleId, Long contentId) {
        this.capsuleId = capsuleId;
        this.contentId = contentId;
        this.eventId = UUID.randomUUID().toString();
    }

    public String getEventId() {
        return eventId;
    }

    public Long getCapsuleId() { return capsuleId; }
    public Long getContentId() { return contentId; }
}