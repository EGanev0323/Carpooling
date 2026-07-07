<script setup>
import { useRouter } from 'vue-router'

const router = useRouter()
</script>

<template>
  <div class="not-found-page">
    <div class="not-found-inner">
      <div class="error-code">404</div>
      <h1>Страницата не е намерена</h1>
      <p>Съжаляваме, но страницата, която търсите, не съществува.</p>
      <div class="actions">
        <Button
          label="Към началната страница"
          icon="pi pi-home"
          @click="router.push('/')"
        />
        <Button
          label="Назад"
          icon="pi pi-arrow-left"
          text
          @click="router.back()"
        />
      </div>
    </div>
  </div>
</template>

<style scoped>
.not-found-page {
  min-height: calc(100vh - 128px);
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--p-surface-50);
  padding: 2rem;
}

.not-found-inner {
  text-align: center;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 1rem;
  max-width: 480px;
}

.error-code {
  font-size: 8rem;
  font-weight: 800;
  color: var(--p-primary-color);
  line-height: 1;
  opacity: 0.3;
}

h1 {
  font-size: 1.75rem;
  font-weight: 700;
}

p {
  color: var(--p-text-muted-color);
  font-size: 1rem;
}

.actions {
  display: flex;
  gap: 1rem;
  margin-top: 0.5rem;
  flex-wrap: wrap;
  justify-content: center;
}
</style>
