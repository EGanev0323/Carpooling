<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useTripsStore } from '../stores/trips.store'
import { useToast } from 'primevue/usetoast'
import TripForm from '../components/TripForm.vue'

const router = useRouter()
const tripsStore = useTripsStore()
const toast = useToast()

const formData = ref({})
const error = ref('')

function formatDateTime(date) {
  if (!date) return null
  return new Date(date).toISOString()
}

async function handleSubmit(data) {
  error.value = ''
  try {
    const payload = {
      ...data,
      departureTime: formatDateTime(data.departureTime),
      estimatedArrival: formatDateTime(data.estimatedArrival),
    }
    const created = await tripsStore.createTrip(payload)
    toast.add({
      severity: 'success',
      summary: 'Пътуването е публикувано',
      life: 3000,
    })
    router.push(`/trips/${created.id}`)
  } catch (err) {
    error.value =
      err.response?.data?.message ?? 'Грешка при публикуване. Опитайте отново.'
    toast.add({
      severity: 'error',
      summary: 'Грешка',
      detail: error.value,
      life: 4000,
    })
  }
}
</script>

<template>
  <div class="create-trip-page">
    <div class="page-inner">
      <div class="page-header">
        <Button
          text
          icon="pi pi-arrow-left"
          label="Назад"
          @click="$router.back()"
        />
        <h1>Публикувай пътуване</h1>
      </div>

      <Message v-if="error" severity="error" :closable="false" class="mb-4">
        {{ error }}
      </Message>

      <TripForm
        v-model="formData"
        :loading="tripsStore.loading"
        @submit="handleSubmit"
      />
    </div>
  </div>
</template>

<style scoped>
.create-trip-page {
  background: var(--p-surface-50);
  min-height: calc(100vh - 128px);
  padding: 2rem 1rem;
}

.page-inner {
  max-width: 760px;
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
</style>
