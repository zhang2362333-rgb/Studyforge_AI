<script setup lang="ts">
import { computed, onMounted, ref, watch, type Component } from 'vue';
import { RouterLink } from 'vue-router';
import {
  ArrowRight,
  BadgeCheck,
  BookOpen,
  BrainCircuit,
  CheckCircle2,
  Database,
  Languages,
  Library,
  MessageSquareText,
  Search,
  ShieldCheck,
  Sparkles,
  TrendingUp,
  UsersRound,
  Workflow
} from '@lucide/vue';
import topicAi from '@/assets/topic-ai.svg';
import topicHelp from '@/assets/topic-help.svg';
import topicLearning from '@/assets/topic-learning.svg';
import topicSystems from '@/assets/topic-systems.svg';
import { getTrendingPosts } from '@/api/posts';
import { usePreferencesStore } from '@/stores/preferences';
import type { PostSummary } from '@/types/api';

interface FeatureItem {
  icon: Component;
  title: string;
  description: string;
  meta: string;
  to: string;
  visual: string;
}

interface BoardRow {
  score: string;
  title: string;
  meta: string;
  tag: string;
}

const preferencesStore = usePreferencesStore();
const trendingPosts = ref<PostSummary[]>([]);

const boardRows = computed<BoardRow[]>(() =>
  trendingPosts.value.slice(0, 3).map((post) => {
    const replyLabel = preferencesStore.languageCode === 'en_US' ? 'replies' : '条讨论';
    return {
      score: post.hotScore.toFixed(1),
      title: post.title,
      meta: `${post.categoryCode.toLowerCase()} · ${post.languageCode} · ${post.commentCount} ${replyLabel}`,
      tag: post.categoryCode.toLowerCase()
    };
  })
);

async function loadTrendingRows() {
  try {
    trendingPosts.value = await getTrendingPosts({
      languageCode: preferencesStore.languageCode,
      limit: 3
    });
  } catch {
    trendingPosts.value = [];
  }
}

onMounted(loadTrendingRows);

watch(
  () => preferencesStore.languageCode,
  () => loadTrendingRows()
);

const content = computed(() => {
  if (preferencesStore.languageCode === 'en_US') {
    return {
      kicker: 'StudyForge AI',
      title: 'Read better, remember more, and ask when you get stuck.',
      lead:
        'Find useful articles in the knowledge flow, turn them into AI summaries and review cards, and keep questions moving in discussion.',
      primaryCta: 'Browse knowledge',
      secondaryCta: 'Open my study',
      consoleCta: 'Admin console',
      live: 'Ready to learn',
      stack: 'Vue / Axios / Spring MVC / MyBatis / MySQL',
      boardTitle: 'People are reading',
      boardSubtitle: 'Fresh topics from the study feed.',
      boardEmpty: 'Articles will appear here after the knowledge feed loads.',
      featuresTitle: 'Learn your way',
      featuresLead: 'Start with one article, save what matters, summarize the hard parts, and keep discussing the questions worth asking.',
      architectureTitle: 'System highlights',
      architectureLead:
        'StudyForge keeps the reading experience fast while moving data through JSON APIs, service logic, MyBatis queries, and MySQL storage.',
      roadmapTitle: 'A simple learning loop',
      roadmapLead: 'Pick a topic, read the key points, make a card, and come back when it is time to review.',
      features: [
        {
          icon: BookOpen,
          title: 'Knowledge Flow',
          description: 'Browse today’s useful reads, filter by topic, switch language, and open the full article.',
          meta: 'Discover',
          to: '/knowledge',
          visual: topicSystems
        },
        {
          icon: Library,
          title: 'My Study',
          description: 'Keep saved articles, recent reading, and review cards together in one place.',
          meta: 'Organize',
          to: '/library',
          visual: topicLearning
        },
        {
          icon: BrainCircuit,
          title: 'AI Study Cards',
          description: 'Turn a long article into a short summary, key terms, and questions for later review.',
          meta: 'Summarize',
          to: '/knowledge',
          visual: topicAi
        },
        {
          icon: MessageSquareText,
          title: 'Help & Discussion',
          description: 'Ask about a confusing point, compare answers, and keep the useful explanation.',
          meta: 'Ask',
          to: '/knowledge',
          visual: topicHelp
        }
      ] satisfies FeatureItem[],
      pillars: [
        ['Read', 'Start from articles that match your topic'],
        ['Summarize', 'Ask AI for the short version and key terms'],
        ['Review', 'Come back to cards and saved notes']
      ],
      roadmap: ['Choose a topic from the feed', 'Save the article you want to revisit', 'Generate a summary and review questions', 'Ask the community about the unclear part'],
      signals: [
        ['Topics', 'vue · java · mysql · ai'],
        ['Discussion', 'questions · answers · saved explanations'],
        ['Languages', 'Chinese · English']
      ]
    };
  }

  return {
    kicker: 'StudyForge AI',
    title: '把好内容读懂、记住、问清楚。',
    lead:
      '在知识流里发现值得读的内容，用 AI 提炼摘要和复习卡片，把不懂的问题拿出来讨论。',
    primaryCta: '进入知识流',
    secondaryCta: '查看我的学习',
    consoleCta: '管理控制台',
    live: '开始学习',
    stack: 'Vue / Axios / Spring MVC / MyBatis / MySQL',
    boardTitle: '大家正在读',
    boardSubtitle: '从知识流里挑出的热门主题。',
    boardEmpty: '知识流加载完成后，这里会显示真实文章。',
    featuresTitle: '你可以这样学',
    featuresLead: '从一篇文章开始，保存重点、生成摘要、做成复习卡片，再把没想明白的问题发起讨论。',
    architectureTitle: '系统特色',
    architectureLead:
      'StudyForge 让前端专注阅读体验，数据通过 JSON 接口进入服务层，再由 MyBatis 写入本机 MySQL。',
    roadmapTitle: '一次学习可以很简单',
    roadmapLead: '先选一个主题，再读一篇好内容，最后把它变成可以回看的笔记和问题。',
    features: [
        {
          icon: BookOpen,
          title: '知识流',
          description: '浏览今天值得读的内容，按主题筛选，切换语言，进入完整阅读页。',
          meta: '发现内容',
          to: '/knowledge',
          visual: topicSystems
        },
        {
          icon: Library,
          title: '我的学习',
          description: '把收藏、最近阅读和复习卡片放在一起，回来时能接着学。',
          meta: '整理进度',
          to: '/library',
          visual: topicLearning
        },
        {
          icon: BrainCircuit,
          title: 'AI 学习卡片',
          description: '把长内容整理成摘要、关键词和复习问题，读完也能记得住。',
          meta: '快速提炼',
          to: '/knowledge',
          visual: topicAi
        },
        {
          icon: MessageSquareText,
          title: '求助与讨论',
          description: '遇到看不懂的地方就提问，参考回答，把有用解释留下来。',
          meta: '提问交流',
          to: '/knowledge',
          visual: topicHelp
        }
      ] satisfies FeatureItem[],
    pillars: [
      ['阅读', '从适合自己的主题开始'],
      ['提炼', '让 AI 帮你抓住重点和术语'],
      ['复习', '用卡片和笔记回到关键问题']
    ],
    roadmap: ['从知识流里选一个主题', '保存想反复看的文章', '生成摘要和复习问题', '把没弄懂的点发到讨论里'],
    signals: [
      ['主题', 'vue · java · mysql · ai'],
      ['讨论', '提问 · 回答 · 经验'],
      ['语言', '中文 · English']
    ]
  };
});
</script>

<template>
  <div class="landing-page">
    <section class="landing-hero">
      <div class="hero-scene" aria-hidden="true">
        <div class="scene-topline">
          <span>{{ content.live }}</span>
          <strong>{{ content.stack }}</strong>
        </div>
        <div class="scene-board">
          <div class="scene-board-head">
            <div>
              <span>{{ content.boardTitle }}</span>
              <strong>{{ content.boardSubtitle }}</strong>
            </div>
            <TrendingUp :size="22" />
          </div>
          <div v-for="row in boardRows" :key="row.title" class="scene-row">
            <strong>{{ row.score }}</strong>
            <div>
              <span>{{ row.title }}</span>
              <small>{{ row.meta }}</small>
            </div>
            <em>{{ row.tag }}</em>
          </div>
          <div v-if="boardRows.length === 0" class="scene-row">
            <strong>--</strong>
            <div>
              <span>{{ content.boardEmpty }}</span>
              <small>StudyForge AI</small>
            </div>
            <em>feed</em>
          </div>
        </div>
      </div>

      <div class="landing-hero-copy">
        <span class="section-kicker">{{ content.kicker }}</span>
        <h1>{{ content.title }}</h1>
        <p>{{ content.lead }}</p>
        <div class="landing-actions">
          <RouterLink class="primary-button" to="/knowledge">
            <BookOpen :size="18" />
            <span>{{ content.primaryCta }}</span>
            <ArrowRight :size="18" />
          </RouterLink>
          <RouterLink class="secondary-button" to="/library">
            <Library :size="18" />
            <span>{{ content.secondaryCta }}</span>
          </RouterLink>
          <a class="secondary-button" href="http://localhost:5173">
            <ShieldCheck :size="18" />
            <span>{{ content.consoleCta }}</span>
          </a>
        </div>
      </div>
    </section>

    <section class="feature-section">
      <div class="landing-section-head">
        <span class="section-kicker">{{ content.featuresTitle }}</span>
        <p>{{ content.featuresLead }}</p>
      </div>

      <div class="feature-grid">
        <RouterLink v-for="feature in content.features" :key="feature.title" class="feature-card" :to="feature.to">
          <img class="feature-card-visual" :src="feature.visual" :alt="feature.title" loading="lazy" />
          <div class="feature-card-body">
            <component :is="feature.icon" :size="24" />
            <span>{{ feature.meta }}</span>
            <h2>{{ feature.title }}</h2>
            <p>{{ feature.description }}</p>
            <strong>
              <ArrowRight :size="17" />
            </strong>
          </div>
        </RouterLink>
      </div>
    </section>

    <section class="architecture-band">
      <div>
        <span class="section-kicker">{{ content.architectureTitle }}</span>
        <h2>{{ content.architectureLead }}</h2>
      </div>
      <div class="architecture-flow">
        <div>
          <Sparkles :size="20" />
          <strong>Vue</strong>
          <span>Axios</span>
        </div>
        <Workflow :size="22" />
        <div>
          <BadgeCheck :size="20" />
          <strong>Spring MVC</strong>
          <span>JSON</span>
        </div>
        <Workflow :size="22" />
        <div>
          <Database :size="20" />
          <strong>MyBatis</strong>
          <span>MySQL</span>
        </div>
      </div>
    </section>

    <section class="detail-columns">
      <div class="landing-panel">
        <div class="panel-title">
          <CheckCircle2 :size="18" />
          <span>{{ content.roadmapTitle }}</span>
        </div>
        <p>{{ content.roadmapLead }}</p>
        <ul>
          <li v-for="item in content.roadmap" :key="item">
            <CheckCircle2 :size="16" />
            <span>{{ item }}</span>
          </li>
        </ul>
      </div>

      <div class="landing-panel pillar-panel">
        <div v-for="[title, body] in content.pillars" :key="title">
          <strong>{{ title }}</strong>
          <span>{{ body }}</span>
        </div>
      </div>

      <div class="landing-panel signal-panel">
        <div v-for="([title, body], index) in content.signals" :key="title">
          <Search v-if="index === 0" :size="20" />
          <UsersRound v-else-if="index === 1" :size="20" />
          <Languages v-else :size="20" />
          <strong>{{ title }}</strong>
          <span>{{ body }}</span>
        </div>
      </div>
    </section>
  </div>
</template>
