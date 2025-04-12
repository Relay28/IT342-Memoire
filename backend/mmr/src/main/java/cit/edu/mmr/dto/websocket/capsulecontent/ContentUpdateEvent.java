package cit.edu.mmr.dto.websocket.capsulecontent;

import java.util.Map;
import java.util.UUID;

public class ContentUpdateEvent {
    private Long capsuleId;
    private Map<String, Object> contentMap;
    private final String eventId;
    // constructor, getters

    public ContentUpdateEvent(Long capsuleId, Map<String, Object> contentMap) {
        this.capsuleId = capsuleId;
        this.contentMap = contentMap;
        this.eventId = UUID.randomUUID().toString();
    }

    public String getEventId() {
        return eventId;
    }

    public Long getCapsuleId() {
        return capsuleId;
    }

    public void setCapsuleId(Long capsuleId) {
        this.capsuleId = capsuleId;
    }

    public Map<String, Object> getContentMap() {
        return contentMap;
    }

    public void setContentMap(Map<String, Object> contentMap) {
        this.contentMap = contentMap;
    }
}