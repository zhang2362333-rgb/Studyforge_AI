<script setup lang="ts">
import { computed } from 'vue';
import { RouterLink, useRouter } from 'vue-router';
import {
  BookOpen,
  Database,
  ShieldCheck,
  LayoutDashboard,
  LogOut,
  Settings,
  Server,
  UserRound
} from '@lucide/vue';
import studyforgeLogo from '@/assets/studyforge-logo-mark.png';
import { useAuthStore } from '@/stores/auth';
import { usePreferencesStore } from '@/stores/preferences';

const router = useRouter();
const authStore = useAuthStore();
const preferencesStore = usePreferencesStore();

const currentLanguage = computed({
  get: () => preferencesStore.languageCode,
  set: (value: 'zh_CN' | 'en_US') => preferencesStore.setLanguageCode(value)
});

async function handleLogout() {
  await authStore.logout();
  await router.push('/login');
}
</script>

<template>
  <div class="app-shell">
    <aside class="sidebar">
      <RouterLink to="/feed" class="brand-lockup" aria-label="StudyForge AI">
        <span class="brand-mark">
          <img :src="studyforgeLogo" alt="" />
        </span>
        <span>
          <strong>StudyForge AI</strong>
          <small>Content Console</small>
        </span>
      </RouterLink>

      <nav class="side-nav" aria-label="主导航">
        <RouterLink to="/feed">
          <BookOpen :size="18" />
          <span>内容库</span>
        </RouterLink>
        <RouterLink to="/admin">
          <LayoutDashboard :size="18" />
          <span>运营看板</span>
        </RouterLink>
        <RouterLink to="/community">
          <ShieldCheck :size="18" />
          <span>社区管理</span>
        </RouterLink>
        <RouterLink to="/settings">
          <Settings :size="18" />
          <span>AI 与模型设置</span>
        </RouterLink>
      </nav>

      <div class="stack-panel">
        <div class="stack-row">
          <Server :size="16" />
          <span>接口：Spring MVC JSON</span>
        </div>
        <div class="stack-row">
          <Database :size="16" />
          <span>数据：MyBatis + MySQL</span>
        </div>
      </div>
    </aside>

    <div class="main-panel">
      <header class="topbar">
        <div class="topbar-title">
          <span class="environment-dot" />
          <span>本地工作台</span>
        </div>

        <div class="topbar-actions">
          <label class="select-field" for="language-select">
            <span>语言</span>
            <select id="language-select" v-model="currentLanguage">
              <option value="zh_CN">中文</option>
              <option value="en_US">English</option>
            </select>
          </label>

          <div class="user-chip">
            <UserRound :size="17" />
            <span>{{ authStore.username }}</span>
            <small>{{ authStore.role }}</small>
          </div>

          <button class="icon-text-button" type="button" @click="handleLogout">
            <LogOut :size="17" />
            <span>退出</span>
          </button>
        </div>
      </header>

      <main class="content-area">
        <slot />
      </main>
    </div>
  </div>
</template>
