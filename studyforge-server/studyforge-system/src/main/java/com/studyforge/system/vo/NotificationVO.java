package com.studyforge.system.vo;

import java.time.LocalDateTime;

public record NotificationVO(Long notificationId,
                             Long recipientId,
                             Long actorId,
                             String actorName,
                             String actorAvatarUrl,
                             String notificationType,
                             String targetType,
                             Long targetId,
                             Long postId,
                             Long helpId,
                             Long commentId,
                             Long answerId,
                             Long friendRequestId,
                             String title,
                             String content,
                             boolean read,
                             LocalDateTime createdTime,
                             LocalDateTime readTime) {
}
