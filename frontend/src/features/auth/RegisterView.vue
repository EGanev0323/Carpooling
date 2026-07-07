<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { useAuthStore } from '@/stores/auth'
import { useToast } from 'primevue/usetoast'

const router = useRouter()
const { t } = useI18n()
const authStore = useAuthStore()
const toast = useToast()

const PHONE_PATTERN = /^(\+359|0)[0-9]{8,9}$/
const MIN_PASSWORD_LENGTH = 12

const form = ref({
  firstName: '',
  lastName: '',
  email: '',
  password: '',
  phoneNumber: '',
  username: '',
  isDriver: false
})

const loading = ref(false)
const error = ref('')
const registered = ref(false)

function validate(): string | null {
  const required = ['firstName', 'lastName', 'email', 'password'] as const
  for (const field of required) {
    if (!form.value[field]) return t('auth.errors.requiredFields')
  }
  if (form.value.password.length < MIN_PASSWORD_LENGTH) {
    return t('auth.errors.passwordMinLength')
  }
  if (form.value.phoneNumber && !PHONE_PATTERN.test(form.value.phoneNumber)) {
    return t('auth.errors.phonePattern')
  }
  return null
}

async function handleRegister() {
  const validationError = validate()
  if (validationError) {
    error.value = validationError
    return
  }

  loading.value = true
  error.value = ''
  try {
    await authStore.register({
      firstName: form.value.firstName,
      lastName: form.value.lastName,
      email: form.value.email,
      password: form.value.password,
      phoneNumber: form.value.phoneNumber || undefined,
      username: form.value.username || undefined,
      isDriver: form.value.isDriver
    })
    registered.value = true
  } catch (err: unknown) {
    const e = err as { response?: { status?: number; data?: { message?: string; error?: string } } }
    if (e.response?.status === 429) {
      error.value = t('auth.errors.tooManyAttempts')
    } else if (e.response?.status === 409) {
      error.value = t('auth.errors.emailTaken')
    } else {
      error.value =
        e.response?.data?.message ??
        e.response?.data?.error ??
        t('auth.errors.generic')
    }
    toast.add({
      severity: 'error',
      summary: 'Грешка',
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
          <i class="pi pi-user-plus auth-icon" />
          <h2>{{ t('auth.registerTitle') }}</h2>
        </div>
      </template>

      <template #content>
        <div v-if="registered" class="verify-message">
          <i class="pi pi-envelope verify-icon" />
          <h3>{{ t('auth.verifyEmailTitle') }}</h3>
          <p>{{ t('auth.verifyEmailMessage') }}</p>
          <Button
            :label="t('nav.login')"
            icon="pi pi-sign-in"
            class="verify-btn"
            @click="router.push('/login')"
          />
        </div>

        <form v-else @submit.prevent="handleRegister" class="auth-form">
          <Message v-if="error" severity="error" :closable="false" class="w-full">
            {{ error }}
          </Message>

          <div class="field-row">
            <div class="field">
              <label for="firstName">{{ t('auth.firstName') }} *</label>
              <InputText
                id="firstName"
                v-model="form.firstName"
                placeholder="Иван"
                class="w-full"
              />
            </div>
            <div class="field">
              <label for="lastName">{{ t('auth.lastName') }} *</label>
              <InputText
                id="lastName"
                v-model="form.lastName"
                placeholder="Иванов"
                class="w-full"
              />
            </div>
          </div>

          <div class="field">
            <label for="email">{{ t('auth.email') }} *</label>
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
            <label for="password">{{ t('auth.password') }} * ({{ t('auth.errors.passwordMinLength') }})</label>
            <Password
              id="password"
              v-model="form.password"
              placeholder="Min. 12 символа"
              fluid
              toggleMask
              autocomplete="new-password"
            />
          </div>

          <div class="field">
            <label for="phone">{{ t('auth.phone') }}</label>
            <InputText
              id="phone"
              v-model="form.phoneNumber"
              placeholder="+359888123456"
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
              <i class="pi pi-car" /> {{ t('auth.isDriver') }}
            </label>
          </div>

          <Button
            type="submit"
            :label="t('auth.registerButton')"
            icon="pi pi-user-plus"
            :loading="loading"
            class="w-full"
          />
        </form>
      </template>

      <template #footer>
        <div class="auth-footer">
          <span>{{ t('auth.haveAccount') }}</span>
          <RouterLink to="/login" class="auth-link">{{ t('nav.login') }}</RouterLink>
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
  max-width: 520px;
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
  gap: var(--spacing-4);
}

.field-row {
  display: flex;
  gap: var(--spacing-4);
}

.field {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-1);
  flex: 1;
}

.field label {
  font-size: var(--fs-small);
  font-weight: 500;
}

.field-checkbox {
  display: flex;
  align-items: center;
  gap: var(--spacing-2);
}

.checkbox-label {
  font-size: var(--fs-body);
  display: flex;
  align-items: center;
  gap: var(--spacing-1);
  cursor: pointer;
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

/* Post-registration success state */
.verify-message {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: var(--spacing-4);
  text-align: center;
  padding: var(--spacing-6) 0;
}

.verify-icon {
  font-size: 3rem;
  color: var(--mark);
}

.verify-message h3 {
  font-size: var(--fs-h3);
  font-weight: 700;
}

.verify-message p {
  color: var(--muted);
  line-height: 1.6;
}

.verify-btn {
  margin-top: var(--spacing-2);
}

@media (max-width: 480px) {
  .field-row {
    flex-direction: column;
  }
}
</style>
