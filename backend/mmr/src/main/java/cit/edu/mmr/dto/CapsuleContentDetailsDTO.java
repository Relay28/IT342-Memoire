package cit.edu.mmr.dto;

import java.util.Date;

public class CapsuleContentDetailsDTO {
    private long id;
    private String contentType;
    private Date uploadedAt;
    private String uploadedBy;

    // Constructors
    public CapsuleContentDetailsDTO() {}

    public CapsuleContentDetailsDTO(long id, String contentType, Date uploadedAt, String uploadedBy) {
        this.id = id;
        this.contentType = contentType;
        this.uploadedAt = uploadedAt;
        this.uploadedBy = uploadedBy;
    }

    // Getters and setters
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public Date getUploadedAt() {
        return uploadedAt;
    }

    public void setUploadedAt(Date uploadedAt) {
        this.uploadedAt = uploadedAt;
    }

    public String getUploadedBy() {
        return uploadedBy;
    }

    public void setUploadedBy(String uploadedBy) {
        this.uploadedBy = uploadedBy;
    }
}