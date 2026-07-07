<script setup>
import './style.css'
import AppNavbar from './components/AppNavbar.vue'
import AppFooter from './components/AppFooter.vue'
</script>

<template>
  <div class="app-wrapper">
    <AppNavbar />
    <main class="app-main">
      <RouterView />
    </main>
    <AppFooter />
    <Toast position="top-right" />
    <ConfirmDialog />
  </div>
</template>

<style>
* {
  box-sizing: border-box;
}

.app-wrapper {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

.app-main {
  flex: 1;
}
</style>
