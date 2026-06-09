package com.studyforge.system.entity;

import com.studyforge.common.enums.RoleType;
import com.studyforge.common.enums.UserStatus;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class User {
    private Long userId;
    private String username;
    private String displayName;
    private String email;
    private String passwordHash;
    private RoleType role;
    private UserStatus status;
    private String bio;
    private String avatarUrl;
    private String bannerUrl;
    private Integer communityLevel;
    private Integer experiencePoints;
    private LocalDate lastLoginRewardDate;
    private Integer reputationScore;
    private LocalDateTime createdTime;

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public RoleType getRole() {
        return role;
    }

    public void setRole(RoleType role) {
        this.role = role;
    }

    public UserStatus getStatus() {
        return status;
    }

    public void setStatus(UserStatus status) {
        this.status = status;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public String getBannerUrl() {
        return bannerUrl;
    }

    public void setBannerUrl(String bannerUrl) {
        this.bannerUrl = bannerUrl;
    }

    public Integer getCommunityLevel() {
        return communityLevel;
    }

    public void setCommunityLevel(Integer communityLevel) {
        this.communityLevel = communityLevel;
    }

    public Integer getExperiencePoints() {
        return experiencePoints;
    }

    public void setExperiencePoints(Integer experiencePoints) {
        this.experiencePoints = experiencePoints;
    }

    public LocalDate getLastLoginRewardDate() {
        return lastLoginRewardDate;
    }

    public void setLastLoginRewardDate(LocalDate lastLoginRewardDate) {
        this.lastLoginRewardDate = lastLoginRewardDate;
    }

    public Integer getReputationScore() {
        return reputationScore;
    }

    public void setReputationScore(Integer reputationScore) {
        this.reputationScore = reputationScore;
    }

    public LocalDateTime getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(LocalDateTime createdTime) {
        this.createdTime = createdTime;
    }
}
