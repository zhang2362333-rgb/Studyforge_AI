<script setup lang="ts">
import { reactive, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { LogIn } from '@lucide/vue';
import { ApiError } from '@/api/http';
import studyforgeLogo from '@/assets/studyforge-logo-mark.png';
import { useAuthStore } from '@/stores/auth';

const router = useRouter();
const route = useRoute();
const authStore = useAuthStore();
const form = reactive({
  account: 'ops_admin',
  password: 'AdminForge@2026'
});
const errorMessage = ref('');

async function submit() {
  errorMessage.value = '';

  try {
    await authStore.login(form);
    const redirect = typeof route.query.redirect === 'string' ? route.query.redirect : '/feed';
    await router.push(redirect);
  } catch (error) {
    errorMessage.value = error instanceof ApiError ? error.message : '登录失败';
  }
}
</script>

<template>
  <main class="login-page">
    <section class="login-product">
      <div class="brand-lockup auth-brand">
        <span class="brand-mark">
          <img :src="studyforgeLogo" alt="" />
        </span>
        <span>
          <strong>StudyForge AI</strong>
          <small>Content Console</small>
        </span>
      </div>

      <div class="login-copy">
        <h1>StudyForge 内容控制台</h1>
        <p>管理知识内容、查看服务状态，并进入团队的日常运营工作台。</p>
      </div>

      <dl class="login-stack">
        <div>
          <dt>Web App</dt>
          <dd>Vue 3 / Vite / TypeScript</dd>
        </div>
        <div>
          <dt>Requests</dt>
          <dd>Ajax / Fetch / Axios</dd>
        </div>
        <div>
          <dt>Service</dt>
          <dd>Spring MVC / Service / MyBatis / MySQL</dd>
        </div>
      </dl>
    </section>

    <section class="login-card" aria-labelledby="login-title">
      <div class="section-heading compact">
        <span>Portal</span>
        <h2 id="login-title">账号登录</h2>
      </div>

      <form class="form-stack" @submit.prevent="submit">
        <label>
          <span>账号</span>
          <input v-model.trim="form.account" name="account" autocomplete="username" required />
        </label>

        <label>
          <span>密码</span>
          <input v-model="form.password" name="password" type="password" autocomplete="current-password" required />
        </label>

        <p v-if="errorMessage" class="form-error">{{ errorMessage }}</p>

        <button class="primary-button full-width" type="submit" :disabled="authStore.loading">
          <LogIn :size="18" />
          <span>{{ authStore.loading ? '登录中' : '登录' }}</span>
        </button>
      </form>
    </section>
  </main>
</template>
