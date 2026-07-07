<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth.store'

const router = useRouter()
const authStore = useAuthStore()

const form = ref({
  username: '',
  email: '',
  password: '',
  firstName: '',
  lastName: '',
  phoneNumber: '',
  isDriver: false,
})

const loading = ref(false)
const error = ref('')

async function handleRegister() {
  const required = ['username', 'email', 'password', 'firstName', 'lastName']
  for (const f of required) {
    if (!form.value[f]) {
      error.value = 'Моля, попълнете всички задължителни полета'
      return
    }
  }

  loading.value = true
  error.value = ''
  try {
    await authStore.register(form.value)
    router.push('/')
  } catch (err) {
    error.value =
      err.response?.data?.message ||
      err.response?.data?.error ||
      'Грешка при регистрация. Опитайте отново.'
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
          <i class="pi pi-user-plus auth-icon" />
          <h2>Регистрация</h2>
        </div>
      </template>

      <template #content>
        <form @submit.prevent="handleRegister" class="auth-form">
          <Message v-if="error" severity="error" :closable="false" class="w-full">
            {{ error }}
          </Message>

          <div class="field-row">
            <div class="field">
              <label for="firstName">Собствено име *</label>
              <InputText
                id="firstName"
                v-model="form.firstName"
                placeholder="Иван"
                class="w-full"
              />
            </div>
            <div class="field">
              <label for="lastName">Фамилия *</label>
              <InputText
                id="lastName"
                v-model="form.lastName"
                placeholder="Иванов"
                class="w-full"
              />
            </div>
          </div>

          <div class="field">
            <label for="username">Потребителско име *</label>
            <InputText
              id="username"
              v-model="form.username"
              placeholder="ivan123"
              class="w-full"
              autocomplete="username"
            />
          </div>

          <div class="field">
            <label for="email">Имейл *</label>
            <InputText
              id="email"
              v-model="form.email"
              type="email"
              placeholder="ivan@example.com"
              class="w-full"
              autocomplete="email"
            />
          </div>

          <div class="field">
            <label for="password">Парола *</label>
            <Password
              id="password"
              v-model="form.password"
              placeholder="Минимум 6 символа"
              fluid
              toggleMask
              autocomplete="new-password"
            />
          </div>

          <div class="field">
            <label for="phone">Телефон</label>
            <InputText
              id="phone"
              v-model="form.phoneNumber"
              placeholder="+359 888 123 456"
              class="w-full"
              autocomplete="tel"
            />
          </div>

          <div class="field-checkbox">
            <Checkbox
              inputId="isDriver"
              v-model="form.isDriver"
              binary
            />
            <label for="isDriver" class="checkbox-label">
              <i class="pi pi-car" /> Искам да предлагам превози (шофьор)
            </label>
          </div>

          <Button
            type="submit"
            label="Регистрирай се"
            icon="pi pi-user-plus"
            :loading="loading"
            class="w-full"
          />
        </form>
      </template>

      <template #footer>
        <div class="auth-footer">
          <span>Вече имате акаунт?</span>
          <RouterLink to="/login" class="auth-link">Влезте</RouterLink>
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
  max-width: 520px;
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
  gap: 1.1rem;
}

.field-row {
  display: flex;
  gap: 1rem;
}

.field {
  display: flex;
  flex-direction: column;
  gap: 0.4rem;
  flex: 1;
}

.field label {
  font-size: 0.875rem;
  font-weight: 500;
}

.field-checkbox {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.checkbox-label {
  font-size: 0.9rem;
  display: flex;
  align-items: center;
  gap: 0.35rem;
  cursor: pointer;
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
