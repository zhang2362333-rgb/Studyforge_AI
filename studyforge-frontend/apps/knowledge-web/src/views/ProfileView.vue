<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue';
import { RouterLink, useRoute } from 'vue-router';
import {
  BookOpen,
  BookmarkCheck,
  CircleHelp,
  Eye,
  Heart,
  MessageCircle,
  MessageSquareReply,
  PenLine,
  RefreshCw,
  Settings,
  Star,
  UserCheck,
  UserPlus,
  UserRound,
  Users
} from '@lucide/vue';
import { ApiError } from '@/api/http';
import { followUser, getFriends, getMyProfile, getUserActivities, getUserPosts, getUserProfile, reviewFriendRequest, sendFriendRequest, unfollowUser } from '@/api/users';
import EmptyState from '@/components/EmptyState.vue';
import KnowledgeCard from '@/components/KnowledgeCard.vue';
import LoadingState from '@/components/LoadingState.vue';
import MarkdownRenderer from '@/components/MarkdownRenderer.vue';
import { usePreferencesStore } from '@/stores/preferences';
import { useSessionStore } from '@/stores/session';
import type { PostSummary, SocialUser, TopicCategory, UserActivity, UserProfile } from '@/types/api';
import { formatShortDateTime } from '@/utils/date';

type ProfileTab = 'activity' | 'posts' | 'friends';

const route = useRoute();
const preferencesStore = usePreferencesStore();
const sessionStore = useSessionStore();

const profile = ref<UserProfile | null>(null);
const posts = ref<PostSummary[]>([]);
const activities = ref<UserActivity[]>([]);
const friends = ref<SocialUser[]>([]);
const loading = ref(false);
const actionLoading = ref(false);
const errorMessage = ref('');
const activeTab = ref<ProfileTab>('activity');

const isMeRoute = computed(() => route.name === 'me');
const targetUserId = computed(() => (isMeRoute.value ? sessionStore.userId : Number(route.params.userId)));
const progressPercent = computed(() => {
  if (!profile.value) {
    return 0;
  }
  const currentStart = Math.max(0, (profile.value.communityLevel - 1) * 100);
  const next = Math.max(profile.value.nextLevelExperience, currentStart + 100);
  return Math.min(100, Math.round(((profile.value.experiencePoints - currentStart) / (next - currentStart)) * 100));
});

const categories: Record<string, TopicCategory> = {
  TECHNOLOGY: { code: 'TECHNOLOGY', name: '技术实践', description: '前端、后端、工具', accent: '#2563eb' },
  BUSINESS: { code: 'BUSINESS', name: '商业观察', description: '案例、决策、市场', accent: '#7c3aed' },
  PRODUCTIVITY: { code: 'PRODUCTIVITY', name: '效率方法', description: '笔记、复盘、计划', accent: '#b45309' },
  CAREER: { code: 'CAREER', name: '职业成长', description: '求职、面试、成长', accent: '#15803d' },
  FINANCE: { code: 'FINANCE', name: '财务入门', description: '预算、风险、常识', accent: '#0891b2' }
};

function categoryFor(post: PostSummary): TopicCategory {
  return categories[post.categoryCode] ?? { code: post.categoryCode, name: post.categoryCode, description: '', accent: '#0f766e' };
}

async function loadProfile() {
  if (isMeRoute.value && !sessionStore.isAuthenticated) {
    profile.value = null;
    posts.value = [];
    activities.value = [];
    friends.value = [];
    return;
  }
  if (!targetUserId.value) {
    return;
  }

  loading.value = true;
  errorMessage.value = '';

  try {
    const profileData = isMeRoute.value ? await getMyProfile() : await getUserProfile(targetUserId.value);
    profile.value = profileData;
    const [postData, friendData, activityData] = await Promise.all([
      getUserPosts(profileData.userId, preferencesStore.languageCode),
      getFriends(profileData.userId),
      getUserActivities(profileData.userId, preferencesStore.languageCode)
    ]);
    posts.value = postData;
    friends.value = friendData;
    activities.value = activityData;
  } catch (error) {
    if (error instanceof ApiError && error.code === 4010) {
      await sessionStore.logout();
      errorMessage.value = '登录状态已过期，请重新登录后查看个人主页。';
      return;
    }
    errorMessage.value = error instanceof Error ? error.message : '个人主页暂时打不开';
  } finally {
    loading.value = false;
  }
}

function formatActivityTime(value: UserActivity['createdTime']) {
  return formatShortDateTime(value, preferencesStore.languageCode);
}

function activityMeta(activity: UserActivity) {
  const map: Record<string, { label: string; icon: typeof PenLine; tone: string }> = {
    POST_PUBLISHED: { label: '发布了帖子', icon: PenLine, tone: 'publish' },
    HELP_ASKED: { label: '提出了问题', icon: CircleHelp, tone: 'help' },
    HELP_ANSWERED: { label: '回答了问题', icon: MessageSquareReply, tone: 'answer' },
    COMMENTED: { label: '评论了帖子', icon: MessageCircle, tone: 'comment' },
    LIKED_POST: { label: '点赞了帖子', icon: Heart, tone: 'like' },
    FAVORITED_POST: { label: '收藏了帖子', icon: Star, tone: 'favorite' }
  };
  return map[activity.activityType] ?? { label: '更新了动态', icon: BookOpen, tone: 'default' };
}

function activityTitle(activity: UserActivity) {
  return activity.title || (activity.targetType === 'HELP' ? '查看这个问题' : '查看这篇内容');
}

function categoryLabel(code: string) {
  if (!code) {
    return '';
  }
  return categories[code]?.name ?? code;
}

function languageLabel(code: string) {
  const labels: Record<string, string> = {
    zh_CN: '中文',
    en_US: 'English'
  };
  return labels[code] ?? code;
}

function activityChipText(activity: UserActivity) {
  return [categoryLabel(activity.categoryCode), languageLabel(activity.languageCode)].filter(Boolean).join(' · ');
}

function activityTarget(activity: UserActivity) {
  if (activity.postId) {
    return { path: `/posts/${activity.postId}`, query: { language: activity.languageCode || preferencesStore.languageCode } };
  }
  if (activity.helpId) {
    return { path: '/help', query: { helpId: activity.helpId } };
  }
  return { path: '/knowledge' };
}

async function toggleFollow() {
  if (!profile.value || profile.value.self || actionLoading.value) {
    return;
  }
  if (!sessionStore.isAuthenticated) {
    errorMessage.value = '请先登录再关注用户';
    return;
  }

  actionLoading.value = true;
  try {
    profile.value = profile.value.followedByViewer ? await unfollowUser(profile.value.userId) : await followUser(profile.value.userId);
  } catch (error) {
    errorMessage.value = error instanceof Error ? error.message : '关注操作暂时没有成功';
  } finally {
    actionLoading.value = false;
  }
}

async function requestFriendship() {
  if (!profile.value || profile.value.self || actionLoading.value) {
    return;
  }
  if (!sessionStore.isAuthenticated) {
    errorMessage.value = '请先登录再发送好友申请';
    return;
  }

  actionLoading.value = true;
  try {
    if (profile.value.friendStatus === 'PENDING_RECEIVED' && profile.value.friendRequestId) {
      await reviewFriendRequest(profile.value.friendRequestId, 'ACCEPT');
    } else if (profile.value.friendStatus === 'NONE') {
      await sendFriendRequest(profile.value.userId, '你好，我想和你成为好友，之后可以继续交流学习内容。');
    }
    profile.value = await getUserProfile(profile.value.userId);
    friends.value = await getFriends(profile.value.userId);
  } catch (error) {
    errorMessage.value = error instanceof Error ? error.message : '好友操作暂时没有成功';
  } finally {
    actionLoading.value = false;
  }
}

onMounted(loadProfile);

watch(
  () => [route.fullPath, sessionStore.isAuthenticated, preferencesStore.languageCode],
  () => loadProfile()
);
</script>

<template>
  <section class="profile-page">
    <div v-if="isMeRoute && !sessionStore.isAuthenticated" class="login-required">
      <UserRound :size="42" />
      <h2>登录后查看个人主页</h2>
      <p>你的动态、收藏夹、好友和社区等级都会放在这里。</p>
      <RouterLink class="primary-button" to="/login">登录</RouterLink>
    </div>

    <LoadingState v-else-if="loading" label="正在打开个人主页" />
    <EmptyState v-else-if="errorMessage" title="个人主页暂时打不开" :description="errorMessage" />

    <template v-else-if="profile">
      <section class="profile-hero" :style="{ '--profile-banner': profile.bannerUrl ? `url(${profile.bannerUrl})` : 'none' }">
        <div class="profile-topbar">
          <button class="secondary-button" type="button" :disabled="loading" @click="loadProfile">
            <RefreshCw :size="17" />
            <span>刷新</span>
          </button>
          <RouterLink v-if="profile.self" class="secondary-button" to="/friends">
            <MessageCircle :size="17" />
            <span>好友消息</span>
          </RouterLink>
          <RouterLink v-if="profile.self" class="primary-button" to="/account">
            <Settings :size="17" />
            <span>编辑资料</span>
          </RouterLink>
          <button v-if="!profile.self" class="primary-button" type="button" :disabled="actionLoading" @click="toggleFollow">
            <UserPlus :size="17" />
            <span>{{ profile.followedByViewer ? '已关注' : '关注' }}</span>
          </button>
          <button
            v-if="!profile.self && profile.friendStatus !== 'FRIEND' && profile.friendStatus !== 'PENDING_SENT'"
            class="secondary-button"
            type="button"
            :disabled="actionLoading"
            @click="requestFriendship"
          >
            <UserCheck :size="17" />
            <span>{{ profile.friendStatus === 'PENDING_RECEIVED' ? '通过好友申请' : '加好友' }}</span>
          </button>
          <button v-if="!profile.self && profile.friendStatus === 'PENDING_SENT'" class="secondary-button" type="button" disabled>
            <UserCheck :size="17" />
            <span>申请已发送</span>
          </button>
          <RouterLink v-if="!profile.self && profile.friendStatus === 'FRIEND'" class="secondary-button" to="/friends">
            <MessageCircle :size="17" />
            <span>发消息</span>
          </RouterLink>
        </div>

        <div class="profile-identity">
          <div class="profile-avatar">
            <img v-if="profile.avatarUrl" :src="profile.avatarUrl" alt="" />
            <UserRound v-else :size="42" />
          </div>
          <div>
            <div class="profile-title-row">
              <h1>{{ profile.displayName }}</h1>
              <span class="level-badge">Lv.{{ profile.communityLevel }}</span>
            </div>
            <p class="profile-username">@{{ profile.username }}</p>
            <p class="profile-bio">{{ profile.bio || '这个用户还没有写签名。' }}</p>
          </div>
        </div>

        <div class="profile-level">
          <div>
            <strong>{{ profile.experiencePoints }}</strong>
            <span>经验值</span>
          </div>
          <meter min="0" max="100" :value="progressPercent" />
          <small>距离下一级还差 {{ Math.max(0, profile.nextLevelExperience - profile.experiencePoints) }} 经验</small>
        </div>
      </section>

      <section class="profile-stats">
        <div>
          <strong>{{ profile.friendCount }}</strong>
          <span>好友</span>
        </div>
        <div>
          <strong>{{ profile.followingCount }}</strong>
          <span>关注</span>
        </div>
        <div>
          <strong>{{ profile.followerCount }}</strong>
          <span>粉丝</span>
        </div>
        <div>
          <strong>{{ profile.favoriteCount }}</strong>
          <span>收藏</span>
        </div>
        <div>
          <strong>{{ profile.historyCount }}</strong>
          <span>历史浏览</span>
        </div>
      </section>

      <section v-if="profile.self" class="profile-shortcuts">
        <RouterLink class="profile-shortcut-card" to="/publish">
          <PenLine :size="24" />
          <strong>创作中心</strong>
          <span>发布新的学习内容</span>
        </RouterLink>
        <RouterLink class="profile-shortcut-card" to="/favorites">
          <BookmarkCheck :size="24" />
          <strong>收藏夹</strong>
          <span>按主题整理文章</span>
        </RouterLink>
        <RouterLink class="profile-shortcut-card" to="/friends">
          <MessageCircle :size="24" />
          <strong>好友</strong>
          <span>处理申请和消息</span>
        </RouterLink>
        <RouterLink class="profile-shortcut-card" to="/account">
          <Settings :size="24" />
          <strong>账号设置</strong>
          <span>修改头像、名字和密码</span>
        </RouterLink>
      </section>

      <nav class="profile-tabs" aria-label="个人主页内容">
        <button type="button" :class="{ active: activeTab === 'activity' }" @click="activeTab = 'activity'">动态</button>
        <button type="button" :class="{ active: activeTab === 'posts' }" @click="activeTab = 'posts'">投稿</button>
        <button type="button" :class="{ active: activeTab === 'friends' }" @click="activeTab = 'friends'">好友</button>
      </nav>

      <section v-if="activeTab === 'activity'" class="profile-content-grid">
        <div class="activity-timeline">
          <article v-for="activity in activities" :key="activity.activityKey" class="timeline-item" :class="`tone-${activityMeta(activity).tone}`">
            <div class="timeline-rail">
              <component :is="activityMeta(activity).icon" :size="18" />
            </div>

            <div class="timeline-card">
              <header class="timeline-header">
                <div class="activity-author">
                  <img v-if="profile.avatarUrl" :src="profile.avatarUrl" alt="" />
                  <UserRound v-else :size="18" />
                  <div>
                    <strong>{{ profile.displayName }}</strong>
                    <span>{{ activityMeta(activity).label }} · {{ formatActivityTime(activity.createdTime) }}</span>
                  </div>
                </div>
                <span v-if="activityChipText(activity)" class="timeline-chip">{{ activityChipText(activity) }}</span>
              </header>

              <RouterLink class="timeline-title" :to="activityTarget(activity)">
                {{ activityTitle(activity) }}
              </RouterLink>

              <div v-if="activity.content" class="timeline-quote">
                <MarkdownRenderer :content="activity.content" />
              </div>
              <p v-else class="timeline-summary">{{ activity.summary }}</p>

              <img v-if="activity.coverImageUrl" class="activity-cover" :src="activity.coverImageUrl" alt="" loading="lazy" />

              <footer v-if="activity.postId" class="timeline-stats">
                <span><Heart :size="15" />{{ activity.likeCount }}</span>
                <span><BookmarkCheck :size="15" />{{ activity.favoriteCount }}</span>
                <span><MessageCircle :size="15" />{{ activity.commentCount }}</span>
                <span><Eye :size="15" />{{ activity.viewCount }}</span>
                <RouterLink :to="activityTarget(activity)">打开</RouterLink>
              </footer>
              <footer v-else class="timeline-stats">
                <RouterLink :to="activityTarget(activity)">查看求助</RouterLink>
              </footer>
            </div>
          </article>
          <EmptyState v-if="activities.length === 0" title="还没有动态" description="发帖、提问、回答、评论、点赞和收藏后，会在这里形成动态。" />
        </div>

        <aside class="profile-side-panel">
          <h2>社区表现</h2>
          <dl>
            <div>
              <dt>发布文章</dt>
              <dd>{{ profile.postCount }}</dd>
            </div>
            <div>
              <dt>收到点赞</dt>
              <dd>{{ profile.receivedLikeCount }}</dd>
            </div>
            <div>
              <dt>参与讨论</dt>
              <dd>{{ profile.commentCount }}</dd>
            </div>
            <div>
              <dt>声望</dt>
              <dd>{{ profile.reputationScore }}</dd>
            </div>
          </dl>
        </aside>
      </section>

      <section v-else-if="activeTab === 'posts'" class="knowledge-grid compact-grid">
        <KnowledgeCard v-for="(post, index) in posts" :key="post.postId" :post="post" :category="categoryFor(post)" :index="index" />
        <EmptyState v-if="posts.length === 0" title="还没有投稿" description="发布后的文章会展示在这里。" />
      </section>

      <section v-else class="social-grid">
        <article v-for="user in friends" :key="user.userId" class="social-card">
          <img v-if="user.avatarUrl" :src="user.avatarUrl" alt="" />
          <UserRound v-else :size="24" />
          <div>
            <strong>{{ user.displayName }}</strong>
            <span>@{{ user.username }} · Lv.{{ user.communityLevel }}</span>
            <p>{{ user.bio }}</p>
          </div>
          <RouterLink class="secondary-button stable-action" :to="`/users/${user.userId}`">
            <Users :size="16" />
            <span>主页</span>
          </RouterLink>
        </article>
        <EmptyState v-if="friends.length === 0" title="还没有好友" description="通过好友申请后，好友会显示在这里。关注和粉丝不会混到好友列表里。" />
      </section>
    </template>
  </section>
</template>
