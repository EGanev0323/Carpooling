<script setup lang="ts">
import { useI18n } from 'vue-i18n'

const { t } = useI18n()
</script>

<template>
  <div class="placeholder-page">
    <i class="pi pi-pencil placeholder-icon" />
    <p>{{ t('trips.soon') }}</p>
  </div>
</template>

<style scoped>
.placeholder-page {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 60vh;
  gap: var(--spacing-4);
  color: var(--muted);
}
.placeholder-icon {
  font-size: 4rem;
}
p {
  font-size: var(--fs-h3);
}
</style>
