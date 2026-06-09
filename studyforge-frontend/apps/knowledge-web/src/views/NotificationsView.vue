<script setup lang="ts">
import { computed, onMounted, ref } from 'vue';
import { RouterLink, useRouter } from 'vue-router';
import {
  ArrowLeft,
  AtSign,
  Bell,
  BookmarkCheck,
  Check,
  CircleHelp,
  Heart,
  MessageCircle,
  MessageSquareReply,
  RefreshCw,
  UserPlus,
  UserRound
} from '@lucide/vue';
import { getNotifications, markAllNotificationsRead, markNotificationRead } from '@/api/notifications';
import EmptyState from '@/components/EmptyState.vue';
import LoadingState from '@/components/LoadingState.vue';
import { usePreferencesStore } from '@/stores/preferences';
import { useSessionStore } from '@/stores/session';
import type { NotificationItem } from '@/types/api';
import { formatDateTime, formatRelativeTime } from '@/utils/date';

type NotificationFilter = 'all' | 'unread';

const router = useRouter();
const preferencesStore = usePreferencesStore();
const sessionStore = useSessionStore();
const notifications = ref<NotificationItem[]>([]);
const activeFilter = ref<NotificationFilter>('all');
const loading = ref(false);
const actionLoading = ref('');
const errorMessage = ref('');

const unreadCount = computed(() => notifications.value.filter((item) => !item.read).length);
const visibleNotifications = computed(() =>
  activeFilter.value === 'unread' ? notifications.value.filter((item) => !item.read) : notifications.value
);

const copy = computed(() => {
  if (preferencesStore.languageCode === 'en_US') {
    return {
      loginTitle: 'Log in to view notifications',
      loginDesc: 'Friend requests, comments, likes, favorites, and answers will appear here.',
      login: 'Log in',
      back: 'Back to profile',
      kicker: 'Inbox',
      title: 'Notifications',
      subtitle: 'Track replies, reactions, and friend requests from the community.',
      refresh: 'Refresh',
      all: 'All',
      unread: 'Unread',
      markAllRead: 'Mark all read',
      markRead: 'Mark read',
      openPost: 'View post',
      openHelp: 'View question',
      openFriends: 'Handle request',
      openProfile: 'View profile',
      emptyAll: 'No notifications yet',
      emptyUnread: 'No unread notifications',
      emptyDesc: 'When someone interacts with your posts, questions, or friend requests, it will show up here.',
      loadError: 'Notifications are unavailable right now'
    };
  }

  return {
    loginTitle: '登录后查看通知',
    loginDesc: '好友申请、评论、点赞、收藏和回答都会出现在这里。',
    login: '登录',
    back: '返回主页',
    kicker: 'Inbox',
    title: '通知',
    subtitle: '社区里与你有关的互动，会按时间显示在这里。',
    refresh: '刷新',
    all: '全部',
    unread: '未读',
    markAllRead: '全部标为已读',
    markRead: '标为已读',
    openPost: '查看帖子',
    openHelp: '查看问题',
    openFriends: '处理申请',
    openProfile: '查看主页',
    emptyAll: '还没有通知',
    emptyUnread: '没有未读通知',
    emptyDesc: '有人评论、点赞、收藏、回答你的内容或发送好友申请时，会在这里提醒你。',
    loadError: '通知暂时没取到'
  };
});

async function loadNotifications() {
  if (!sessionStore.isAuthenticated) {
    notifications.value = [];
    return;
  }

  loading.value = true;
  errorMessage.value = '';

  try {
    notifications.value = await getNotifications(false, 80);
  } catch (error) {
    errorMessage.value = error instanceof Error ? error.message : copy.value.loadError;
  } finally {
    loading.value = false;
  }
}

function notificationIcon(type: string) {
  const icons: Record<string, typeof Bell> = {
    POST_LIKED: Heart,
    POST_FAVORITED: BookmarkCheck,
    POST_COMMENTED: MessageCircle,
    COMMENT_REPLIED: MessageSquareReply,
    COMMENT_LIKED: Heart,
    COMMENT_MENTIONED: AtSign,
    HELP_ANSWERED: CircleHelp,
    HELP_ANSWER_REPLIED: MessageSquareReply,
    HELP_ANSWER_LIKED: Heart,
    HELP_ANSWER_MENTIONED: AtSign,
    FRIEND_REQUEST: UserPlus
  };
  return icons[type] ?? Bell;
}

function notificationTarget(notification: NotificationItem) {
  if (notification.postId) {
    return { path: `/posts/${notification.postId}` };
  }
  if (notification.helpId) {
    return { path: '/help', query: { helpId: notification.helpId } };
  }
  if (notification.notificationType === 'FRIEND_REQUEST') {
    return { path: '/friends' };
  }
  if (notification.actorId) {
    return { path: `/users/${notification.actorId}` };
  }
  return { path: '/me' };
}

function actionLabel(notification: NotificationItem) {
  if (notification.postId) {
    return copy.value.openPost;
  }
  if (notification.helpId) {
    return copy.value.openHelp;
  }
  if (notification.notificationType === 'FRIEND_REQUEST') {
    return copy.value.openFriends;
  }
  return copy.value.openProfile;
}

async function markRead(notification: NotificationItem) {
  if (notification.read) {
    return;
  }

  actionLoading.value = `read-${notification.notificationId}`;
  try {
    await markNotificationRead(notification.notificationId);
    notifications.value = notifications.value.map((item) =>
      item.notificationId === notification.notificationId ? { ...item, read: true } : item
    );
    notifyShell();
  } catch (error) {
    errorMessage.value = error instanceof Error ? error.message : '操作暂时没有成功';
  } finally {
    actionLoading.value = '';
  }
}

async function openNotification(notification: NotificationItem) {
  await markRead(notification);
  await router.push(notificationTarget(notification));
}

async function markAllRead() {
  if (unreadCount.value === 0) {
    return;
  }

  actionLoading.value = 'read-all';
  try {
    await markAllNotificationsRead();
    notifications.value = notifications.value.map((item) => ({ ...item, read: true }));
    notifyShell();
  } catch (error) {
    errorMessage.value = error instanceof Error ? error.message : '操作暂时没有成功';
  } finally {
    actionLoading.value = '';
  }
}

function relativeTime(notification: NotificationItem) {
  return formatRelativeTime(notification.createdTime, preferencesStore.languageCode);
}

function fullTime(notification: NotificationItem) {
  return formatDateTime(notification.createdTime, preferencesStore.languageCode);
}

function notifyShell() {
  window.dispatchEvent(new CustomEvent('studyforge:notifications-updated'));
}

onMounted(loadNotifications);
</script>

<template>
  <section class="notifications-page">
    <div v-if="!sessionStore.isAuthenticated" class="login-required">
      <Bell :size="42" />
      <h2>{{ copy.loginTitle }}</h2>
      <p>{{ copy.loginDesc }}</p>
      <RouterLink class="primary-button" to="/login">{{ copy.login }}</RouterLink>
    </div>

    <template v-else>
      <div class="page-heading with-actions">
        <div>
          <RouterLink class="secondary-button return-link" to="/me">
            <ArrowLeft :size="17" />
            <span>{{ copy.back }}</span>
          </RouterLink>
          <span class="section-kicker">{{ copy.kicker }}</span>
          <h1>{{ copy.title }}</h1>
          <p>{{ copy.subtitle }}</p>
        </div>
        <div class="notification-heading-actions">
          <button class="secondary-button" type="button" :disabled="loading" @click="loadNotifications">
            <RefreshCw :size="17" />
            <span>{{ copy.refresh }}</span>
          </button>
          <button class="primary-button" type="button" :disabled="actionLoading === 'read-all' || unreadCount === 0" @click="markAllRead">
            <Check :size="17" />
            <span>{{ copy.markAllRead }}</span>
          </button>
        </div>
      </div>

      <div class="notification-tabs" aria-label="通知筛选">
        <button type="button" :class="{ active: activeFilter === 'all' }" @click="activeFilter = 'all'">
          {{ copy.all }}
          <span>{{ notifications.length }}</span>
        </button>
        <button type="button" :class="{ active: activeFilter === 'unread' }" @click="activeFilter = 'unread'">
          {{ copy.unread }}
          <span>{{ unreadCount }}</span>
        </button>
      </div>

      <LoadingState v-if="loading" label="正在读取通知" />
      <p v-else-if="errorMessage" class="form-error">{{ errorMessage }}</p>

      <div v-else-if="visibleNotifications.length" class="notification-list">
        <article
          v-for="notification in visibleNotifications"
          :key="notification.notificationId"
          class="notification-card"
          :class="{ unread: !notification.read }"
        >
          <div class="notification-icon">
            <component :is="notificationIcon(notification.notificationType)" :size="18" />
          </div>

          <div class="notification-main">
            <header>
              <div class="notification-actor">
                <img v-if="notification.actorAvatarUrl" :src="notification.actorAvatarUrl" alt="" />
                <UserRound v-else :size="18" />
                <span>{{ notification.actorName || 'StudyForge AI' }}</span>
              </div>
              <time :datetime="fullTime(notification)" :title="fullTime(notification)">
                {{ relativeTime(notification) }}
              </time>
            </header>

            <button class="notification-content" type="button" @click="openNotification(notification)">
              <strong>{{ notification.title }}</strong>
              <span v-if="notification.content">{{ notification.content }}</span>
            </button>

            <footer>
              <button class="secondary-button" type="button" @click="openNotification(notification)">
                {{ actionLabel(notification) }}
              </button>
              <button
                v-if="!notification.read"
                class="ghost-button"
                type="button"
                :disabled="actionLoading === `read-${notification.notificationId}`"
                @click="markRead(notification)"
              >
                {{ copy.markRead }}
              </button>
            </footer>
          </div>
        </article>
      </div>

      <EmptyState
        v-else
        :title="activeFilter === 'unread' ? copy.emptyUnread : copy.emptyAll"
        :description="copy.emptyDesc"
      />
    </template>
  </section>
</template>
