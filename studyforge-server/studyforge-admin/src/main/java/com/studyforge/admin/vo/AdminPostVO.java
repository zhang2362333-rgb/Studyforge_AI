package com.studyforge.admin.vo;

import java.time.LocalDateTime;

public record AdminPostVO(Long postId,
                          Long authorId,
                          String authorName,
                          String title,
                          String summary,
                          String content,
                          String languageCode,
                          String categoryCode,
                          String status,
                          boolean featured,
                          String coverImageUrl,
                          int likeCount,
                          int favoriteCount,
                          int commentCount,
                          int viewCount,
                          double hotScore,
                          LocalDateTime createdTime,
                          LocalDateTime updatedTime) {
}
