<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useTripsStore } from '../stores/trips.store'
import { tripsApi } from '../api/trips.api'
import { useToast } from 'primevue/usetoast'
import TripForm from '../components/TripForm.vue'
import LoadingSpinner from '../components/LoadingSpinner.vue'

const route = useRoute()
const router = useRouter()
const tripsStore = useTripsStore()
const toast = useToast()

const formData = ref({})
const error = ref('')
const saving = ref(false)

onMounted(async () => {
  await tripsStore.fetchTrip(route.params.id)
  if (tripsStore.currentTrip) {
    formData.value = { ...tripsStore.currentTrip, carId: tripsStore.currentTrip.car?.id }
  }
})

function formatDateTime(date) {
  if (!date) return null
  return new Date(date).toISOString()
}

async function handleSubmit(data) {
  error.value = ''
  saving.value = true
  try {
    const payload = {
      ...data,
      departureTime: formatDateTime(data.departureTime),
      estimatedArrival: formatDateTime(data.estimatedArrival),
    }
    await tripsApi.update(route.params.id, payload)
    toast.add({
      severity: 'success',
      summary: 'Промените са запазени',
      life: 3000,
    })
    router.push(`/trips/${route.params.id}`)
  } catch (err) {
    error.value =
      err.response?.data?.message ?? 'Грешка при запазване. Опитайте отново.'
    toast.add({
      severity: 'error',
      summary: 'Грешка',
      detail: error.value,
      life: 4000,
    })
  } finally {
    saving.value = false
  }
}
</script>

<template>
  <div class="edit-trip-page">
    <div class="page-inner">
      <div class="page-header">
        <Button
          text
          icon="pi pi-arrow-left"
          label="Назад"
          @click="$router.back()"
        />
        <h1>Редактирай пътуване</h1>
      </div>

      <LoadingSpinner v-if="tripsStore.loading && !tripsStore.currentTrip" />

      <template v-else>
        <Message v-if="error" severity="error" :closable="false" class="mb-4">
          {{ error }}
        </Message>

        <TripForm
          v-model="formData"
          :loading="saving"
          @submit="handleSubmit"
        />
      </template>
    </div>
  </div>
</template>

<style scoped>
.edit-trip-page {
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
