package com.studyforge.help.vo;

import java.time.LocalDateTime;

public record HelpAnswerVO(Long answerId,
                           Long helpId,
                           Long parentAnswerId,
                           Long userId,
                           String authorUsername,
                           String authorName,
                           String authorAvatarUrl,
                           Long parentUserId,
                           String parentAuthorUsername,
                           String parentAuthorName,
                           String content,
                           Integer accepted,
                           String status,
                           Integer floorNo,
                           Integer likeCount,
                           Boolean likedByViewer,
                           Boolean canDelete,
                           Boolean deleted,
                           LocalDateTime createdTime,
                           LocalDateTime updatedTime) {
}
