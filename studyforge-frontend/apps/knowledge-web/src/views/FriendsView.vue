<script setup lang="ts">
import { computed, onMounted, ref } from 'vue';
import { RouterLink } from 'vue-router';
import { ArrowLeft, Check, Inbox, MessageCircle, RefreshCw, Send, UserRound, X } from '@lucide/vue';
import {
  getFriendMessages,
  getIncomingFriendRequests,
  getMyFriends,
  getOutgoingFriendRequests,
  reviewFriendRequest,
  sendFriendMessage
} from '@/api/users';
import EmptyState from '@/components/EmptyState.vue';
import LoadingState from '@/components/LoadingState.vue';
import { usePreferencesStore } from '@/stores/preferences';
import { useSessionStore } from '@/stores/session';
import type { FriendMessage, FriendRequest, SocialUser } from '@/types/api';
import { formatShortDateTime } from '@/utils/date';

const sessionStore = useSessionStore();
const preferencesStore = usePreferencesStore();
const friends = ref<SocialUser[]>([]);
const incoming = ref<FriendRequest[]>([]);
const outgoing = ref<FriendRequest[]>([]);
const messages = ref<FriendMessage[]>([]);
const activeFriend = ref<SocialUser | null>(null);
const draftMessage = ref('');
const activePanel = ref<'messages' | 'requests'>('messages');
const loading = ref(false);
const actionLoading = ref('');
const errorMessage = ref('');
const successMessage = ref('');

const pendingIncoming = computed(() => incoming.value.filter((item) => item.status === 'PENDING'));
const pendingOutgoing = computed(() => outgoing.value.filter((item) => item.status === 'PENDING'));

async function loadFriends() {
  if (!sessionStore.isAuthenticated) {
    return;
  }
  loading.value = true;
  errorMessage.value = '';
  successMessage.value = '';

  try {
    const [friendData, incomingData, outgoingData] = await Promise.all([
      getMyFriends(),
      getIncomingFriendRequests('ALL'),
      getOutgoingFriendRequests('ALL')
    ]);
    friends.value = friendData;
    incoming.value = incomingData;
    outgoing.value = outgoingData;
    if (!activeFriend.value && friendData.length) {
      await openChat(friendData[0]);
    } else if (activeFriend.value) {
      const stillFriend = friendData.find((friend) => friend.userId === activeFriend.value?.userId);
      activeFriend.value = stillFriend ?? null;
      if (stillFriend) {
        await loadMessages(stillFriend.userId);
      } else {
        messages.value = [];
      }
    }
  } catch (error) {
    errorMessage.value = error instanceof Error ? error.message : '好友数据暂时没取到';
  } finally {
    loading.value = false;
  }
}

async function openChat(friend: SocialUser) {
  activePanel.value = 'messages';
  activeFriend.value = friend;
  await loadMessages(friend.userId);
}

async function loadMessages(friendId: number) {
  try {
    messages.value = await getFriendMessages(friendId);
  } catch (error) {
    errorMessage.value = error instanceof Error ? error.message : '消息暂时没取到';
  }
}

async function handleRequest(request: FriendRequest, decision: 'ACCEPT' | 'REJECT') {
  actionLoading.value = `${decision}-${request.requestId}`;
  errorMessage.value = '';
  successMessage.value = '';

  try {
    await reviewFriendRequest(request.requestId, decision);
    successMessage.value = decision === 'ACCEPT' ? '已通过好友申请' : '已拒绝好友申请';
    await loadFriends();
  } catch (error) {
    errorMessage.value = error instanceof Error ? error.message : '好友申请处理失败';
  } finally {
    actionLoading.value = '';
  }
}

async function sendMessage() {
  const content = draftMessage.value.trim();
  if (!activeFriend.value || !content) {
    return;
  }

  actionLoading.value = 'message';
  errorMessage.value = '';
  successMessage.value = '';

  try {
    const message = await sendFriendMessage(activeFriend.value.userId, content);
    messages.value = [...messages.value, message];
    draftMessage.value = '';
  } catch (error) {
    errorMessage.value = error instanceof Error ? error.message : '消息没有发送成功';
  } finally {
    actionLoading.value = '';
  }
}

function formatDate(value: unknown) {
  return formatShortDateTime(value, preferencesStore.languageCode);
}

onMounted(loadFriends);
</script>

<template>
  <section class="friends-page">
    <div v-if="!sessionStore.isAuthenticated" class="login-required">
      <UserRound :size="42" />
      <h2>登录后使用好友</h2>
      <p>好友申请和私信需要登录后使用。</p>
      <RouterLink class="primary-button" to="/login">登录</RouterLink>
    </div>

    <template v-else>
      <div class="page-heading with-actions">
        <div>
          <RouterLink class="secondary-button return-link" to="/me">
            <ArrowLeft :size="17" />
            <span>返回主页</span>
          </RouterLink>
          <span>Friends</span>
          <h1>好友</h1>
        </div>
        <button class="secondary-button" type="button" :disabled="loading" @click="loadFriends">
          <RefreshCw :size="17" />
          <span>刷新</span>
        </button>
      </div>

      <LoadingState v-if="loading" label="正在读取好友和消息" />
      <p v-else-if="errorMessage" class="form-error">{{ errorMessage }}</p>
      <p v-if="successMessage" class="form-success">{{ successMessage }}</p>

      <div class="friend-workspace">
        <aside class="friend-sidebar">
          <div class="friend-tabs">
            <button type="button" :class="{ active: activePanel === 'messages' }" @click="activePanel = 'messages'">
              <MessageCircle :size="17" />
              <span>消息</span>
            </button>
            <button type="button" :class="{ active: activePanel === 'requests' }" @click="activePanel = 'requests'">
              <Inbox :size="17" />
              <span>申请 {{ pendingIncoming.length }}</span>
            </button>
          </div>

          <div v-if="activePanel === 'messages'" class="friend-list">
            <button
              v-for="friend in friends"
              :key="friend.userId"
              type="button"
              :class="{ active: activeFriend?.userId === friend.userId }"
              @click="openChat(friend)"
            >
              <img v-if="friend.avatarUrl" :src="friend.avatarUrl" alt="" />
              <UserRound v-else :size="20" />
              <span>
                <strong>{{ friend.displayName }}</strong>
                <small>@{{ friend.username }} · Lv.{{ friend.communityLevel }}</small>
              </span>
            </button>
            <EmptyState v-if="friends.length === 0" title="还没有好友" description="在用户主页发送好友申请，通过后会出现在这里。" />
          </div>

          <div v-else class="request-list compact">
            <article v-for="request in pendingIncoming" :key="request.requestId" class="request-card">
              <div class="request-user">
                <img v-if="request.requester.avatarUrl" :src="request.requester.avatarUrl" alt="" />
                <UserRound v-else :size="20" />
                <div>
                  <strong>{{ request.requester.displayName }}</strong>
                  <span>@{{ request.requester.username }}</span>
                </div>
              </div>
              <p>{{ request.message || '想添加你为好友。' }}</p>
              <div class="request-actions">
                <button class="primary-button" type="button" :disabled="actionLoading === `ACCEPT-${request.requestId}`" @click="handleRequest(request, 'ACCEPT')">
                  <Check :size="16" />
                  <span>通过</span>
                </button>
                <button class="secondary-button" type="button" :disabled="actionLoading === `REJECT-${request.requestId}`" @click="handleRequest(request, 'REJECT')">
                  <X :size="16" />
                  <span>拒绝</span>
                </button>
              </div>
            </article>

            <article v-for="request in pendingOutgoing" :key="`out-${request.requestId}`" class="request-card muted">
              <div class="request-user">
                <img v-if="request.addressee.avatarUrl" :src="request.addressee.avatarUrl" alt="" />
                <UserRound v-else :size="20" />
                <div>
                  <strong>{{ request.addressee.displayName }}</strong>
                  <span>@{{ request.addressee.username }}</span>
                </div>
              </div>
              <p>等待对方通过：{{ request.message || '已发送好友申请。' }}</p>
            </article>

            <EmptyState v-if="pendingIncoming.length === 0 && pendingOutgoing.length === 0" title="没有待处理申请" description="新的好友申请会出现在这里。" />
          </div>
        </aside>

        <section class="chat-panel">
          <template v-if="activeFriend">
            <header class="chat-header">
              <div class="request-user">
                <img v-if="activeFriend.avatarUrl" :src="activeFriend.avatarUrl" alt="" />
                <UserRound v-else :size="22" />
                <div>
                  <strong>{{ activeFriend.displayName }}</strong>
                  <span>@{{ activeFriend.username }}</span>
                </div>
              </div>
              <RouterLink class="secondary-button" :to="`/users/${activeFriend.userId}`">主页</RouterLink>
            </header>

            <div class="message-list">
              <article v-for="message in messages" :key="message.messageId" class="message-bubble" :class="{ mine: message.senderId === sessionStore.userId }">
                <p>{{ message.content }}</p>
                <span>{{ formatDate(message.createdTime) }}</span>
              </article>
              <EmptyState v-if="messages.length === 0" title="还没有消息" description="发送第一条消息开始交流。" />
            </div>

            <form class="message-compose" @submit.prevent="sendMessage">
              <textarea v-model.trim="draftMessage" rows="3" maxlength="2000" placeholder="写一条消息" />
              <button class="primary-button" type="submit" :disabled="actionLoading === 'message'">
                <Send :size="17" />
                <span>发送</span>
              </button>
            </form>
          </template>

          <EmptyState v-else title="选择一位好友" description="通过好友申请后，就可以在这里发送消息。" />
        </section>
      </div>
    </template>
  </section>
</template>
