package com.studyforge.interaction.entity;

import java.time.LocalDateTime;

public class Comment {
    private Long commentId;
    private Long postId;
    private Long parentCommentId;
    private Long userId;
    private String languageCode;
    private String content;
    private String status;
    private Integer floorNo;
    private Integer likeCount;
    private LocalDateTime createdTime;
    private LocalDateTime updatedTime;
    private String authorUsername;
    private String authorName;
    private String authorAvatarUrl;
    private Long parentUserId;
    private String parentAuthorUsername;
    private String parentAuthorName;
    private Boolean likedByViewer;

    public Long getCommentId() {
        return commentId;
    }

    public void setCommentId(Long commentId) {
        this.commentId = commentId;
    }

    public Long getPostId() {
        return postId;
    }

    public void setPostId(Long postId) {
        this.postId = postId;
    }

    public Long getParentCommentId() {
        return parentCommentId;
    }

    public void setParentCommentId(Long parentCommentId) {
        this.parentCommentId = parentCommentId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getLanguageCode() {
        return languageCode;
    }

    public void setLanguageCode(String languageCode) {
        this.languageCode = languageCode;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Integer getFloorNo() {
        return floorNo;
    }

    public void setFloorNo(Integer floorNo) {
        this.floorNo = floorNo;
    }

    public Integer getLikeCount() {
        return likeCount;
    }

    public void setLikeCount(Integer likeCount) {
        this.likeCount = likeCount;
    }

    public LocalDateTime getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(LocalDateTime createdTime) {
        this.createdTime = createdTime;
    }

    public LocalDateTime getUpdatedTime() {
        return updatedTime;
    }

    public void setUpdatedTime(LocalDateTime updatedTime) {
        this.updatedTime = updatedTime;
    }

    public String getAuthorUsername() {
        return authorUsername;
    }

    public void setAuthorUsername(String authorUsername) {
        this.authorUsername = authorUsername;
    }

    public String getAuthorName() {
        return authorName;
    }

    public void setAuthorName(String authorName) {
        this.authorName = authorName;
    }

    public String getAuthorAvatarUrl() {
        return authorAvatarUrl;
    }

    public void setAuthorAvatarUrl(String authorAvatarUrl) {
        this.authorAvatarUrl = authorAvatarUrl;
    }

    public Long getParentUserId() {
        return parentUserId;
    }

    public void setParentUserId(Long parentUserId) {
        this.parentUserId = parentUserId;
    }

    public String getParentAuthorUsername() {
        return parentAuthorUsername;
    }

    public void setParentAuthorUsername(String parentAuthorUsername) {
        this.parentAuthorUsername = parentAuthorUsername;
    }

    public String getParentAuthorName() {
        return parentAuthorName;
    }

    public void setParentAuthorName(String parentAuthorName) {
        this.parentAuthorName = parentAuthorName;
    }

    public Boolean getLikedByViewer() {
        return likedByViewer;
    }

    public void setLikedByViewer(Boolean likedByViewer) {
        this.likedByViewer = likedByViewer;
    }
}
