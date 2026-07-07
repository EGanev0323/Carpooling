<script setup lang="ts">
import { useI18n } from 'vue-i18n'
import { useRouter } from 'vue-router'

const { t } = useI18n()
const router = useRouter()
</script>

<template>
  <div class="verify-page">
    <Card class="verify-card">
      <template #content>
        <div class="verify-content">
          <i class="pi pi-envelope verify-icon" />
          <h2>{{ t('auth.verifyEmailTitle') }}</h2>
          <p>{{ t('auth.verifyEmailMessage') }}</p>
          <Button
            :label="t('nav.login')"
            icon="pi pi-sign-in"
            @click="router.push('/login')"
          />
        </div>
      </template>
    </Card>
  </div>
</template>

<style scoped>
.verify-page {
  min-height: calc(100vh - 128px);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: var(--spacing-8) var(--spacing-4);
}

.verify-card {
  width: 100%;
  max-width: 440px;
  box-shadow: 0 4px 24px rgba(0, 0, 0, 0.15);
}

.verify-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: var(--spacing-4);
  text-align: center;
  padding: var(--spacing-6) 0;
}

.verify-icon {
  font-size: 3.5rem;
  color: var(--mark);
}

.verify-content h2 {
  font-size: var(--fs-h2);
  font-weight: 700;
}

.verify-content p {
  color: var(--muted);
  line-height: 1.6;
}
</style>
