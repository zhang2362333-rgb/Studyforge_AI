package com.studyforge.content.vo;

import java.time.LocalDateTime;

public record PostDetailVO(Long postId,
                           Long authorId,
                           String authorName,
                           String authorAvatarUrl,
                           String title,
                           String summary,
                           String content,
                           String languageCode,
                           String categoryCode,
                           String coverImageUrl,
                           String contentFormat,
                           int likeCount,
                           int favoriteCount,
                           int commentCount,
                           int viewCount,
                           double hotScore,
                           LocalDateTime createdTime,
                           LocalDateTime updatedTime) {
}
