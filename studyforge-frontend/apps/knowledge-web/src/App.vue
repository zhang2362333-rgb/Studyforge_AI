<script setup lang="ts">
import { RouterView } from 'vue-router';
import AppErrorBoundary from '@/components/AppErrorBoundary.vue';
import SiteShell from '@/layouts/SiteShell.vue';
</script>

<template>
  <SiteShell>
    <AppErrorBoundary>
      <RouterView />
    </AppErrorBoundary>
  </SiteShell>
</template>
