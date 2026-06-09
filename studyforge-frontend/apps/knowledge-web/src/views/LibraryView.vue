<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue';
import { RouterLink } from 'vue-router';
import { BookOpen, BookmarkCheck, Clock3, LogIn, NotebookTabs, RefreshCw } from '@lucide/vue';
import { getMyReviewCards } from '@/api/ai';
import { getFavoritePosts, getHistoryPosts } from '@/api/posts';
import EmptyState from '@/components/EmptyState.vue';
import KnowledgeCard from '@/components/KnowledgeCard.vue';
import LoadingState from '@/components/LoadingState.vue';
import MarkdownRenderer from '@/components/MarkdownRenderer.vue';
import { usePreferencesStore } from '@/stores/preferences';
import { useSessionStore } from '@/stores/session';
import type { AiLogItem, PostSummary, TopicCategory } from '@/types/api';

const sessionStore = useSessionStore();
const preferencesStore = usePreferencesStore();
const favorites = ref<PostSummary[]>([]);
const history = ref<PostSummary[]>([]);
const reviewCards = ref<AiLogItem[]>([]);
const loading = ref(false);
const errorMessage = ref('');
const latestFavorite = computed(() => favorites.value[0] ?? null);
const latestHistory = computed(() => history.value[0] ?? null);
const latestReviewCard = computed(() => reviewCards.value[0] ?? null);
const totalSavedInteractions = computed(() => favorites.value.reduce((sum, post) => sum + post.favoriteCount + post.commentCount, 0));

const category: TopicCategory = {
  code: 'STUDY',
  name: '我的内容',
  description: '已保存',
  accent: '#0f766e'
};

async function loadLibrary() {
  if (!sessionStore.isAuthenticated) {
    return;
  }
  loading.value = true;
  errorMessage.value = '';

  try {
    const [favoriteData, historyData, cardData] = await Promise.all([
      getFavoritePosts(preferencesStore.languageCode),
      getHistoryPosts(preferencesStore.languageCode),
      getMyReviewCards()
    ]);
    favorites.value = favoriteData;
    history.value = historyData;
    reviewCards.value = cardData;
  } catch (error) {
    errorMessage.value = error instanceof Error ? error.message : '学习记录暂时没取到';
  } finally {
    loading.value = false;
  }
}

onMounted(loadLibrary);

watch(
  () => [sessionStore.isAuthenticated, preferencesStore.languageCode],
  () => loadLibrary()
);
</script>

<template>
  <section class="library-page">
    <div class="page-heading">
      <span class="section-kicker">My Study</span>
      <h1>我的学习</h1>
    </div>

    <div v-if="!sessionStore.isAuthenticated" class="login-required">
      <NotebookTabs :size="42" />
      <h2>登录后查看你的学习</h2>
      <p>保存的文章、最近读过的内容和复习卡片都会放在这里。</p>
      <RouterLink class="primary-button" to="/login">
        <LogIn :size="17" />
        <span>登录</span>
      </RouterLink>
    </div>

    <template v-else>
      <div class="library-dashboard">
        <section class="library-focus-panel">
          <div class="focus-copy">
            <span>Continue</span>
            <h2>{{ latestHistory?.title || latestFavorite?.title || '从知识流里挑一篇文章开始' }}</h2>
            <p>{{ latestHistory?.summary || latestFavorite?.summary || '阅读过的内容、收藏的文章和生成的复习卡片会在这里汇总。' }}</p>
          </div>
          <div class="focus-actions">
            <RouterLink
              v-if="latestHistory || latestFavorite"
              class="primary-button"
              :to="{ path: `/posts/${(latestHistory || latestFavorite)?.postId}`, query: { language: (latestHistory || latestFavorite)?.languageCode } }"
            >
              <BookOpen :size="17" />
              <span>继续阅读</span>
            </RouterLink>
            <RouterLink class="secondary-button" to="/favorites">
              <BookmarkCheck :size="17" />
              <span>整理收藏夹</span>
            </RouterLink>
          </div>
        </section>

        <section class="library-stat-card">
          <BookmarkCheck :size="21" />
          <span>收藏文章</span>
          <strong>{{ favorites.length }}</strong>
        </section>
        <section class="library-stat-card">
          <Clock3 :size="21" />
          <span>最近读过</span>
          <strong>{{ history.length }}</strong>
        </section>
        <section class="library-stat-card">
          <NotebookTabs :size="21" />
          <span>复习卡片</span>
          <strong>{{ reviewCards.length }}</strong>
        </section>
      </div>

      <div class="library-workspace">
        <section class="library-board-main">
          <div class="board-header">
            <div>
              <span>Reading Queue</span>
              <h2>学习看板</h2>
            </div>
            <button class="secondary-button" type="button" :disabled="loading" @click="loadLibrary">
              <RefreshCw :size="17" />
              <span>刷新</span>
            </button>
          </div>
          <div class="library-board-grid">
            <article class="library-board-card">
              <span>最新收藏</span>
              <strong>{{ latestFavorite?.title || '还没有收藏文章' }}</strong>
              <p>{{ latestFavorite?.summary || '在文章页点击收藏后，会出现在这里。' }}</p>
            </article>
            <article class="library-board-card">
              <span>最近阅读</span>
              <strong>{{ latestHistory?.title || '还没有阅读记录' }}</strong>
              <p>{{ latestHistory?.summary || '打开文章详情后，会自动记录最近读过。' }}</p>
            </article>
            <article class="library-board-card">
              <span>保存内容带来的互动</span>
              <strong>{{ totalSavedInteractions }}</strong>
              <p>来自你收藏文章的收藏数和讨论数，方便判断哪些内容值得回看。</p>
            </article>
          </div>
        </section>

        <aside class="library-review-panel">
          <div class="panel-title">
            <NotebookTabs :size="18" />
            <span>复习卡片</span>
          </div>
          <MarkdownRenderer
            v-if="latestReviewCard"
            class="review-card-markdown"
            :content="latestReviewCard.responseText"
          />
          <p v-else>在文章详情里生成复习卡片后，最新的一张会显示在这里。</p>
        </aside>
      </div>
    </template>

    <LoadingState v-if="sessionStore.isAuthenticated && loading" label="正在整理学习记录" />
    <EmptyState v-else-if="sessionStore.isAuthenticated && errorMessage" title="暂时无法加载" :description="errorMessage" />

    <section v-if="sessionStore.isAuthenticated && !loading" class="library-sections">
      <div class="library-section">
        <h2>收藏的文章</h2>
        <div v-if="favorites.length" class="knowledge-grid compact-grid">
          <KnowledgeCard v-for="(post, index) in favorites" :key="post.postId" :post="post" :category="category" :index="index" />
        </div>
        <EmptyState v-else title="还没有收藏" description="在文章页点击收藏后，会出现在这里。" />
      </div>

      <div class="library-section">
        <h2>最近读过</h2>
        <div v-if="history.length" class="knowledge-grid compact-grid">
          <KnowledgeCard v-for="(post, index) in history" :key="`history-${post.postId}-${index}`" :post="post" :category="category" :index="index" />
        </div>
        <EmptyState v-else title="还没有阅读记录" description="打开文章详情后，最近读过会自动记录。" />
      </div>

      <div class="library-section">
        <h2>复习卡片</h2>
        <div v-if="reviewCards.length" class="review-card-list">
          <article v-for="card in reviewCards" :key="card.logId" class="review-card-item">
            <span>#{{ card.postId }}</span>
            <MarkdownRenderer class="review-card-markdown" :content="card.responseText" />
          </article>
        </div>
        <EmptyState v-else title="还没有复习卡片" description="在文章详情页生成复习卡片后，会保存到这里。" />
      </div>
    </section>
  </section>
</template>
