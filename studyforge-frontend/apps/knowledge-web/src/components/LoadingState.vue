<template>
  <div class="loading-state" role="status" aria-live="polite">
    <span class="loading-dot" />
    <span>{{ label }}</span>
  </div>
</template>

<script setup lang="ts">
withDefaults(
  defineProps<{
    label?: string;
  }>(),
  {
    label: '加载中'
  }
);
</script>
