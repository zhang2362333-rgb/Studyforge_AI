<script setup lang="ts">
import { computed, onMounted, reactive, ref, watch } from 'vue';
import { RouterLink } from 'vue-router';
import { BookmarkCheck, FolderPlus, LogIn, RefreshCw, Trash2 } from '@lucide/vue';
import { createCollection, getCollectionPosts, getMyCollections, removePostFromCollection } from '@/api/collections';
import { ApiError } from '@/api/http';
import EmptyState from '@/components/EmptyState.vue';
import KnowledgeCard from '@/components/KnowledgeCard.vue';
import LoadingState from '@/components/LoadingState.vue';
import { usePreferencesStore } from '@/stores/preferences';
import { useSessionStore } from '@/stores/session';
import type { FavoriteCollection, PostSummary, TopicCategory } from '@/types/api';

const sessionStore = useSessionStore();
const preferencesStore = usePreferencesStore();

const collections = ref<FavoriteCollection[]>([]);
const posts = ref<PostSummary[]>([]);
const activeCollectionId = ref<number | null>(null);
const loading = ref(false);
const postLoading = ref(false);
const errorMessage = ref('');
const form = reactive({
  name: '',
  description: '',
  visibility: 'PRIVATE' as 'PUBLIC' | 'PRIVATE'
});

const activeCollection = computed(() => collections.value.find((collection) => collection.collectionId === activeCollectionId.value) ?? collections.value[0] ?? null);
const totalSaved = computed(() => collections.value.reduce((total, collection) => total + collection.itemCount, 0));

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

async function loadCollections() {
  if (!sessionStore.isAuthenticated) {
    return;
  }
  loading.value = true;
  errorMessage.value = '';

  try {
    collections.value = await getMyCollections();
    activeCollectionId.value = activeCollectionId.value ?? collections.value[0]?.collectionId ?? null;
    await loadPosts();
  } catch (error) {
    if (error instanceof ApiError && error.code === 4010) {
      await sessionStore.logout();
      errorMessage.value = '登录状态已过期，请重新登录后整理收藏夹。';
      return;
    }
    errorMessage.value = error instanceof Error ? error.message : '收藏夹暂时没取到';
  } finally {
    loading.value = false;
  }
}

async function loadPosts() {
  if (!activeCollection.value) {
    posts.value = [];
    return;
  }

  postLoading.value = true;
  try {
    posts.value = await getCollectionPosts(activeCollection.value.collectionId, preferencesStore.languageCode);
  } catch (error) {
    errorMessage.value = error instanceof Error ? error.message : '这个收藏夹暂时打不开';
  } finally {
    postLoading.value = false;
  }
}

async function selectCollection(collectionId: number) {
  activeCollectionId.value = collectionId;
  await loadPosts();
}

async function submitCollection() {
  if (!form.name.trim()) {
    return;
  }

  try {
    const collection = await createCollection({
      name: form.name.trim(),
      description: form.description.trim(),
      visibility: form.visibility
    });
    collections.value = [...collections.value, collection];
    activeCollectionId.value = collection.collectionId;
    form.name = '';
    form.description = '';
    form.visibility = 'PRIVATE';
    await loadPosts();
  } catch (error) {
    errorMessage.value = error instanceof Error ? error.message : '收藏夹暂时创建不了';
  }
}

async function removePost(postId: number) {
  if (!activeCollection.value) {
    return;
  }
  await removePostFromCollection(activeCollection.value.collectionId, postId);
  posts.value = posts.value.filter((post) => post.postId !== postId);
  collections.value = collections.value.map((collection) =>
    collection.collectionId === activeCollection.value?.collectionId
      ? { ...collection, itemCount: Math.max(0, collection.itemCount - 1) }
      : collection
  );
}

onMounted(loadCollections);

watch(
  () => [sessionStore.isAuthenticated, preferencesStore.languageCode],
  () => loadCollections()
);
</script>

<template>
  <section class="favorites-page">
    <div class="page-heading">
      <span class="section-kicker">Collections</span>
      <h1>收藏夹</h1>
      <p>把值得反复看的文章按主题收好，读完之后也能找得回来。</p>
    </div>

    <div v-if="!sessionStore.isAuthenticated" class="login-required">
      <BookmarkCheck :size="42" />
      <h2>登录后整理收藏夹</h2>
      <p>你可以创建不同主题的收藏夹，把文章按项目、课程或复习计划整理起来。</p>
      <RouterLink class="primary-button" to="/login">
        <LogIn :size="17" />
        <span>登录</span>
      </RouterLink>
    </div>

    <template v-else>
      <section class="favorites-summary">
        <div>
          <BookmarkCheck :size="24" />
          <span>收藏夹</span>
          <strong>{{ collections.length }}</strong>
        </div>
        <div>
          <BookmarkCheck :size="24" />
          <span>已整理文章</span>
          <strong>{{ totalSaved }}</strong>
        </div>
        <button class="secondary-button" type="button" :disabled="loading" @click="loadCollections">
          <RefreshCw :size="17" />
          <span>刷新</span>
        </button>
      </section>

      <LoadingState v-if="loading" label="正在整理收藏夹" />
      <EmptyState v-else-if="errorMessage" title="暂时无法加载" :description="errorMessage" />

      <section v-else class="favorites-layout">
        <aside class="collection-sidebar">
          <div class="panel-title">
            <BookmarkCheck :size="18" />
            <span>我的收藏夹</span>
          </div>
          <button
            v-for="collection in collections"
            :key="collection.collectionId"
            class="collection-item"
            :class="{ active: collection.collectionId === activeCollection?.collectionId }"
            type="button"
            @click="selectCollection(collection.collectionId)"
          >
            <strong>{{ collection.name }}</strong>
            <span>{{ collection.itemCount }} 篇 · {{ collection.visibility === 'PUBLIC' ? '公开' : '私密' }}</span>
            <small>{{ collection.description || '还没有说明。' }}</small>
          </button>

          <form class="collection-form" @submit.prevent="submitCollection">
            <div class="panel-title">
              <FolderPlus :size="18" />
              <span>新建收藏夹</span>
            </div>
            <input v-model.trim="form.name" maxlength="80" placeholder="收藏夹名称" />
            <textarea v-model.trim="form.description" rows="3" maxlength="300" placeholder="简单说明这个收藏夹适合放什么" />
            <select v-model="form.visibility">
              <option value="PRIVATE">私密</option>
              <option value="PUBLIC">公开</option>
            </select>
            <button class="primary-button full-width" type="submit">创建</button>
          </form>
        </aside>

        <main class="collection-posts">
          <div class="feed-toolbar">
            <strong>{{ activeCollection?.name || '收藏夹' }}</strong>
            <span>{{ posts.length }} 篇</span>
          </div>

          <LoadingState v-if="postLoading" label="正在打开收藏夹" />
          <div v-else-if="posts.length" class="collection-post-list">
            <article v-for="(post, index) in posts" :key="post.postId" class="collection-post-card">
              <KnowledgeCard :post="post" :category="categoryFor(post)" :index="index" />
              <button class="secondary-button remove-button" type="button" @click="removePost(post.postId)">
                <Trash2 :size="16" />
                <span>移出</span>
              </button>
            </article>
          </div>
          <EmptyState v-else title="这个收藏夹还没有文章" description="在文章详情页点收藏后，会先进入默认收藏；你也可以在这里继续新建主题收藏夹。" />
        </main>
      </section>
    </template>
  </section>
</template>
