package com.studyforge.system.service.impl;

import com.studyforge.common.exception.BizException;
import com.studyforge.common.exception.ErrorCode;
import com.studyforge.system.entity.Notification;
import com.studyforge.system.mapper.NotificationMapper;
import com.studyforge.system.service.NotificationService;
import com.studyforge.system.vo.NotificationVO;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class NotificationServiceImpl implements NotificationService {
    private final NotificationMapper notificationMapper;

    public NotificationServiceImpl(NotificationMapper notificationMapper) {
        this.notificationMapper = notificationMapper;
    }

    @Override
    public void notifyPostLiked(Long recipientId, Long actorId, Long postId, String postTitle) {
        create(recipientId, actorId, "POST_LIKED", "POST", postId, postId, null, null, null, null,
                "有人点赞了你的帖子", titleOrFallback(postTitle, "查看这篇帖子"));
    }

    @Override
    public void notifyPostFavorited(Long recipientId, Long actorId, Long postId, String postTitle) {
        create(recipientId, actorId, "POST_FAVORITED", "POST", postId, postId, null, null, null, null,
                "有人收藏了你的帖子", titleOrFallback(postTitle, "查看这篇帖子"));
    }

    @Override
    public void notifyPostCommented(Long recipientId, Long actorId, Long postId, Long commentId, String postTitle, String content) {
        create(recipientId, actorId, "POST_COMMENTED", "POST", postId, postId, null, commentId, null, null,
                "有人评论了你的帖子", titleOrFallback(postTitle, preview(content)));
    }

    @Override
    public void notifyPostCommentReplied(Long recipientId, Long actorId, Long postId, Long commentId, String postTitle, String content) {
        create(recipientId, actorId, "COMMENT_REPLIED", "POST", postId, postId, null, commentId, null, null,
                "有人回复了你的评论", titleOrFallback(postTitle, preview(content)));
    }

    @Override
    public void notifyPostCommentLiked(Long recipientId, Long actorId, Long postId, Long commentId, String postTitle) {
        create(recipientId, actorId, "COMMENT_LIKED", "POST", postId, postId, null, commentId, null, null,
                "有人点赞了你的评论", titleOrFallback(postTitle, "查看这条评论"));
    }

    @Override
    public void notifyPostCommentMentioned(Long recipientId, Long actorId, Long postId, Long commentId, String postTitle, String content) {
        create(recipientId, actorId, "COMMENT_MENTIONED", "POST", postId, postId, null, commentId, null, null,
                "有人在评论里提到了你", titleOrFallback(postTitle, preview(content)));
    }

    @Override
    public void notifyHelpAnswered(Long recipientId, Long actorId, Long helpId, Long answerId, String helpTitle, String content) {
        create(recipientId, actorId, "HELP_ANSWERED", "HELP", helpId, null, helpId, null, answerId, null,
                "有人回答了你的问题", titleOrFallback(helpTitle, preview(content)));
    }

    @Override
    public void notifyHelpAnswerReplied(Long recipientId, Long actorId, Long helpId, Long answerId, String helpTitle, String content) {
        create(recipientId, actorId, "HELP_ANSWER_REPLIED", "HELP", helpId, null, helpId, null, answerId, null,
                "有人回复了你的回答", titleOrFallback(helpTitle, preview(content)));
    }

    @Override
    public void notifyHelpAnswerLiked(Long recipientId, Long actorId, Long helpId, Long answerId, String helpTitle) {
        create(recipientId, actorId, "HELP_ANSWER_LIKED", "HELP", helpId, null, helpId, null, answerId, null,
                "有人点赞了你的回答", titleOrFallback(helpTitle, "查看这条回答"));
    }

    @Override
    public void notifyHelpAnswerMentioned(Long recipientId, Long actorId, Long helpId, Long answerId, String helpTitle, String content) {
        create(recipientId, actorId, "HELP_ANSWER_MENTIONED", "HELP", helpId, null, helpId, null, answerId, null,
                "有人在回答里提到了你", titleOrFallback(helpTitle, preview(content)));
    }

    @Override
    public void notifyFriendRequest(Long recipientId, Long actorId, Long friendRequestId, String message) {
        create(recipientId, actorId, "FRIEND_REQUEST", "USER", actorId, null, null, null, null, friendRequestId,
                "有人想加你为好友", preview(message));
    }

    @Override
    public List<NotificationVO> list(Long recipientId, boolean unreadOnly, int limit) {
        requireRecipient(recipientId);
        int normalizedLimit = limit <= 0 ? 30 : Math.min(limit, 80);
        return notificationMapper.selectByRecipient(recipientId, unreadOnly, normalizedLimit)
                .stream()
                .map(this::toVO)
                .toList();
    }

    @Override
    public int unreadCount(Long recipientId) {
        requireRecipient(recipientId);
        return notificationMapper.countUnread(recipientId);
    }

    @Override
    @Transactional
    public void markRead(Long recipientId, Long notificationId) {
        requireRecipient(recipientId);
        if (notificationId == null) {
            throw new BizException(ErrorCode.VALIDATION_ERROR, "notificationId is required");
        }
        notificationMapper.markRead(recipientId, notificationId);
    }

    @Override
    @Transactional
    public void markAllRead(Long recipientId) {
        requireRecipient(recipientId);
        notificationMapper.markAllRead(recipientId);
    }

    private void create(Long recipientId,
                        Long actorId,
                        String notificationType,
                        String targetType,
                        Long targetId,
                        Long postId,
                        Long helpId,
                        Long commentId,
                        Long answerId,
                        Long friendRequestId,
                        String title,
                        String content) {
        if (recipientId == null || recipientId.equals(actorId)) {
            return;
        }

        Notification notification = new Notification();
        notification.setRecipientId(recipientId);
        notification.setActorId(actorId);
        notification.setNotificationType(notificationType);
        notification.setTargetType(targetType);
        notification.setTargetId(targetId);
        notification.setPostId(postId);
        notification.setHelpId(helpId);
        notification.setCommentId(commentId);
        notification.setAnswerId(answerId);
        notification.setFriendRequestId(friendRequestId);
        notification.setTitle(limit(title, 200));
        notification.setContent(limit(content, 500));
        notification.setReadFlag(0);
        notificationMapper.insert(notification);
    }

    private NotificationVO toVO(Map<String, Object> row) {
        return new NotificationVO(
                longValue(row.get("notificationId")),
                longValue(row.get("recipientId")),
                longValue(row.get("actorId")),
                stringValue(row.get("actorName")),
                stringValue(row.get("actorAvatarUrl")),
                stringValue(row.get("notificationType")),
                stringValue(row.get("targetType")),
                longValue(row.get("targetId")),
                longValue(row.get("postId")),
                longValue(row.get("helpId")),
                longValue(row.get("commentId")),
                longValue(row.get("answerId")),
                longValue(row.get("friendRequestId")),
                stringValue(row.get("title")),
                stringValue(row.get("content")),
                intValue(row.get("readFlag")) == 1,
                timeValue(row.get("createdTime")),
                timeValue(row.get("readTime"))
        );
    }

    private void requireRecipient(Long recipientId) {
        if (recipientId == null) {
            throw new BizException(ErrorCode.UNAUTHORIZED);
        }
    }

    private String titleOrFallback(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value.trim();
    }

    private String preview(String value) {
        return value == null || value.isBlank() ? "" : limit(value.trim().replaceAll("\\s+", " "), 140);
    }

    private String limit(String value, int maxLength) {
        if (value == null) {
            return "";
        }
        return value.length() <= maxLength ? value : value.substring(0, maxLength);
    }

    private int intValue(Object value) {
        if (value instanceof Number number) {
            return number.intValue();
        }
        if (value instanceof Boolean bool) {
            return bool ? 1 : 0;
        }
        return value == null ? 0 : Integer.parseInt(value.toString());
    }

    private Long longValue(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Number number) {
            return number.longValue();
        }
        return Long.parseLong(value.toString());
    }

    private String stringValue(Object value) {
        return value == null ? "" : value.toString();
    }

    private LocalDateTime timeValue(Object value) {
        if (value instanceof LocalDateTime time) {
            return time;
        }
        if (value instanceof Timestamp timestamp) {
            return timestamp.toLocalDateTime();
        }
        return null;
    }
}
