package com.studyforge.system.vo;

import java.time.LocalDateTime;

public record UserActivityVO(String activityKey,
                             String activityType,
                             String targetType,
                             Long targetId,
                             Long postId,
                             Long helpId,
                             Long commentId,
                             Long answerId,
                             String title,
                             String summary,
                             String content,
                             String languageCode,
                             String categoryCode,
                             String coverImageUrl,
                             int likeCount,
                             int favoriteCount,
                             int commentCount,
                             int viewCount,
                             LocalDateTime createdTime) {
}
