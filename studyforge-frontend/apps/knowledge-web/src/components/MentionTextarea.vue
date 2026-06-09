<script setup lang="ts">
import { computed, nextTick, ref } from 'vue';
import { UserRound } from '@lucide/vue';
import { getMyFriends } from '@/api/users';
import { useSessionStore } from '@/stores/session';
import type { SocialUser } from '@/types/api';

defineOptions({
  inheritAttrs: false
});

const props = defineProps<{
  modelValue: string;
}>();

const emit = defineEmits<{
  'update:modelValue': [value: string];
  keydown: [event: KeyboardEvent];
  paste: [event: ClipboardEvent];
  dragenter: [event: DragEvent];
  dragover: [event: DragEvent];
  dragleave: [event: DragEvent];
  drop: [event: DragEvent];
  focus: [event: FocusEvent];
  blur: [event: FocusEvent];
}>();

const sessionStore = useSessionStore();
const textareaRef = ref<HTMLTextAreaElement | null>(null);
const friends = ref<SocialUser[]>([]);
const loadingFriends = ref(false);
const loadedFriends = ref(false);
const mentionStart = ref<number | null>(null);

const visibleFriends = computed(() => friends.value.slice(0, 8));
const showSuggestions = computed(() => mentionStart.value !== null && sessionStore.isAuthenticated && visibleFriends.value.length > 0);

async function loadFriends() {
  if (!sessionStore.isAuthenticated || loadedFriends.value || loadingFriends.value) {
    return;
  }

  loadingFriends.value = true;
  try {
    friends.value = await getMyFriends();
    loadedFriends.value = true;
  } catch {
    friends.value = [];
  } finally {
    loadingFriends.value = false;
  }
}

function triggerStart(value: string, cursor: number) {
  const before = value.slice(0, cursor);
  const atIndex = before.lastIndexOf('@');
  if (atIndex < 0 || atIndex !== cursor - 1) {
    return null;
  }
  if (atIndex > 0 && !/\s/.test(before.charAt(atIndex - 1))) {
    return null;
  }
  return atIndex;
}

function refreshMention(value: string, cursor = textareaRef.value?.selectionStart ?? value.length) {
  const start = triggerStart(value, cursor);
  mentionStart.value = start;
  if (start !== null) {
    void loadFriends();
  }
}

function handleInput(event: Event) {
  const target = event.target as HTMLTextAreaElement;
  emit('update:modelValue', target.value);
  refreshMention(target.value, target.selectionStart);
}

function selectFriend(friend: SocialUser) {
  const textarea = textareaRef.value;
  const start = mentionStart.value;
  if (!textarea || start === null) {
    return;
  }

  const cursor = textarea.selectionStart;
  const mention = `@${friend.username} `;
  const next = props.modelValue.slice(0, start) + mention + props.modelValue.slice(cursor);
  const nextCursor = start + mention.length;
  emit('update:modelValue', next);
  mentionStart.value = null;

  nextTick(() => {
    textarea.focus();
    textarea.setSelectionRange(nextCursor, nextCursor);
  });
}

function handleKeydown(event: KeyboardEvent) {
  if (event.key === 'Escape' && mentionStart.value !== null) {
    mentionStart.value = null;
    event.preventDefault();
  }
  emit('keydown', event);
}

function handleFocus(event: FocusEvent) {
  refreshMention(props.modelValue);
  emit('focus', event);
}

function handleBlur(event: FocusEvent) {
  window.setTimeout(() => {
    mentionStart.value = null;
  }, 120);
  emit('blur', event);
}

function handlePaste(event: ClipboardEvent) {
  emit('paste', event);
}

function handleDragenter(event: DragEvent) {
  emit('dragenter', event);
}

function handleDragover(event: DragEvent) {
  emit('dragover', event);
}

function handleDragleave(event: DragEvent) {
  emit('dragleave', event);
}

function handleDrop(event: DragEvent) {
  emit('drop', event);
}

defineExpose({
  focus: () => textareaRef.value?.focus(),
  setSelectionRange: (start: number, end: number) => textareaRef.value?.setSelectionRange(start, end),
  get selectionStart() {
    return textareaRef.value?.selectionStart ?? 0;
  },
  get selectionEnd() {
    return textareaRef.value?.selectionEnd ?? 0;
  }
});
</script>

<template>
  <div class="mention-textarea">
    <textarea
      ref="textareaRef"
      v-bind="$attrs"
      :value="modelValue"
      @input="handleInput"
      @keydown="handleKeydown"
      @paste="handlePaste"
      @dragenter="handleDragenter"
      @dragover="handleDragover"
      @dragleave="handleDragleave"
      @drop="handleDrop"
      @focus="handleFocus"
      @blur="handleBlur"
    />

    <div v-if="showSuggestions" class="mention-menu">
      <button
        v-for="friend in visibleFriends"
        :key="friend.userId"
        class="mention-option"
        type="button"
        @mousedown.prevent="selectFriend(friend)"
      >
        <span class="mention-avatar">
          <img v-if="friend.avatarUrl" :src="friend.avatarUrl" alt="" />
          <UserRound v-else :size="16" />
        </span>
        <span>
          <strong>{{ friend.displayName }}</strong>
          <small>@{{ friend.username }}</small>
        </span>
      </button>
    </div>
  </div>
</template>
