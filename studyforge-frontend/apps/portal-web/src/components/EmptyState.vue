<template>
  <div class="empty-state">
    <component :is="iconComponent" :size="34" stroke-width="1.8" />
    <p>{{ title }}</p>
    <span v-if="description">{{ description }}</span>
  </div>
</template>

<script setup lang="ts">
import { computed, type Component } from 'vue';
import { Inbox } from '@lucide/vue';

const props = withDefaults(
  defineProps<{
    title: string;
    description?: string;
    icon?: Component;
  }>(),
  {
    description: ''
  }
);

const iconComponent = computed(() => props.icon ?? Inbox);
</script>
