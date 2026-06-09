package com.studyforge.content.vo;

import java.time.LocalDateTime;

public record PostSummaryVO(Long postId,
                            Long authorId,
                            String authorName,
                            String authorAvatarUrl,
                            String title,
                            String summary,
                            String languageCode,
                            String categoryCode,
                            String coverImageUrl,
                            int likeCount,
                            int favoriteCount,
                            int commentCount,
                            int viewCount,
                            double hotScore,
                            LocalDateTime createdTime,
                            LocalDateTime updatedTime) {
}
