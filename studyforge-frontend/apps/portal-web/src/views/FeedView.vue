<script setup lang="ts">
import { onMounted, ref, watch } from 'vue';
import { RouterLink } from 'vue-router';
import { Flame, RefreshCw, Search } from '@lucide/vue';
import { getTrendingPosts } from '@/api/posts';
import EmptyState from '@/components/EmptyState.vue';
import LoadingState from '@/components/LoadingState.vue';
import { usePreferencesStore } from '@/stores/preferences';
import type { PostSummary } from '@/types/api';

const preferencesStore = usePreferencesStore();
const posts = ref<PostSummary[]>([]);
const loading = ref(false);
const errorMessage = ref('');
const limit = ref(10);

async function loadPosts() {
  loading.value = true;
  errorMessage.value = '';

  try {
    posts.value = await getTrendingPosts({
      languageCode: preferencesStore.languageCode,
      limit: limit.value
    });
  } catch (error) {
    errorMessage.value = error instanceof Error ? error.message : '内容列表暂时没取到';
  } finally {
    loading.value = false;
  }
}

onMounted(loadPosts);

watch(
  () => preferencesStore.languageCode,
  () => loadPosts()
);
</script>

<template>
  <section class="page-section">
    <div class="page-header">
      <div class="section-heading">
        <span>Content Library</span>
        <h1>内容库</h1>
      </div>

      <div class="toolbar">
        <label class="inline-input">
          <Search :size="17" />
          <span>显示数量</span>
          <input v-model.number="limit" type="number" min="1" max="20" @change="loadPosts" />
        </label>
        <button class="secondary-button" type="button" :disabled="loading" @click="loadPosts">
          <RefreshCw :size="17" />
          <span>刷新</span>
        </button>
      </div>
    </div>

    <LoadingState v-if="loading" label="正在整理内容" />
    <EmptyState v-else-if="errorMessage" title="内容暂时没取到" :description="errorMessage" />
    <EmptyState v-else-if="posts.length === 0" title="暂无内容" description="当前语言下还没有可展示的文章" />

    <div v-else class="post-list">
      <article v-for="(post, index) in posts" :key="`${post.postId}-${index}`" class="post-item">
        <div class="post-main">
          <div class="post-meta">
            <span class="score-pill">
              <Flame :size="15" />
              {{ post.hotScore.toFixed(1) }}
            </span>
            <span>{{ post.languageCode }}</span>
          </div>
          <h2>{{ post.title }}</h2>
          <p>{{ post.summary }}</p>
        </div>

        <RouterLink class="secondary-button stable-action" :to="`/posts/${post.postId}`">
          查看
        </RouterLink>
      </article>
    </div>
  </section>
</template>
