<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth.store'
import { usersApi } from '../api/users.api'
import { useToast } from 'primevue/usetoast'

const router = useRouter()
const authStore = useAuthStore()
const toast = useToast()

const form = ref({
  firstName: '',
  lastName: '',
  phoneNumber: '',
  isDriver: false,
  isPassenger: true,
})

const saving = ref(false)
const error = ref('')

onMounted(() => {
  if (authStore.user) {
    const u = authStore.user
    form.value = {
      firstName: u.firstName ?? '',
      lastName: u.lastName ?? '',
      phoneNumber: u.phoneNumber ?? '',
      isDriver: u.isDriver ?? false,
      isPassenger: u.isPassenger ?? true,
    }
  }
})

async function handleSave() {
  saving.value = true
  error.value = ''
  try {
    await usersApi.update(authStore.user.id, form.value)
    await authStore.fetchMe()
    toast.add({
      severity: 'success',
      summary: 'Профилът е обновен',
      life: 3000,
    })
    router.push(`/users/${authStore.user.id}`)
  } catch (err) {
    error.value =
      err.response?.data?.message ?? 'Грешка при запазване. Опитайте отново.'
  } finally {
    saving.value = false
  }
}
</script>

<template>
  <div class="edit-profile-page">
    <div class="page-inner">
      <div class="page-header">
        <Button text icon="pi pi-arrow-left" label="Назад" @click="$router.back()" />
        <h1>Редактиране на профил</h1>
      </div>

      <Card>
        <template #content>
          <form @submit.prevent="handleSave" class="profile-form">
            <Message v-if="error" severity="error" :closable="false">
              {{ error }}
            </Message>

            <div class="field-row">
              <div class="field">
                <label for="firstName">Собствено име</label>
                <InputText
                  id="firstName"
                  v-model="form.firstName"
                  placeholder="Иван"
                  class="w-full"
                />
              </div>
              <div class="field">
                <label for="lastName">Фамилия</label>
                <InputText
                  id="lastName"
                  v-model="form.lastName"
                  placeholder="Иванов"
                  class="w-full"
                />
              </div>
            </div>

            <div class="field">
              <label for="phoneNumber">Телефон</label>
              <InputText
                id="phoneNumber"
                v-model="form.phoneNumber"
                placeholder="+359 888 123 456"
                class="w-full"
              />
            </div>

            <div class="roles-section">
              <h3>Роли</h3>
              <div class="role-toggle">
                <ToggleSwitch v-model="form.isDriver" inputId="isDriver" />
                <label for="isDriver">
                  <i class="pi pi-car" />
                  <strong>Шофьор</strong> — предлагам превози
                </label>
              </div>
              <div class="role-toggle">
                <ToggleSwitch v-model="form.isPassenger" inputId="isPassenger" />
                <label for="isPassenger">
                  <i class="pi pi-user" />
                  <strong>Пътник</strong> — резервирам места
                </label>
              </div>
            </div>

            <div class="form-actions">
              <Button
                type="button"
                label="Отказ"
                text
                @click="$router.back()"
              />
              <Button
                type="submit"
                label="Запази промените"
                icon="pi pi-check"
                :loading="saving"
              />
            </div>
          </form>
        </template>
      </Card>
    </div>
  </div>
</template>

<style scoped>
.edit-profile-page {
  background: var(--p-surface-50);
  min-height: calc(100vh - 128px);
  padding: 2rem 1rem;
}

.page-inner {
  max-width: 600px;
  margin: 0 auto;
}

.page-header {
  display: flex;
  align-items: center;
  gap: 1rem;
  margin-bottom: 1.5rem;
}

.page-header h1 {
  font-size: 1.5rem;
  font-weight: 700;
}

.profile-form {
  display: flex;
  flex-direction: column;
  gap: 1.25rem;
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

.roles-section {
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
}

.roles-section h3 {
  font-size: 0.95rem;
  font-weight: 600;
  color: var(--p-text-muted-color);
}

.role-toggle {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  font-size: 0.9rem;
}

.role-toggle label {
  display: flex;
  align-items: center;
  gap: 0.4rem;
  cursor: pointer;
}

.form-actions {
  display: flex;
  justify-content: flex-end;
  gap: 0.5rem;
  padding-top: 0.5rem;
}

@media (max-width: 480px) {
  .field-row {
    flex-direction: column;
  }
}
</style>
