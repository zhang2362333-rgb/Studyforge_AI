<script setup lang="ts">
import { computed } from 'vue';
import { RouterView, useRoute } from 'vue-router';
import AppErrorBoundary from '@/components/AppErrorBoundary.vue';
import AppShell from '@/layouts/AppShell.vue';

const route = useRoute();
const usesAuthLayout = computed(() => route.meta.layout === 'auth');
</script>

<template>
  <AppErrorBoundary v-if="usesAuthLayout">
    <RouterView />
  </AppErrorBoundary>
  <AppShell v-else>
    <AppErrorBoundary>
      <RouterView />
    </AppErrorBoundary>
  </AppShell>
</template>
