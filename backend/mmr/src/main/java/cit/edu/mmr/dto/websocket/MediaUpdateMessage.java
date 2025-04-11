//package cit.edu.mmr.dto.websocket;
//
//import lombok.Data;
//
//import java.util.Map;
//
//@Data
//
//public class MediaUpdateMessage {
//    private String type; // NEW_MEDIA, DELETE_MEDIA, UPDATE_MEDIA
//    private Long capsuleId;
//    private Long contentId;
//    private String contentType;
//    private String username;
//    private long timestamp;
//    private Map<String, Object> additionalData;
//
//    public Map<String, Object> getAdditionalData() {
//        return additionalData;
//    }
//
//    public void setAdditionalData(Map<String, Object> additionalData) {
//        this.additionalData = additionalData;
//    }
//
//    public String getType() {
//        return type;
//    }
//
//    public void setType(String type) {
//        this.type = type;
//    }
//
//    public Long getCapsuleId() {
//        return capsuleId;
//    }
//
//    public void setCapsuleId(Long capsuleId) {
//        this.capsuleId = capsuleId;
//    }
//
//    public Long getContentId() {
//        return contentId;
//    }
//
//    public void setContentId(Long contentId) {
//        this.contentId = contentId;
//    }
//
//    public String getContentType() {
//        return contentType;
//    }
//
//    public void setContentType(String contentType) {
//        this.contentType = contentType;
//    }
//
//    public String getUsername() {
//        return username;
//    }
//
//    public void setUsername(String username) {
//        this.username = username;
//    }
//
//    public long getTimestamp() {
//        return timestamp;
//    }
//
//    public void setTimestamp(long timestamp) {
//        this.timestamp = timestamp;
//    }
//}
