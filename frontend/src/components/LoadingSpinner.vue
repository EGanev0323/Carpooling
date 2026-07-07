<script setup>
defineProps({
  message: {
    type: String,
    default: 'Зарежда се...',
  },
})
</script>

<template>
  <div class="loading-wrapper">
    <ProgressSpinner style="width: 50px; height: 50px" strokeWidth="4" />
    <p class="loading-message">{{ message }}</p>
  </div>
</template>

<style scoped>
.loading-wrapper {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 3rem 1rem;
  gap: 1rem;
}

.loading-message {
  color: var(--p-text-muted-color);
  font-size: 0.9rem;
}
</style>
