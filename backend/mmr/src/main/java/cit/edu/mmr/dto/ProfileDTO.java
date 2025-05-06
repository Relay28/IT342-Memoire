package cit.edu.mmr.dto;

import jakarta.persistence.Lob;

public class ProfileDTO {
    private long userId;
    private String username;
    private String email;
    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getBiography() {
        return biography;
    }

    public void setBiography(String biography) {
        this.biography = biography;
    }

    private String biography;

    @Lob
    private byte[] profilePicture;
    private java.util.Date createdAt;
    private String role;
    private boolean isOauthUser;

    // Getters and setters
    public long getUserId() { return userId; }
    public void setUserId(long userId) { this.userId = userId; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public byte[] getProfilePicture() {
        return profilePicture;
    }

    public void setProfilePicture(byte[] profilePicture) {
        this.profilePicture = profilePicture;
    }

    public java.util.Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(java.util.Date createdAt) { this.createdAt = createdAt; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public boolean isOauthUser() { return isOauthUser; }
    public void setOauthUser(boolean oauthUser) { isOauthUser = oauthUser; }
}
