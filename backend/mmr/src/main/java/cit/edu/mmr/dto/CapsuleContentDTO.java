package cit.edu.mmr.dto;


import java.util.Date;

public class CapsuleContentDTO {
    private long id;
    private Long contentUploadedById;
    private String contentType;
    private Date uploadedAt;

    // Constructors
    public CapsuleContentDTO() {}

    // Getters and setters
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Long getContentUploadedById() {
        return contentUploadedById;
    }

    public void setContentUploadedById(Long contentUploadedById) {
        this.contentUploadedById = contentUploadedById;
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
}