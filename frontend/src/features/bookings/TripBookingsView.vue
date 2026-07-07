<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { useToast } from 'primevue/usetoast'
import { bookingsApi } from '@/shared/api/bookings'
import type { BookingResponse, BookingStatus } from '@/types'

const route = useRoute()
const router = useRouter()
const { t, locale } = useI18n()
const toast = useToast()

const bookings = ref<BookingResponse[]>([])
const loading = ref(false)
const actionLoadingId = ref<string | null>(null)

const ridePublicId = computed(() => route.params.id as string)

const statusSeverity = (status: BookingStatus): 'warn' | 'success' | 'danger' | 'secondary' => {
  switch (status) {
    case 'PENDING': return 'warn'
    case 'CONFIRMED': return 'success'
    case 'REJECTED':
    case 'CANCELLED':
    case 'NO_SHOW': return 'danger'
    default: return 'secondary'
  }
}

function formatDate(isoString: string): string {
  return new Intl.DateTimeFormat(locale.value === 'bg' ? 'bg-BG' : 'en-GB', {
    timeZone: 'Europe/Sofia',
    day: '2-digit',
    month: '2-digit',
    year: 'numeric',
    hour: '2-digit',
    minute: '2-digit',
  }).format(new Date(isoString))
}

async function loadBookings(): Promise<void> {
  loading.value = true
  try {
    const { data } = await bookingsApi.getRideBookings(ridePublicId.value)
    bookings.value = data
  } catch {
    toast.add({
      severity: 'error',
      summary: t('auth.errors.generic'),
      life: 4000,
    })
  } finally {
    loading.value = false
  }
}

async function confirmBooking(publicId: string): Promise<void> {
  actionLoadingId.value = publicId
  try {
    await bookingsApi.confirm(publicId)
    updateBookingStatus(publicId, 'CONFIRMED')
    toast.add({ severity: 'success', summary: t('bookings.confirm'), life: 3000 })
  } catch {
    toast.add({ severity: 'error', summary: t('auth.errors.generic'), life: 4000 })
  } finally {
    actionLoadingId.value = null
  }
}

async function rejectBooking(publicId: string): Promise<void> {
  actionLoadingId.value = publicId
  try {
    await bookingsApi.reject(publicId)
    updateBookingStatus(publicId, 'REJECTED')
    toast.add({ severity: 'info', summary: t('bookings.reject'), life: 3000 })
  } catch {
    toast.add({ severity: 'error', summary: t('auth.errors.generic'), life: 4000 })
  } finally {
    actionLoadingId.value = null
  }
}

function updateBookingStatus(publicId: string, status: BookingStatus): void {
  const idx = bookings.value.findIndex(b => b.publicId === publicId)
  if (idx !== -1) {
    bookings.value[idx] = { ...bookings.value[idx], status }
  }
}

onMounted(loadBookings)
</script>

<template>
  <div class="trip-bookings">
    <div class="trip-bookings__header">
      <Button
        :label="t('common.back')"
        icon="pi pi-arrow-left"
        text
        @click="router.push('/my-trips')"
      />
      <h1 class="trip-bookings__title">{{ 'Резервации за маршрута' }}</h1>
    </div>

    <div v-if="loading" class="trip-bookings__loading">
      <ProgressSpinner />
    </div>

    <div v-else-if="bookings.length === 0" class="trip-bookings__empty">
      <i class="pi pi-ticket empty-icon" />
      <p class="empty-text">{{ t('bookings.title') }} — 0</p>
    </div>

    <Card v-else class="trip-bookings__card">
      <template #content>
        <DataTable
          :value="bookings"
          responsiveLayout="scroll"
          :rowHover="true"
        >
          <Column field="passenger" :header="'Пасажер'">
            <template #body="{ data: booking }: { data: BookingResponse }">
              {{ booking.passenger.firstName }} {{ booking.passenger.lastName }}
            </template>
          </Column>

          <Column field="seats" :header="t('bookings.seats')" style="width: 120px">
            <template #body="{ data: booking }: { data: BookingResponse }">
              {{ booking.seats }}
            </template>
          </Column>

          <Column field="status" :header="'Статус'" style="width: 150px">
            <template #body="{ data: booking }: { data: BookingResponse }">
              <Tag
                :value="t(`bookings.status.${booking.status}`)"
                :severity="statusSeverity(booking.status)"
              />
            </template>
          </Column>

          <Column field="createdAt" :header="'Дата на заявка'" style="width: 180px">
            <template #body="{ data: booking }: { data: BookingResponse }">
              {{ formatDate(booking.createdAt) }}
            </template>
          </Column>

          <Column field="message" :header="t('bookings.message')">
            <template #body="{ data: booking }: { data: BookingResponse }">
              <span class="booking-message">{{ booking.message ?? '—' }}</span>
            </template>
          </Column>

          <Column :header="'Действие'" style="width: 200px">
            <template #body="{ data: booking }: { data: BookingResponse }">
              <div v-if="booking.status === 'PENDING'" class="action-btns">
                <Button
                  :label="t('bookings.confirm')"
                  icon="pi pi-check"
                  severity="success"
                  size="small"
                  :loading="actionLoadingId === booking.publicId"
                  @click="confirmBooking(booking.publicId)"
                />
                <Button
                  :label="t('bookings.reject')"
                  icon="pi pi-times"
                  severity="danger"
                  size="small"
                  outlined
                  :loading="actionLoadingId === booking.publicId"
                  @click="rejectBooking(booking.publicId)"
                />
              </div>
              <span v-else class="action-none">—</span>
            </template>
          </Column>
        </DataTable>
      </template>
    </Card>
  </div>
</template>

<style scoped>
.trip-bookings {
  padding: var(--spacing-4) 0;
}

.trip-bookings__header {
  display: flex;
  align-items: center;
  gap: var(--spacing-4);
  margin-bottom: var(--spacing-5);
}

.trip-bookings__title {
  font-size: var(--fs-h1);
  font-weight: 700;
  margin: 0;
}

.trip-bookings__loading {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 40vh;
}

.trip-bookings__empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 40vh;
  gap: var(--spacing-4);
  color: var(--muted);
}

.empty-icon {
  font-size: 4rem;
}

.empty-text {
  font-size: var(--fs-h3);
  margin: 0;
}

.action-btns {
  display: flex;
  gap: var(--spacing-2);
}

.action-none {
  color: var(--muted);
}

.booking-message {
  color: var(--muted);
  font-size: var(--fs-small);
}
</style>
