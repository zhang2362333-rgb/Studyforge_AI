<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue';
import { RouterLink } from 'vue-router';
import { Activity, BadgeCheck, BrainCircuit, Compass, RefreshCw, Search, Sparkles, TrendingUp } from '@lucide/vue';
import { getPosts } from '@/api/posts';
import EmptyState from '@/components/EmptyState.vue';
import KnowledgeCard from '@/components/KnowledgeCard.vue';
import LoadingState from '@/components/LoadingState.vue';
import TopicRail from '@/components/TopicRail.vue';
import { usePreferencesStore } from '@/stores/preferences';
import type { PostSummary, TopicCategory } from '@/types/api';

interface DecoratedPost extends PostSummary {
  category: TopicCategory;
}

interface CategoryInsight {
  name: string;
  count: number;
  percentage: number;
}

const preferencesStore = usePreferencesStore();
const posts = ref<PostSummary[]>([]);
const activeCategory = ref('ALL');
const keyword = ref('');
const loading = ref(false);
const errorMessage = ref('');

const categories = computed<TopicCategory[]>(() => {
  if (preferencesStore.languageCode === 'en_US') {
    return [
      { code: 'ALL', name: 'All', description: 'Fresh picks', accent: '#0f766e' },
      { code: 'TECHNOLOGY', name: 'Technology', description: 'Frontend, backend, tools', accent: '#2563eb' },
      { code: 'BUSINESS', name: 'Business', description: 'Cases and decisions', accent: '#7c3aed' },
      { code: 'PRODUCTIVITY', name: 'Productivity', description: 'Notes and reviews', accent: '#b45309' },
      { code: 'CAREER', name: 'Career', description: 'Interviews and growth', accent: '#15803d' },
      { code: 'FINANCE', name: 'Finance', description: 'Budget and money basics', accent: '#0891b2' }
    ];
  }

  return [
    { code: 'ALL', name: '全部', description: '今天推荐', accent: '#0f766e' },
    { code: 'TECHNOLOGY', name: '技术实践', description: '前端、后端、工具', accent: '#2563eb' },
    { code: 'BUSINESS', name: '商业观察', description: '案例、决策、市场', accent: '#7c3aed' },
    { code: 'PRODUCTIVITY', name: '效率方法', description: '笔记、复盘、计划', accent: '#b45309' },
    { code: 'CAREER', name: '职业成长', description: '求职、面试、成长', accent: '#15803d' },
    { code: 'FINANCE', name: '财务入门', description: '预算、风险、常识', accent: '#0891b2' }
  ];
});

const fallbackCategory = computed(() => categories.value[1]);

const decoratedPosts = computed<DecoratedPost[]>(() =>
  posts.value.map((post) => ({
    ...post,
    category: categories.value.find((category) => category.code === post.categoryCode) ?? fallbackCategory.value
  }))
);

const visiblePosts = computed(() => {
  const normalizedKeyword = keyword.value.trim().toLowerCase();

  return decoratedPosts.value.filter((post) => {
    const matchesCategory = activeCategory.value === 'ALL' || post.category.code === activeCategory.value;
    const matchesKeyword =
      !normalizedKeyword ||
      post.title.toLowerCase().includes(normalizedKeyword) ||
      post.summary.toLowerCase().includes(normalizedKeyword);

    return matchesCategory && matchesKeyword;
  });
});

const activeCategoryName = computed(() => categories.value.find((category) => category.code === activeCategory.value)?.name ?? '全部');

const averageHotScore = computed(() => {
  if (posts.value.length === 0) {
    return '0.0';
  }

  const total = posts.value.reduce((sum, post) => sum + post.hotScore, 0);
  return (total / posts.value.length).toFixed(1);
});

const categoryInsights = computed<CategoryInsight[]>(() => {
  const categoryRows = categories.value
    .filter((category) => category.code !== 'ALL')
    .map((category) => {
      const matchingPosts = posts.value.filter((post) => post.categoryCode === category.code);
      const score = matchingPosts.reduce((sum, post) => sum + post.hotScore, 0);

      return {
        name: category.name,
        count: matchingPosts.length,
        score
      };
    })
    .filter((row) => row.count > 0)
    .sort((first, second) => second.count - first.count || second.score - first.score)
    .slice(0, 3);

  const maxCount = Math.max(1, ...categoryRows.map((row) => row.count));

  return categoryRows.map((row) => ({
    name: row.name,
    count: row.count,
    percentage: Math.max(8, Math.round((row.count / maxCount) * 100))
  }));
});

const copy = computed(() => {
  if (preferencesStore.languageCode === 'en_US') {
    return {
      kicker: 'Study Feed',
      title: 'Worth reading today',
      subtitle: 'Start with one post: skim the summary, filter by topic, open the detail, then ask or discuss when you get stuck.',
      items: 'posts',
      topics: 'topics',
      search: 'Search titles or summaries',
      refresh: 'Refresh',
      write: 'Write',
      loading: 'Loading posts',
      loadErrorTitle: 'Posts are not available',
      emptyTitle: 'No matching posts',
      emptyDescription: 'Try another keyword or topic.',
      rhythm: 'My rhythm',
      rhythmEmpty: 'Topic distribution will appear after the feed loads.',
      assistant: 'AI study helper',
      assistantText: 'Turn long posts into summaries, keywords, and review questions for later recall.',
      cards: 'Review cards'
    };
  }

  return {
    kicker: 'Study Feed',
    title: '今天值得读',
    subtitle: '从一篇文章开始：看摘要、筛主题、进详情，遇到问题再继续讨论。',
    items: '篇内容',
    topics: '个主题',
    search: '搜索标题或摘要',
    refresh: '刷新',
    write: '写一篇',
    loading: '正在整理内容',
    loadErrorTitle: '内容暂时没取到',
    emptyTitle: '没有找到相关内容',
    emptyDescription: '换个关键词或主题再试试',
    rhythm: '我的节奏',
    rhythmEmpty: '知识流加载后会显示当前内容分布。',
    assistant: 'AI 学习助手',
    assistantText: '把长文章整理成摘要、关键词和复习问题，读完后也能快速回顾。',
    cards: '查看复习卡片'
  };
});

async function loadPosts() {
  loading.value = true;
  errorMessage.value = '';

  try {
    posts.value = await getPosts({
      languageCode: preferencesStore.languageCode,
      categoryCode: 'ALL',
      limit: 30
    });
  } catch (error) {
    errorMessage.value = error instanceof Error ? error.message : '内容暂时没取到';
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
  <div class="knowledge-home">
    <TopicRail :categories="categories" :active-code="activeCategory" @select="activeCategory = $event" />

    <section class="content-column">
      <div class="hero-panel">
        <div class="hero-copy">
          <span class="section-kicker">{{ copy.kicker }}</span>
          <h1>{{ copy.title }}</h1>
          <p>{{ copy.subtitle }}</p>

          <div class="hero-badges">
            <span>
              <Activity :size="16" />
              {{ activeCategoryName }}
            </span>
            <span>
              <BadgeCheck :size="16" />
              {{ preferencesStore.languageCode }}
            </span>
            <span>
              <Sparkles :size="16" />
              {{ averageHotScore }}
            </span>
          </div>
        </div>

        <div class="hero-stats">
          <div>
            <strong>{{ posts.length }}</strong>
            <span>{{ copy.items }}</span>
          </div>
          <div>
            <strong>{{ categories.length - 1 }}</strong>
            <span>{{ copy.topics }}</span>
          </div>
        </div>

        <div class="hero-visual" aria-hidden="true">
          <div class="motion-card card-a">
            <Compass :size="18" />
            <span />
            <strong />
          </div>
          <div class="motion-card card-b">
            <TrendingUp :size="18" />
            <span />
            <strong />
          </div>
          <div class="motion-card card-c">
            <BrainCircuit :size="18" />
            <span />
            <strong />
          </div>
        </div>
      </div>

      <div class="feed-toolbar">
        <label class="inline-search" for="feed-search">
          <Search :size="17" />
          <input id="feed-search" v-model.trim="keyword" type="search" :placeholder="copy.search" />
        </label>
        <button class="secondary-button" type="button" :disabled="loading" @click="loadPosts">
          <RefreshCw :size="17" />
          <span>{{ copy.refresh }}</span>
        </button>
        <RouterLink class="primary-button" to="/publish">
          <span>{{ copy.write }}</span>
        </RouterLink>
      </div>

      <LoadingState v-if="loading" :label="copy.loading" />
      <EmptyState v-else-if="errorMessage" :title="copy.loadErrorTitle" :description="errorMessage" />
      <EmptyState v-else-if="visiblePosts.length === 0" :title="copy.emptyTitle" :description="copy.emptyDescription" />

      <div v-else class="knowledge-grid">
        <KnowledgeCard
          v-for="(post, index) in visiblePosts"
          :key="`${post.postId}-${index}-${post.category.code}`"
          :post="post"
          :category="post.category"
          :index="index"
        />
      </div>
    </section>

    <aside class="insight-column">
      <section class="side-panel">
        <div class="panel-title">
          <TrendingUp :size="18" />
          <span>{{ copy.rhythm }}</span>
        </div>
        <div v-if="categoryInsights.length" class="progress-list">
          <div v-for="item in categoryInsights" :key="item.name">
            <span>{{ item.name }} · {{ item.count }}</span>
            <meter min="0" max="100" :value="item.percentage" />
          </div>
        </div>
        <p v-else class="side-note">{{ copy.rhythmEmpty }}</p>
      </section>

      <section class="side-panel assistant-panel">
        <div class="panel-title">
          <BrainCircuit :size="18" />
          <span>{{ copy.assistant }}</span>
        </div>
        <p>{{ copy.assistantText }}</p>
        <RouterLink class="primary-button" to="/library">{{ copy.cards }}</RouterLink>
      </section>
    </aside>
  </div>
</template>
