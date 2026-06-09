<script setup lang="ts">
import { onMounted, ref } from 'vue';
import { Activity, Database, Flag, Layers3, Pin, RefreshCw, ServerCog, Users } from '@lucide/vue';
import { getAdminOverview } from '@/api/community';
import { getHealth } from '@/api/health';
import LoadingState from '@/components/LoadingState.vue';
import type { AdminOverview, HealthStatus } from '@/types/api';

const health = ref<HealthStatus | null>(null);
const overview = ref<AdminOverview | null>(null);
const loading = ref(false);
const errorMessage = ref('');

const modules = [
  { name: '帖子审核', owner: '处理举报、下架违规内容、恢复误判帖子', status: 'live', label: '可管理' },
  { name: '置顶推荐', owner: '把值得阅读的文章固定在更靠前的位置', status: 'live', label: '可管理' },
  { name: '账号状态', owner: '查看账号资料、发帖数、评论数和社区等级', status: 'live', label: '可管理' },
  { name: 'AI 与模型设置', owner: '维护文本、语音和封面生图的模型参数', status: 'live', label: '可管理' }
];

async function loadHealth() {
  loading.value = true;
  errorMessage.value = '';

  try {
    const [healthStatus, communityOverview] = await Promise.all([getHealth(), getAdminOverview()]);
    health.value = healthStatus;
    overview.value = communityOverview;
  } catch (error) {
    errorMessage.value = error instanceof Error ? error.message : '服务状态暂时没取到';
  } finally {
    loading.value = false;
  }
}

onMounted(loadHealth);
</script>

<template>
  <section class="page-section">
    <div class="page-header">
      <div class="section-heading">
        <span>Operations</span>
        <h1>运营看板</h1>
      </div>

      <button class="secondary-button" type="button" :disabled="loading" @click="loadHealth">
        <RefreshCw :size="17" />
        <span>刷新</span>
      </button>
    </div>

    <div class="metric-grid">
      <div class="metric-card">
        <ServerCog :size="20" />
        <span>Web API</span>
        <strong>{{ health?.service || 'studyforge-webapi' }}</strong>
      </div>
      <div class="metric-card">
        <Users :size="20" />
        <span>活跃账号</span>
        <strong>{{ overview ? `${overview.activeUsers}/${overview.totalUsers}` : '...' }}</strong>
      </div>
      <div class="metric-card">
        <Flag :size="20" />
        <span>待处理举报</span>
        <strong>{{ overview?.pendingReports ?? '...' }}</strong>
      </div>
      <div class="metric-card">
        <Pin :size="20" />
        <span>置顶文章</span>
        <strong>{{ overview?.featuredPosts ?? '...' }}</strong>
      </div>
    </div>

    <div class="metric-grid compact-metrics">
      <div class="metric-card">
        <Activity :size="20" />
        <span>接口状态</span>
        <strong>{{ health?.status || (loading ? 'CHECKING' : 'UNKNOWN') }}</strong>
      </div>
      <div class="metric-card">
        <Layers3 :size="20" />
        <span>已发布文章</span>
        <strong>{{ overview?.publishedPosts ?? '...' }}</strong>
      </div>
      <div class="metric-card">
        <Database :size="20" />
        <span>数据库</span>
        <strong>MySQL / MariaDB</strong>
      </div>
      <div class="metric-card">
        <ServerCog :size="20" />
        <span>前端</span>
        <strong>portal-web</strong>
      </div>
    </div>

    <LoadingState v-if="loading" label="正在查看服务状态" />
    <p v-else-if="errorMessage" class="form-error">{{ errorMessage }}</p>

    <div class="table-surface">
      <table>
        <thead>
          <tr>
            <th>区域</th>
            <th>可以管理</th>
            <th>状态</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="item in modules" :key="item.name">
            <td>{{ item.name }}</td>
            <td>{{ item.owner }}</td>
            <td>
              <span class="state-badge" :class="`state-${item.status}`">{{ item.label }}</span>
            </td>
          </tr>
        </tbody>
      </table>
    </div>
  </section>
</template>
