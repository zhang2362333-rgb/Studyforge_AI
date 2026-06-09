package com.studyforge.admin.vo;

import java.time.LocalDateTime;

public record AdminUserVO(Long userId,
                          String username,
                          String displayName,
                          String email,
                          String role,
                          String status,
                          int communityLevel,
                          int experiencePoints,
                          int reputationScore,
                          int postCount,
                          int commentCount,
                          int favoriteCount,
                          int followerCount,
                          LocalDateTime createdTime) {
}
