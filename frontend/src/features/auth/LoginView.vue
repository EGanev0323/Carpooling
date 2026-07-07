<script setup lang="ts">
import { ref } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { useAuthStore } from '@/stores/auth'
import { useToast } from 'primevue/usetoast'

const router = useRouter()
const route = useRoute()
const { t } = useI18n()
const authStore = useAuthStore()
const toast = useToast()

const form = ref({ email: '', password: '' })
const loading = ref(false)
const error = ref('')

async function handleLogin() {
  if (!form.value.email || !form.value.password) {
    error.value = t('auth.errors.requiredFields')
    return
  }
  loading.value = true
  error.value = ''
  try {
    await authStore.login({ email: form.value.email, password: form.value.password })
    const redirect = (route.query.redirect as string) || '/rides'
    router.push(redirect)
  } catch (err: unknown) {
    const e = err as { response?: { status?: number; data?: { message?: string; error?: string } } }
    if (e.response?.status === 429) {
      error.value = t('auth.errors.tooManyAttempts')
    } else if (e.response?.status === 401) {
      error.value = t('auth.errors.invalidCredentials')
    } else {
      error.value =
        e.response?.data?.message ??
        e.response?.data?.error ??
        t('auth.errors.generic')
    }
    toast.add({
      severity: 'error',
      summary: t('auth.errors.invalidCredentials'),
      detail: error.value,
      life: 4000
    })
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="auth-page">
    <Card class="auth-card">
      <template #title>
        <div class="auth-header">
          <i class="pi pi-sign-in auth-icon" />
          <h2>{{ t('auth.loginTitle') }}</h2>
        </div>
      </template>

      <template #content>
        <form @submit.prevent="handleLogin" class="auth-form">
          <Message v-if="error" severity="error" :closable="false" class="w-full">
            {{ error }}
          </Message>

          <div class="field">
            <label for="email">{{ t('auth.email') }}</label>
            <InputText
              id="email"
              v-model="form.email"
              type="email"
              :placeholder="t('auth.email')"
              class="w-full"
              autocomplete="email"
              autofocus
            />
          </div>

          <div class="field">
            <label for="password">{{ t('auth.password') }}</label>
            <Password
              id="password"
              v-model="form.password"
              :placeholder="t('auth.password')"
              fluid
              :feedback="false"
              toggleMask
              autocomplete="current-password"
            />
          </div>

          <Button
            type="submit"
            :label="t('auth.loginButton')"
            icon="pi pi-sign-in"
            :loading="loading"
            class="w-full"
          />
        </form>
      </template>

      <template #footer>
        <div class="auth-footer">
          <span>{{ t('auth.noAccount') }}</span>
          <RouterLink to="/register" class="auth-link">{{ t('nav.register') }}</RouterLink>
        </div>
      </template>
    </Card>
  </div>
</template>

<style scoped>
.auth-page {
  min-height: calc(100vh - 128px);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: var(--spacing-8) var(--spacing-4);
}

.auth-card {
  width: 100%;
  max-width: 440px;
  box-shadow: 0 4px 24px rgba(0, 0, 0, 0.15);
}

.auth-header {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: var(--spacing-2);
  padding-bottom: var(--spacing-2);
}

.auth-icon {
  font-size: 2.5rem;
  color: var(--mark);
}

.auth-header h2 {
  font-size: var(--fs-h2);
  font-weight: 700;
}

.auth-form {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-6);
}

.field {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-1);
}

.field label {
  font-size: var(--fs-small);
  font-weight: 500;
}

.auth-footer {
  text-align: center;
  font-size: var(--fs-small);
  display: flex;
  gap: var(--spacing-1);
  justify-content: center;
}

.auth-link {
  color: var(--route);
  text-decoration: none;
  font-weight: 500;
}

.auth-link:hover {
  text-decoration: underline;
}
</style>
