<script setup lang="ts">
import { computed, onMounted, ref } from 'vue';
import { ImagePlus, KeyRound, RefreshCw, Save, Volume2 } from '@lucide/vue';
import { getIntegrationSettings, saveIntegrationSettings } from '@/api/settings';
import LoadingState from '@/components/LoadingState.vue';
import type { IntegrationSetting } from '@/types/api';

const settings = ref<IntegrationSetting[]>([]);
const loading = ref(false);
const saving = ref(false);
const errorMessage = ref('');
const savedMessage = ref('');

const aiSettings = computed(() => settings.value.filter((item) => item.settingKey.startsWith('ai.')));
const voiceSettings = computed(() => settings.value.filter((item) => item.settingKey.startsWith('voice.')));
const imageSettings = computed(() => settings.value.filter((item) => item.settingKey.startsWith('image.')));

async function loadSettings() {
  loading.value = true;
  errorMessage.value = '';
  savedMessage.value = '';

  try {
    settings.value = await getIntegrationSettings();
  } catch (error) {
    errorMessage.value = error instanceof Error ? error.message : '设置暂时没取到';
  } finally {
    loading.value = false;
  }
}

async function saveSettings() {
  saving.value = true;
  errorMessage.value = '';
  savedMessage.value = '';

  try {
    await saveIntegrationSettings(settings.value);
    savedMessage.value = '已保存';
    await loadSettings();
  } catch (error) {
    errorMessage.value = error instanceof Error ? error.message : '设置暂时保存不了';
  } finally {
    saving.value = false;
  }
}

function label(key: string) {
  const labels: Record<string, string> = {
    'ai.base_url': 'AI Base URL',
    'ai.api_key': 'AI API Key',
    'ai.chat_model': '文本模型',
    'voice.base_url': '语音 Base URL',
    'voice.api_key': '语音 API Key',
    'voice.model': '语音模型',
    'voice.name': '语音音色',
    'image.base_url': '生图 Base URL',
    'image.api_key': '生图 API Key',
    'image.model': '生图模型',
    'image.size': '封面尺寸'
  };
  return labels[key] ?? key;
}

onMounted(loadSettings);
</script>

<template>
  <section class="page-section">
    <div class="page-header">
      <div class="section-heading">
        <span>Integrations</span>
        <h1>AI、语音与生图设置</h1>
      </div>

      <div class="toolbar">
        <button class="secondary-button" type="button" :disabled="loading" @click="loadSettings">
          <RefreshCw :size="17" />
          <span>刷新</span>
        </button>
        <button class="primary-button" type="button" :disabled="saving" @click="saveSettings">
          <Save :size="17" />
          <span>{{ saving ? '保存中' : '保存' }}</span>
        </button>
      </div>
    </div>

    <LoadingState v-if="loading" label="正在读取设置" />
    <p v-if="errorMessage" class="form-error">{{ errorMessage }}</p>
    <p v-if="savedMessage" class="form-success">{{ savedMessage }}</p>

    <div v-if="!loading" class="settings-grid">
      <section class="settings-panel">
        <div class="settings-panel-title">
          <KeyRound :size="19" />
          <span>文本 AI</span>
        </div>
        <p class="settings-panel-note">用于 AI 摘要、复习卡片、文章问答和 AI 排版。</p>
        <label v-for="setting in aiSettings" :key="setting.settingKey">
          <span>{{ label(setting.settingKey) }}</span>
          <input v-model.trim="setting.settingValue" :type="setting.secretFlag ? 'password' : 'text'" />
        </label>
      </section>

      <section class="settings-panel">
        <div class="settings-panel-title">
          <Volume2 :size="19" />
          <span>语音服务</span>
        </div>
        <p class="settings-panel-note">用于用户侧语音输入、语音记录和学习内容转写。</p>
        <label v-for="setting in voiceSettings" :key="setting.settingKey">
          <span>{{ label(setting.settingKey) }}</span>
          <input v-model.trim="setting.settingValue" :type="setting.secretFlag ? 'password' : 'text'" />
        </label>
      </section>

      <section class="settings-panel">
        <div class="settings-panel-title">
          <ImagePlus :size="19" />
          <span>封面生图</span>
        </div>
        <p class="settings-panel-note">用于用户发布文章时的“生成封面”，会根据标题、摘要和正文生成博客风格封面图。</p>
        <label v-for="setting in imageSettings" :key="setting.settingKey">
          <span>{{ label(setting.settingKey) }}</span>
          <input v-model.trim="setting.settingValue" :type="setting.secretFlag ? 'password' : 'text'" />
        </label>
      </section>
    </div>
  </section>
</template>
