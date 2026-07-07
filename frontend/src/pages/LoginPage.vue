<script setup>
import { ref } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useAuthStore } from '../stores/auth.store'
import { useToast } from 'primevue/usetoast'

const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()
const toast = useToast()

const form = ref({ username: '', password: '' })
const loading = ref(false)
const error = ref('')

async function handleLogin() {
  if (!form.value.username || !form.value.password) {
    error.value = 'Моля, попълнете всички полета'
    return
  }
  loading.value = true
  error.value = ''
  try {
    await authStore.login(form.value)
    const redirect = route.query.redirect || '/'
    router.push(redirect)
  } catch (err) {
    error.value =
      err.response?.data?.message ||
      err.response?.data?.error ||
      'Грешно потребителско име или парола'
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
          <h2>Вход в системата</h2>
        </div>
      </template>

      <template #content>
        <form @submit.prevent="handleLogin" class="auth-form">
          <Message v-if="error" severity="error" :closable="false" class="w-full">
            {{ error }}
          </Message>

          <div class="field">
            <label for="username">Потребителско име</label>
            <InputText
              id="username"
              v-model="form.username"
              placeholder="Вашето потребителско име"
              class="w-full"
              autocomplete="username"
              autofocus
            />
          </div>

          <div class="field">
            <label for="password">Парола</label>
            <Password
              id="password"
              v-model="form.password"
              placeholder="Вашата парола"
              fluid
              :feedback="false"
              toggleMask
              autocomplete="current-password"
            />
          </div>

          <Button
            type="submit"
            label="Влез"
            icon="pi pi-sign-in"
            :loading="loading"
            class="w-full"
          />
        </form>
      </template>

      <template #footer>
        <div class="auth-footer">
          <span>Нямате акаунт?</span>
          <RouterLink to="/register" class="auth-link">Регистрирайте се</RouterLink>
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
  padding: 2rem 1rem;
  background: var(--p-surface-50);
}

.auth-card {
  width: 100%;
  max-width: 440px;
  box-shadow: 0 4px 24px rgba(0, 0, 0, 0.1);
}

.auth-header {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 0.5rem;
  padding-bottom: 0.5rem;
}

.auth-icon {
  font-size: 2.5rem;
  color: var(--p-primary-color);
}

.auth-header h2 {
  font-size: 1.4rem;
  font-weight: 700;
}

.auth-form {
  display: flex;
  flex-direction: column;
  gap: 1.25rem;
}

.field {
  display: flex;
  flex-direction: column;
  gap: 0.4rem;
}

.field label {
  font-size: 0.875rem;
  font-weight: 500;
}

.auth-footer {
  text-align: center;
  font-size: 0.875rem;
  display: flex;
  gap: 0.4rem;
  justify-content: center;
}

.auth-link {
  color: var(--p-primary-color);
  text-decoration: none;
  font-weight: 500;
}

.auth-link:hover {
  text-decoration: underline;
}
</style>
