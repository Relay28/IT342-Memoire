package cit.edu.mmr.dto;

import cit.edu.mmr.entity.CapsuleContentEntity;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class TimeCapsuleDTO {

    private Long id;
    private String title;
    private String description;
    private Date createdAt;
    private Date openDate;
    private boolean isLocked;
    private Long createdById;
    private String status;
    private List<CapsuleContentDTO> contents = new ArrayList<>();  // Changed to use CapsuleContentDTO

    public List<CapsuleContentDTO> getContents() {  // Updated return type
        return contents;
    }

    public void setContents(List<CapsuleContentDTO> contents) {  // Updated parameter type
        this.contents = contents;
    }



    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Date getOpenDate() {
        return openDate;
    }

    public void setOpenDate(Date openDate) {
        this.openDate = openDate;
    }

    public boolean isLocked() {
        return isLocked;
    }

    public void setLocked(boolean locked) {
        isLocked = locked;
    }

    public Long getCreatedById() {
        return createdById;
    }

    public void setCreatedById(Long createdById) {
        this.createdById = createdById;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
