package com.studyforge.system.vo;

public record SocialUserVO(Long userId,
                           String username,
                           String displayName,
                           String avatarUrl,
                           int communityLevel,
                           String bio,
                           boolean followedByViewer) {
}
