<script setup lang="ts">
import { computed } from 'vue';
import { RouterLink } from 'vue-router';
import { Heart, MessageSquareReply, Trash2, UserRound } from '@lucide/vue';
import MentionTextarea from '@/components/MentionTextarea.vue';
import MarkdownRenderer from '@/components/MarkdownRenderer.vue';

interface ForumThreadNode {
  id: number;
  userId: number;
  authorUsername: string;
  authorName: string;
  authorAvatarUrl: string;
  parentAuthorName: string;
  content: string;
  floorNo: number;
  likeCount: number;
  likedByViewer: boolean;
  canDelete: boolean;
  deleted: boolean;
  accepted?: boolean;
  createdLabel: string;
  replies: ForumThreadNode[];
}

const props = withDefaults(
  defineProps<{
    node: ForumThreadNode;
    replyingToId: number | null;
    replyText: string;
    depth?: number;
    submitLabel?: string;
    replyPlaceholder?: string;
  }>(),
  {
    depth: 0,
    submitLabel: '发送回复',
    replyPlaceholder: '写下回复，输入 @用户名 可以提醒对方'
  }
);

const emit = defineEmits<{
  reply: [node: ForumThreadNode];
  cancelReply: [];
  updateReplyText: [value: string];
  submitReply: [];
  like: [node: ForumThreadNode];
  delete: [node: ForumThreadNode];
}>();

const depthClass = computed(() => `depth-${Math.min(props.depth, 3)}`);

</script>

<template>
  <article class="forum-comment" :class="[depthClass, { deleted: node.deleted, accepted: node.accepted }]">
    <RouterLink class="forum-comment-avatar" :to="`/users/${node.userId}`" :aria-label="node.authorName">
      <img v-if="node.authorAvatarUrl" :src="node.authorAvatarUrl" alt="" />
      <UserRound v-else :size="20" />
    </RouterLink>

    <div class="forum-comment-body">
      <header class="forum-comment-head">
        <div class="forum-comment-author">
          <RouterLink :to="`/users/${node.userId}`">{{ node.authorName }}</RouterLink>
          <span>@{{ node.authorUsername }}</span>
        </div>
        <div class="forum-comment-meta">
          <span>{{ node.floorNo }} 楼</span>
          <span v-if="node.parentAuthorName">回复 {{ node.parentAuthorName }}</span>
          <span v-if="node.accepted" class="accepted-pill">已采纳</span>
          <time v-if="node.createdLabel">{{ node.createdLabel }}</time>
        </div>
      </header>

      <MarkdownRenderer class="comment-markdown forum-comment-markdown" :content="node.content" />

      <footer v-if="!node.deleted" class="forum-comment-actions">
        <button class="ghost-button" :class="{ active: node.likedByViewer }" type="button" @click="emit('like', node)">
          <Heart :size="15" />
          <span>{{ node.likeCount }}</span>
        </button>
        <button class="ghost-button" type="button" @click="emit('reply', node)">
          <MessageSquareReply :size="15" />
          <span>回复</span>
        </button>
        <button v-if="node.canDelete" class="ghost-button danger" type="button" @click="emit('delete', node)">
          <Trash2 :size="15" />
          <span>删除</span>
        </button>
      </footer>

      <form v-if="replyingToId === node.id" class="comment-form forum-reply-form" @submit.prevent="emit('submitReply')">
        <MentionTextarea
          :model-value="replyText"
          rows="3"
          :placeholder="replyPlaceholder"
          @update:model-value="emit('updateReplyText', $event)"
        />
        <div class="forum-reply-actions">
          <button class="secondary-button" type="button" @click="emit('cancelReply')">取消</button>
          <button class="primary-button" type="submit">{{ submitLabel }}</button>
        </div>
      </form>

      <div v-if="node.replies.length" class="forum-replies">
        <ForumThreadItem
          v-for="reply in node.replies"
          :key="reply.id"
          :node="reply"
          :replying-to-id="replyingToId"
          :reply-text="replyText"
          :depth="depth + 1"
          :submit-label="submitLabel"
          :reply-placeholder="replyPlaceholder"
          @reply="emit('reply', $event)"
          @cancel-reply="emit('cancelReply')"
          @update-reply-text="emit('updateReplyText', $event)"
          @submit-reply="emit('submitReply')"
          @like="emit('like', $event)"
          @delete="emit('delete', $event)"
        />
      </div>
    </div>
  </article>
</template>
