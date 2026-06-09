import { http, unwrap } from '@/api/http';
import type { NotificationItem } from '@/types/api';

export function getNotifications(unreadOnly = false, limit = 50) {
  return unwrap<NotificationItem[]>(
    http.get('/notifications', {
      params: {
        unreadOnly,
        limit
      }
    })
  );
}

export function getUnreadNotificationCount() {
  return unwrap<number>(http.get('/notifications/unread-count'));
}

export function markNotificationRead(notificationId: number | string) {
  return unwrap<void>(http.post(`/notifications/${notificationId}/read`));
}

export function markAllNotificationsRead() {
  return unwrap<void>(http.post('/notifications/read-all'));
}
