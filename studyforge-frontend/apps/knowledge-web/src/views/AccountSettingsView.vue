<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue';
import { RouterLink } from 'vue-router';
import { ArrowLeft, Image, KeyRound, RefreshCw, Save, UserRound } from '@lucide/vue';
import { uploadImage } from '@/api/uploads';
import { getMyProfile, updateMyPassword, updateMyProfile } from '@/api/users';
import EmptyState from '@/components/EmptyState.vue';
import LoadingState from '@/components/LoadingState.vue';
import { useSessionStore } from '@/stores/session';
import type { UserProfile } from '@/types/api';

const sessionStore = useSessionStore();
const profile = ref<UserProfile | null>(null);
const loading = ref(false);
const saving = ref('');
const errorMessage = ref('');
const successMessage = ref('');

const profileForm = reactive({
  username: '',
  email: '',
  displayName: '',
  bio: '',
  avatarUrl: '',
  bannerUrl: ''
});

const passwordForm = reactive({
  currentPassword: '',
  newPassword: '',
  confirmPassword: ''
});

async function loadProfile() {
  if (!sessionStore.isAuthenticated) {
    return;
  }
  loading.value = true;
  errorMessage.value = '';
  successMessage.value = '';

  try {
    const data = await getMyProfile();
    profile.value = data;
    profileForm.username = data.username;
    profileForm.email = data.email;
    profileForm.displayName = data.displayName;
    profileForm.bio = data.bio;
    profileForm.avatarUrl = data.avatarUrl;
    profileForm.bannerUrl = data.bannerUrl;
  } catch (error) {
    errorMessage.value = error instanceof Error ? error.message : '账号资料暂时没取到';
  } finally {
    loading.value = false;
  }
}

async function saveProfile() {
  saving.value = 'profile';
  errorMessage.value = '';
  successMessage.value = '';

  try {
    const next = await updateMyProfile({ ...profileForm });
    profile.value = next;
    sessionStore.updateFromProfile(next);
    successMessage.value = '资料已保存';
  } catch (error) {
    errorMessage.value = error instanceof Error ? error.message : '资料没有保存成功';
  } finally {
    saving.value = '';
  }
}

async function savePassword() {
  if (passwordForm.newPassword !== passwordForm.confirmPassword) {
    errorMessage.value = '两次输入的新密码不一致';
    return;
  }

  saving.value = 'password';
  errorMessage.value = '';
  successMessage.value = '';

  try {
    const next = await updateMyPassword({
      currentPassword: passwordForm.currentPassword,
      newPassword: passwordForm.newPassword
    });
    sessionStore.updateFromProfile(next);
    passwordForm.currentPassword = '';
    passwordForm.newPassword = '';
    passwordForm.confirmPassword = '';
    successMessage.value = '密码已更新';
  } catch (error) {
    errorMessage.value = error instanceof Error ? error.message : '密码没有更新成功';
  } finally {
    saving.value = '';
  }
}

async function uploadProfileImage(event: Event, field: 'avatarUrl' | 'bannerUrl') {
  const input = event.target as HTMLInputElement;
  const file = input.files?.[0];
  if (!file) {
    return;
  }

  saving.value = field;
  errorMessage.value = '';
  successMessage.value = '';

  try {
    const uploaded = await uploadImage(file);
    profileForm[field] = uploaded.url;
    successMessage.value = field === 'avatarUrl' ? '头像已上传，记得保存资料' : '主页背景已上传，记得保存资料';
  } catch (error) {
    errorMessage.value = error instanceof Error ? error.message : '图片上传失败';
  } finally {
    saving.value = '';
    input.value = '';
  }
}

onMounted(loadProfile);
</script>

<template>
  <section class="account-page">
    <div v-if="!sessionStore.isAuthenticated" class="login-required">
      <UserRound :size="42" />
      <h2>登录后编辑账号</h2>
      <p>登录后可以修改头像、资料、账号邮箱和密码。</p>
      <RouterLink class="primary-button" to="/login">登录</RouterLink>
    </div>

    <template v-else>
      <div class="page-heading with-actions">
        <div>
          <RouterLink class="secondary-button return-link" to="/me">
            <ArrowLeft :size="17" />
            <span>返回主页</span>
          </RouterLink>
          <span>Account</span>
          <h1>账号设置</h1>
        </div>
        <button class="secondary-button" type="button" :disabled="loading" @click="loadProfile">
          <RefreshCw :size="17" />
          <span>刷新</span>
        </button>
      </div>

      <LoadingState v-if="loading" label="正在读取账号资料" />
      <EmptyState v-else-if="errorMessage && !profile" title="账号资料暂时打不开" :description="errorMessage" />

      <div v-else class="account-layout">
        <section class="account-panel">
          <div class="panel-title">
            <UserRound :size="18" />
            <span>个人资料</span>
          </div>

          <div class="profile-preview-strip" :style="{ '--profile-banner': profileForm.bannerUrl ? `url(${profileForm.bannerUrl})` : 'none' }">
            <div class="profile-avatar preview-avatar">
              <img v-if="profileForm.avatarUrl" :src="profileForm.avatarUrl" alt="" />
              <UserRound v-else :size="36" />
            </div>
            <div>
              <strong>{{ profileForm.displayName || profileForm.username }}</strong>
              <span>@{{ profileForm.username }}</span>
            </div>
          </div>

          <form class="account-form" @submit.prevent="saveProfile">
            <label>
              <span>用户名</span>
              <input v-model.trim="profileForm.username" type="text" autocomplete="username" maxlength="50" required />
            </label>
            <label>
              <span>邮箱</span>
              <input v-model.trim="profileForm.email" type="email" autocomplete="email" maxlength="100" required />
            </label>
            <label>
              <span>显示名称</span>
              <input v-model.trim="profileForm.displayName" type="text" maxlength="80" required />
            </label>
            <label>
              <span>个人签名</span>
              <textarea v-model.trim="profileForm.bio" rows="4" maxlength="300" />
            </label>
            <label>
              <span>头像地址</span>
              <input v-model.trim="profileForm.avatarUrl" type="text" maxlength="512" />
            </label>
            <label class="file-upload-line">
              <Image :size="17" />
              <span>{{ saving === 'avatarUrl' ? '头像上传中' : '上传头像' }}</span>
              <input type="file" accept="image/*" :disabled="saving === 'avatarUrl'" @change="uploadProfileImage($event, 'avatarUrl')" />
            </label>
            <label>
              <span>主页背景地址</span>
              <input v-model.trim="profileForm.bannerUrl" type="text" maxlength="512" />
            </label>
            <label class="file-upload-line">
              <Image :size="17" />
              <span>{{ saving === 'bannerUrl' ? '背景上传中' : '上传主页背景' }}</span>
              <input type="file" accept="image/*" :disabled="saving === 'bannerUrl'" @change="uploadProfileImage($event, 'bannerUrl')" />
            </label>
            <button class="primary-button" type="submit" :disabled="saving === 'profile'">
              <Save :size="17" />
              <span>{{ saving === 'profile' ? '保存中' : '保存资料' }}</span>
            </button>
          </form>
        </section>

        <section class="account-panel">
          <div class="panel-title">
            <KeyRound :size="18" />
            <span>修改密码</span>
          </div>
          <form class="account-form" @submit.prevent="savePassword">
            <label>
              <span>当前密码</span>
              <input v-model="passwordForm.currentPassword" type="password" autocomplete="current-password" required />
            </label>
            <label>
              <span>新密码</span>
              <input v-model="passwordForm.newPassword" type="password" autocomplete="new-password" minlength="8" required />
            </label>
            <label>
              <span>确认新密码</span>
              <input v-model="passwordForm.confirmPassword" type="password" autocomplete="new-password" minlength="8" required />
            </label>
            <button class="secondary-button" type="submit" :disabled="saving === 'password'">
              <KeyRound :size="17" />
              <span>{{ saving === 'password' ? '更新中' : '更新密码' }}</span>
            </button>
          </form>

          <p v-if="successMessage" class="form-success">{{ successMessage }}</p>
          <p v-if="errorMessage" class="form-error">{{ errorMessage }}</p>
        </section>
      </div>
    </template>
  </section>
</template>
