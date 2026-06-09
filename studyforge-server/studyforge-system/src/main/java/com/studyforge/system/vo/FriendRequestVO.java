package com.studyforge.system.vo;

import java.time.LocalDateTime;

public record FriendRequestVO(Long requestId,
                              SocialUserVO requester,
                              SocialUserVO addressee,
                              String message,
                              String status,
                              LocalDateTime createdTime,
                              LocalDateTime processedTime) {
}
