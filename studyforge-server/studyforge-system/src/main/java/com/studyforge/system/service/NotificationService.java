package com.studyforge.system.service;

import com.studyforge.system.vo.NotificationVO;
import java.util.List;

public interface NotificationService {
    void notifyPostLiked(Long recipientId, Long actorId, Long postId, String postTitle);

    void notifyPostFavorited(Long recipientId, Long actorId, Long postId, String postTitle);

    void notifyPostCommented(Long recipientId, Long actorId, Long postId, Long commentId, String postTitle, String content);

    void notifyPostCommentReplied(Long recipientId, Long actorId, Long postId, Long commentId, String postTitle, String content);

    void notifyPostCommentLiked(Long recipientId, Long actorId, Long postId, Long commentId, String postTitle);

    void notifyPostCommentMentioned(Long recipientId, Long actorId, Long postId, Long commentId, String postTitle, String content);

    void notifyHelpAnswered(Long recipientId, Long actorId, Long helpId, Long answerId, String helpTitle, String content);

    void notifyHelpAnswerReplied(Long recipientId, Long actorId, Long helpId, Long answerId, String helpTitle, String content);

    void notifyHelpAnswerLiked(Long recipientId, Long actorId, Long helpId, Long answerId, String helpTitle);

    void notifyHelpAnswerMentioned(Long recipientId, Long actorId, Long helpId, Long answerId, String helpTitle, String content);

    void notifyFriendRequest(Long recipientId, Long actorId, Long friendRequestId, String message);

    List<NotificationVO> list(Long recipientId, boolean unreadOnly, int limit);

    int unreadCount(Long recipientId);

    void markRead(Long recipientId, Long notificationId);

    void markAllRead(Long recipientId);
}
