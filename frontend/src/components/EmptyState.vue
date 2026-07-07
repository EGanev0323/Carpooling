<script setup>
defineProps({
  message: {
    type: String,
    default: 'Няма намерени резултати',
  },
  icon: {
    type: String,
    default: 'pi pi-inbox',
  },
})
</script>

<template>
  <div class="empty-state">
    <i :class="icon" class="empty-icon" />
    <p class="empty-message">{{ message }}</p>
  </div>
</template>

<style scoped>
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 3rem 1rem;
  gap: 1rem;
  text-align: center;
}

.empty-icon {
  font-size: 3rem;
  color: var(--p-text-muted-color);
}

.empty-message {
  color: var(--p-text-muted-color);
  font-size: 1rem;
}
</style>
