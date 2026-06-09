<script setup lang="ts">
import { BookOpenCheck, Layers3 } from '@lucide/vue';
import type { TopicCategory } from '@/types/api';

defineProps<{
  categories: TopicCategory[];
  activeCode: string;
}>();

defineEmits<{
  select: [code: string];
}>();
</script>

<template>
  <aside class="topic-rail" aria-label="按主题浏览">
    <div class="rail-title">
      <Layers3 :size="18" />
      <span>按主题浏览</span>
    </div>

    <button
      v-for="category in categories"
      :key="category.code"
      class="topic-button"
      :class="{ active: category.code === activeCode }"
      type="button"
      :style="{ '--category-color': category.accent }"
      @click="$emit('select', category.code)"
    >
      <BookOpenCheck :size="17" />
      <span>
        <strong>{{ category.name }}</strong>
        <small>{{ category.description }}</small>
      </span>
    </button>
  </aside>
</template>
