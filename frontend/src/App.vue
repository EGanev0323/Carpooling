<script setup lang="ts">
import { onMounted } from 'vue'
import AppHeader from '@/shared/components/AppHeader.vue'

onMounted(() => {
  document.documentElement.setAttribute('data-theme', 'dark')
})
</script>

<template>
  <div class="app-wrapper">
    <AppHeader />
    <main class="main-content">
      <RouterView />
    </main>
    <Toast position="top-right" />
    <ConfirmDialog />
  </div>
</template>

<style>
.app-wrapper {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

.main-content {
  flex: 1;
  max-width: 1200px;
  margin: 0 auto;
  width: 100%;
  padding: var(--spacing-4);
}
</style>
