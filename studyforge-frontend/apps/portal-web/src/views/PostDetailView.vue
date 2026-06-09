<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue';
import { RouterLink, useRoute } from 'vue-router';
import { Archive, ArchiveRestore, ArrowLeft, BadgeCheck, Flame, Pin } from '@lucide/vue';
import { getAdminPostDetail, updatePostFeatured, updatePostStatus } from '@/api/community';
import EmptyState from '@/components/EmptyState.vue';
import LoadingState from '@/components/LoadingState.vue';
import MarkdownRenderer from '@/components/MarkdownRenderer.vue';
import type { AdminPost } from '@/types/api';

const route = useRoute();
const post = ref<AdminPost | null>(null);
const loading = ref(false);
const saving = ref('');
const errorMessage = ref('');
const successMessage = ref('');
const actionRemark = ref('');
const postId = computed(() => String(route.params.postId));

async function loadDetail() {
  loading.value = true;
  errorMessage.value = '';
  successMessage.value = '';

  try {
    post.value = await getAdminPostDetail(postId.value);
  } catch (error) {
    errorMessage.value = error instanceof Error ? error.message : '这篇内容暂时没取到';
  } finally {
    loading.value = false;
  }
}

onMounted(loadDetail);

watch(
  () => postId.value,
  () => loadDetail()
);

async function setFeatured() {
  if (!post.value) {
    return;
  }

  saving.value = 'featured';
  errorMessage.value = '';
  successMessage.value = '';

  try {
    post.value = await updatePostFeatured(post.value.postId, !post.value.featured, actionRemark.value);
    successMessage.value = post.value.featured ? '文章已置顶' : '文章已取消置顶';
  } catch (error) {
    errorMessage.value = error instanceof Error ? error.message : '置顶状态没有更新成功';
  } finally {
    saving.value = '';
  }
}

async function setStatus(status: string) {
  if (!post.value) {
    return;
  }

  saving.value = status;
  errorMessage.value = '';
  successMessage.value = '';

  try {
    post.value = await updatePostStatus(post.value.postId, status, actionRemark.value);
    successMessage.value = status === 'ARCHIVED' ? '文章已下架' : '文章已恢复发布';
  } catch (error) {
    errorMessage.value = error instanceof Error ? error.message : '文章状态没有更新成功';
  } finally {
    saving.value = '';
  }
}

function statusClass(status: string) {
  return `state-${status.toLowerCase()}`;
}
</script>

<template>
  <section class="page-section">
    <div class="page-header">
      <RouterLink class="secondary-button" to="/community">
        <ArrowLeft :size="17" />
        返回社区管理
      </RouterLink>
    </div>

    <LoadingState v-if="loading" label="正在打开文章" />
    <EmptyState v-else-if="errorMessage" title="文章暂时打不开" :description="errorMessage" />

    <article v-else-if="post" class="detail-surface">
      <div class="detail-header">
        <div class="post-meta">
          <span class="score-pill">
            <Flame :size="15" />
            {{ post.hotScore.toFixed(1) }}
          </span>
          <span class="state-badge" :class="statusClass(post.status)">{{ post.status }}</span>
          <span v-if="post.featured" class="state-badge state-featured">置顶</span>
          <span>{{ post.languageCode }}</span>
          <span>{{ post.categoryCode }}</span>
        </div>

        <h1>{{ post.title }}</h1>
        <p>{{ post.summary }}</p>

        <div class="detail-actions">
          <label class="moderation-remark compact">
            <span>处理备注</span>
            <input v-model.trim="actionRemark" type="text" placeholder="写下置顶、下架或恢复的原因" />
          </label>
          <button class="secondary-button" type="button" :disabled="saving === 'featured'" @click="setFeatured">
            <Pin :size="16" />
            <span>{{ post.featured ? '取消置顶' : '置顶文章' }}</span>
          </button>
          <button class="secondary-button danger-action" type="button" :disabled="post.status === 'ARCHIVED' || saving === 'ARCHIVED'" @click="setStatus('ARCHIVED')">
            <Archive :size="16" />
            <span>下架</span>
          </button>
          <button class="secondary-button" type="button" :disabled="post.status === 'PUBLISHED' || saving === 'PUBLISHED'" @click="setStatus('PUBLISHED')">
            <ArchiveRestore :size="16" />
            <span>恢复发布</span>
          </button>
        </div>
        <p v-if="successMessage" class="form-success">{{ successMessage }}</p>
      </div>

      <div class="detail-body">
        <MarkdownRenderer :content="post.content" />
      </div>

      <footer class="detail-footer">
        <span>
          <BadgeCheck :size="16" />
          {{ post.authorName }} · 作者 #{{ post.authorId }}
        </span>
        <span>内容 #{{ post.postId }}</span>
      </footer>
    </article>
  </section>
</template>
