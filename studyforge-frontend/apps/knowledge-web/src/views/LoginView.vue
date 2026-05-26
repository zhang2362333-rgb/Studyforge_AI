<script setup lang="ts">
import { computed, reactive, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { LogIn, UserPlus } from '@lucide/vue';
import { ApiError } from '@/api/http';
import { useSessionStore } from '@/stores/session';

const router = useRouter();
const route = useRoute();
const sessionStore = useSessionStore();
const errorMessage = ref('');
const mode = ref<'login' | 'register'>(route.query.mode === 'register' ? 'register' : 'login');

const loginForm = reactive({
  account: '',
  password: ''
});

const registerForm = reactive({
  username: '',
  email: '',
  password: '',
  confirmPassword: ''
});

async function submit() {
  if (mode.value === 'register') {
    await submitRegister();
    return;
  }
  await submitLogin();
}

async function submitLogin() {
  errorMessage.value = '';

  try {
    await sessionStore.login(loginForm);
    await router.push(redirectPath());
  } catch (error) {
    errorMessage.value = error instanceof ApiError ? error.message : '登录失败';
  }
}

async function submitRegister() {
  errorMessage.value = '';

  if (registerForm.password !== registerForm.confirmPassword) {
    errorMessage.value = '两次输入的密码不一致';
    return;
  }
  if (registerForm.password.length < 8) {
    errorMessage.value = '密码至少需要 8 位';
    return;
  }

  try {
    await sessionStore.register({
      username: registerForm.username,
      email: registerForm.email,
      password: registerForm.password
    });
    await router.push(redirectPath());
  } catch (error) {
    errorMessage.value = error instanceof ApiError ? error.message : '注册失败';
  }
}

function switchMode(nextMode: 'login' | 'register') {
  mode.value = nextMode;
  errorMessage.value = '';
}

function redirectPath() {
  return typeof route.query.redirect === 'string' ? route.query.redirect : '/knowledge';
}

const copy = computed(() => {
  if (mode.value === 'register') {
    return {
      kicker: 'Create Account',
      title: '创建你的学习账号',
      desc: '用一个账号保存文章、收藏、复习卡片和社区互动，之后可以继续完善头像和个人主页。',
      submit: sessionStore.loading ? '创建中' : '创建账号',
      switchText: '已经有账号？',
      switchAction: '去登录'
    };
  }

  return {
    kicker: 'StudyForge Account',
    title: '登录后同步你的学习',
    desc: '收藏、阅读记录和复习卡片会跟着账号走，换个设备也能接着看。',
    submit: sessionStore.loading ? '登录中' : '登录',
    switchText: '还没有账号？',
    switchAction: '创建账号'
  };
});
</script>

<template>
  <section class="auth-page">
    <div class="auth-copy">
      <span class="section-kicker">{{ copy.kicker }}</span>
      <h1>{{ copy.title }}</h1>
      <p>{{ copy.desc }}</p>
    </div>

    <form class="auth-card" @submit.prevent="submit">
      <label v-if="mode === 'login'">
        <span>账号</span>
        <input v-model.trim="loginForm.account" autocomplete="username" required />
      </label>

      <label v-if="mode === 'login'">
        <span>密码</span>
        <input v-model="loginForm.password" type="password" autocomplete="current-password" required />
      </label>

      <template v-else>
        <label>
          <span>用户名</span>
          <input
            v-model.trim="registerForm.username"
            autocomplete="username"
            pattern="[A-Za-z0-9_]{3,24}"
            title="3-24 位字母、数字或下划线"
            required
          />
        </label>

        <label>
          <span>邮箱</span>
          <input v-model.trim="registerForm.email" type="email" autocomplete="email" required />
        </label>

        <label>
          <span>密码</span>
          <input v-model="registerForm.password" type="password" autocomplete="new-password" minlength="8" required />
        </label>

        <label>
          <span>确认密码</span>
          <input v-model="registerForm.confirmPassword" type="password" autocomplete="new-password" minlength="8" required />
        </label>
      </template>

      <p v-if="mode === 'register'" class="auth-hint">用户名支持字母、数字和下划线，注册后可以在个人资料里修改昵称和头像。</p>

      <p v-if="errorMessage" class="form-error">{{ errorMessage }}</p>

      <button class="primary-button full-width" type="submit" :disabled="sessionStore.loading">
        <LogIn v-if="mode === 'login'" :size="18" />
        <UserPlus v-else :size="18" />
        <span>{{ copy.submit }}</span>
      </button>

      <div class="auth-switch">
        <span>{{ copy.switchText }}</span>
        <button v-if="mode === 'login'" type="button" @click="switchMode('register')">{{ copy.switchAction }}</button>
        <button v-else type="button" @click="switchMode('login')">{{ copy.switchAction }}</button>
      </div>
    </form>
  </section>
</template>
