<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth.store'
import { usersApi } from '../api/users.api'
import { tripsApi } from '../api/trips.api'
import { useConfirm } from 'primevue/useconfirm'
import { useToast } from 'primevue/usetoast'
import LoadingSpinner from '../components/LoadingSpinner.vue'
import EmptyState from '../components/EmptyState.vue'
import TripStatusBadge from '../components/TripStatusBadge.vue'

const router = useRouter()
const authStore = useAuthStore()
const confirm = useConfirm()
const toast = useToast()

const trips = ref([])
const loading = ref(false)

const grouped = computed(() => {
  const map = { SCHEDULED: [], ACTIVE: [], COMPLETED: [], CANCELLED: [] }
  for (const t of trips.value) {
    const key = t.status ?? 'SCHEDULED'
    if (!map[key]) map[key] = []
    map[key].push(t)
  }
  return map
})

onMounted(async () => {
  loading.value = true
  try {
    const res = await usersApi.getTrips(authStore.user.id)
    trips.value = res.data
  } catch {
    toast.add({ severity: 'error', summary: 'Грешка при зареждане', life: 3000 })
  } finally {
    loading.value = false
  }
})

function departureStr(trip) {
  if (!trip.departureTime) return ''
  return new Date(trip.departureTime).toLocaleString('bg-BG', {
    day: '2-digit',
    month: '2-digit',
    year: 'numeric',
    hour: '2-digit',
    minute: '2-digit',
  })
}

function availableSeats(trip) {
  return (trip.totalSeats ?? 0) - (trip.bookedSeats ?? 0)
}

function cancelTrip(trip) {
  confirm.require({
    message: `Сигурни ли сте, че искате да отмените пътуването ${trip.originCity} → ${trip.destinationCity}?`,
    header: 'Отмяна на пътуване',
    icon: 'pi pi-exclamation-triangle',
    acceptLabel: 'Отмени пътуването',
    rejectLabel: 'Не',
    acceptClass: 'p-button-danger',
    accept: async () => {
      try {
        await tripsApi.cancel(trip.id)
        const idx = trips.value.findIndex((t) => t.id === trip.id)
        if (idx !== -1) trips.value[idx] = { ...trips.value[idx], status: 'CANCELLED' }
        toast.add({ severity: 'info', summary: 'Пътуването е отменено', life: 3000 })
      } catch {
        toast.add({ severity: 'error', summary: 'Грешка при отмяна', life: 3000 })
      }
    },
  })
}

const statusLabels = {
  SCHEDULED: 'Планирани',
  ACTIVE: 'Активни',
  COMPLETED: 'Завършени',
  CANCELLED: 'Отменени',
}
</script>

<template>
  <div class="my-trips-page">
    <div class="page-inner">
      <div class="page-header">
        <h1>Моите пътувания</h1>
        <Button
          label="Публикувай пътуване"
          icon="pi pi-plus"
          @click="router.push('/trips/create')"
        />
      </div>

      <LoadingSpinner v-if="loading" />

      <template v-else-if="trips.length === 0">
        <EmptyState
          message="Нямате публикувани пътувания"
          icon="pi pi-car"
        />
        <div style="text-align: center; margin-top: 1rem">
          <Button
            label="Публикувай първото си пътуване"
            icon="pi pi-plus"
            @click="router.push('/trips/create')"
          />
        </div>
      </template>

      <template v-else>
        <div
          v-for="(groupTrips, status) in grouped"
          :key="status"
          v-show="groupTrips.length > 0"
          class="status-group"
        >
          <h2 class="group-title">
            {{ statusLabels[status] }}
            <span class="group-count">({{ groupTrips.length }})</span>
          </h2>

          <div class="trips-table-wrapper">
            <DataTable :value="groupTrips" stripedRows class="trips-table">
              <Column field="originCity" header="Маршрут">
                <template #body="{ data }">
                  <span class="route-cell">
                    {{ data.originCity }}
                    <i class="pi pi-arrow-right" />
                    {{ data.destinationCity }}
                  </span>
                </template>
              </Column>
              <Column header="Дата">
                <template #body="{ data }">
                  {{ departureStr(data) }}
                </template>
              </Column>
              <Column header="Места">
                <template #body="{ data }">
                  {{ data.bookedSeats ?? 0 }}/{{ data.totalSeats }}
                  <span class="seats-free">({{ availableSeats(data) }} свободни)</span>
                </template>
              </Column>
              <Column header="Цена">
                <template #body="{ data }">
                  {{ data.pricePerSeat }} лв.
                </template>
              </Column>
              <Column header="Статус">
                <template #body="{ data }">
                  <TripStatusBadge :status="data.status" />
                </template>
              </Column>
              <Column header="Действия">
                <template #body="{ data }">
                  <div class="action-btns">
                    <Button
                      icon="pi pi-eye"
                      text
                      rounded
                      size="small"
                      v-tooltip="'Виж'"
                      @click="router.push(`/trips/${data.id}`)"
                    />
                    <Button
                      v-if="data.status === 'SCHEDULED'"
                      icon="pi pi-pencil"
                      text
                      rounded
                      size="small"
                      v-tooltip="'Редактирай'"
                      @click="router.push(`/trips/${data.id}/edit`)"
                    />
                    <Button
                      v-if="data.status === 'SCHEDULED' || data.status === 'ACTIVE'"
                      icon="pi pi-times"
                      text
                      rounded
                      size="small"
                      severity="danger"
                      v-tooltip="'Отмени'"
                      @click="cancelTrip(data)"
                    />
                  </div>
                </template>
              </Column>
            </DataTable>
          </div>
        </div>
      </template>
    </div>
  </div>
</template>

<style scoped>
.my-trips-page {
  background: var(--p-surface-50);
  min-height: calc(100vh - 128px);
}

.page-inner {
  max-width: 1100px;
  margin: 0 auto;
  padding: 2rem 1.5rem;
}

.page-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 2rem;
  flex-wrap: wrap;
  gap: 1rem;
}

.page-header h1 {
  font-size: 1.75rem;
  font-weight: 700;
}

.status-group {
  margin-bottom: 2rem;
}

.group-title {
  font-size: 1.1rem;
  font-weight: 600;
  margin-bottom: 0.75rem;
  display: flex;
  align-items: center;
  gap: 0.4rem;
}

.group-count {
  color: var(--p-text-muted-color);
  font-weight: normal;
  font-size: 0.9rem;
}

.trips-table-wrapper {
  border-radius: 8px;
  overflow: hidden;
  border: 1px solid var(--p-surface-200);
}

.route-cell {
  display: flex;
  align-items: center;
  gap: 0.4rem;
  font-weight: 500;
}

.seats-free {
  font-size: 0.8rem;
  color: var(--p-text-muted-color);
}

.action-btns {
  display: flex;
  gap: 0.25rem;
}
</style>
