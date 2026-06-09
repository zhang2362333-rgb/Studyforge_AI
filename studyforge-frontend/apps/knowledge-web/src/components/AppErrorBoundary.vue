<script setup lang="ts">
import { onErrorCaptured, ref } from 'vue';
import { AlertTriangle, RefreshCw } from '@lucide/vue';

const errorMessage = ref('');

onErrorCaptured((error) => {
  errorMessage.value = error instanceof Error ? error.message : '页面暂时打不开';
  return false;
});

function reload() {
  window.location.reload();
}
</script>

<template>
  <div v-if="errorMessage" class="error-boundary">
    <AlertTriangle :size="28" />
    <div>
      <h2>页面暂时打不开</h2>
      <p>{{ errorMessage }}</p>
    </div>
    <button class="secondary-button" type="button" @click="reload">
      <RefreshCw :size="17" />
      <span>重新加载</span>
    </button>
  </div>

  <slot v-else />
</template>
