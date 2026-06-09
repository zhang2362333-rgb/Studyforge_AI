package com.studyforge.system.mapper;

import com.studyforge.system.entity.Notification;
import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.Param;

public interface NotificationMapper {
    int insert(Notification notification);

    List<Map<String, Object>> selectByRecipient(@Param("recipientId") Long recipientId,
                                                @Param("unreadOnly") boolean unreadOnly,
                                                @Param("limit") int limit);

    int countUnread(@Param("recipientId") Long recipientId);

    int markRead(@Param("recipientId") Long recipientId, @Param("notificationId") Long notificationId);

    int markAllRead(@Param("recipientId") Long recipientId);
}
