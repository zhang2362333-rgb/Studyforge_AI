package com.studyforge.system.vo;

import java.time.LocalDateTime;

public record FriendMessageVO(Long messageId,
                              Long senderId,
                              Long receiverId,
                              String senderName,
                              String senderAvatarUrl,
                              String receiverName,
                              String receiverAvatarUrl,
                              String content,
                              boolean read,
                              LocalDateTime createdTime) {
}
