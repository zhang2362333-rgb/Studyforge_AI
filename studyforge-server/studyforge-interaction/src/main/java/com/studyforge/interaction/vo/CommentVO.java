package com.studyforge.interaction.vo;

import java.time.LocalDateTime;

public record CommentVO(Long commentId,
                        Long postId,
                        Long parentCommentId,
                        Long userId,
                        String authorUsername,
                        String authorName,
                        String authorAvatarUrl,
                        Long parentUserId,
                        String parentAuthorUsername,
                        String parentAuthorName,
                        String languageCode,
                        String content,
                        String status,
                        Integer floorNo,
                        Integer likeCount,
                        Boolean likedByViewer,
                        Boolean canDelete,
                        Boolean deleted,
                        LocalDateTime createdTime,
                        LocalDateTime updatedTime) {
}
