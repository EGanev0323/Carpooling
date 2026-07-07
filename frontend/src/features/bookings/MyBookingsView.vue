<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { useToast } from 'primevue/usetoast'
import { useRouter } from 'vue-router'
import { bookingsApi } from '@/shared/api/bookings'
import type { BookingResponse, BookingStatus } from '@/types'

const { t, locale } = useI18n()
const toast = useToast()
const router = useRouter()

const bookings = ref<BookingResponse[]>([])
const loading = ref(false)
const cancellingId = ref<string | null>(null)

function statusSeverity(status: BookingStatus): 'warn' | 'success' | 'danger' | 'secondary' {
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
    month: 'short',
    year: 'numeric',
    hour: '2-digit',
    minute: '2-digit',
  }).format(new Date(isoString))
}

function canCancel(booking: BookingResponse): boolean {
  if (booking.status !== 'PENDING' && booking.status !== 'CONFIRMED') return false
  const departure = new Date(booking.rideSummary.departureAt)
  return departure > new Date()
}

const cityName = (city: { nameBg: string; nameEn: string }): string =>
  locale.value === 'bg' ? city.nameBg : city.nameEn

const sortedBookings = computed(() =>
  [...bookings.value].sort(
    (a, b) =>
      new Date(b.rideSummary.departureAt).getTime() -
      new Date(a.rideSummary.departureAt).getTime()
  )
)

async function loadBookings(): Promise<void> {
  loading.value = true
  try {
    const { data } = await bookingsApi.getMy()
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

async function cancelBooking(publicId: string): Promise<void> {
  cancellingId.value = publicId
  try {
    await bookingsApi.cancel(publicId)
    const idx = bookings.value.findIndex(b => b.publicId === publicId)
    if (idx !== -1) {
      bookings.value[idx] = { ...bookings.value[idx], status: 'CANCELLED' }
    }
    toast.add({
      severity: 'info',
      summary: t('bookings.cancel'),
      detail: t('bookings.status.CANCELLED'),
      life: 3000,
    })
  } catch {
    toast.add({
      severity: 'error',
      summary: t('auth.errors.generic'),
      life: 4000,
    })
  } finally {
    cancellingId.value = null
  }
}

onMounted(loadBookings)
</script>

<template>
  <div class="my-bookings">
    <div class="my-bookings__header">
      <h1 class="my-bookings__title">{{ t('bookings.title') }}</h1>
    </div>

    <div v-if="loading" class="my-bookings__skeletons">
      <Skeleton v-for="n in 3" :key="n" height="160px" border-radius="12px" />
    </div>

    <div v-else-if="sortedBookings.length === 0" class="my-bookings__empty">
      <i class="pi pi-ticket empty-icon" />
      <p class="empty-text">{{ t('rides.noResults') }}</p>
      <Button
        :label="t('nav.searchTrip')"
        icon="pi pi-search"
        @click="router.push('/rides')"
      />
    </div>

    <div v-else class="my-bookings__list">
      <Card
        v-for="booking in sortedBookings"
        :key="booking.publicId"
        class="booking-card"
      >
        <template #content>
          <div class="booking-card__inner">
            <div class="booking-card__route">
              <div class="route-city">
                <i class="pi pi-map-marker" />
                <span>{{ cityName(booking.rideSummary.originCity) }}</span>
              </div>
              <i class="pi pi-arrow-right route-arrow" />
              <div class="route-city route-city--dest">
                <i class="pi pi-map-marker" />
                <span>{{ cityName(booking.rideSummary.destinationCity) }}</span>
              </div>
            </div>

            <div class="booking-card__meta">
              <div class="meta-item">
                <i class="pi pi-clock" />
                <span>{{ formatDate(booking.rideSummary.departureAt) }}</span>
              </div>
              <div class="meta-item">
                <i class="pi pi-user" />
                <span>{{ booking.rideSummary.driverName }}</span>
              </div>
              <div class="meta-item">
                <i class="pi pi-users" />
                <span>{{ booking.seats }} {{ t('bookings.seats') }}</span>
              </div>
              <div class="meta-item">
                <i class="pi pi-tag" />
                <span>{{ booking.rideSummary.pricePerSeat * booking.seats }} лв.</span>
              </div>
            </div>

            <div class="booking-card__footer">
              <Tag
                :value="t(`bookings.status.${booking.status}`)"
                :severity="statusSeverity(booking.status)"
              />

              <div class="booking-card__actions">
                <Button
                  :label="'Виж маршрута'"
                  icon="pi pi-eye"
                  text
                  size="small"
                  @click="router.push(`/trips/${booking.rideSummary.publicId}`)"
                />
                <Button
                  v-if="canCancel(booking)"
                  :label="t('bookings.cancel')"
                  icon="pi pi-times"
                  severity="danger"
                  outlined
                  size="small"
                  :loading="cancellingId === booking.publicId"
                  @click="cancelBooking(booking.publicId)"
                />
              </div>
            </div>
          </div>
        </template>
      </Card>
    </div>
  </div>
</template>

<style scoped>
.my-bookings {
  padding: var(--spacing-4) 0;
}

.my-bookings__header {
  margin-bottom: var(--spacing-6);
}

.my-bookings__title {
  font-size: var(--fs-h1);
  font-weight: 700;
  margin: 0;
}

.my-bookings__skeletons {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-4);
}

.my-bookings__empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 40vh;
  gap: var(--spacing-4);
  color: var(--muted);
  text-align: center;
}

.empty-icon {
  font-size: 4rem;
}

.empty-text {
  font-size: var(--fs-h3);
  margin: 0;
}

.my-bookings__list {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-4);
}

.booking-card__inner {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-3);
}

.booking-card__route {
  display: flex;
  align-items: center;
  gap: var(--spacing-3);
}

.route-city {
  display: flex;
  align-items: center;
  gap: var(--spacing-1);
  font-weight: 600;
  font-size: 1.05rem;
}

.route-city--dest .pi {
  color: var(--mark);
}

.route-arrow {
  color: var(--muted);
}

.booking-card__meta {
  display: flex;
  flex-wrap: wrap;
  gap: var(--spacing-4);
}

.meta-item {
  display: flex;
  align-items: center;
  gap: var(--spacing-1);
  font-size: var(--fs-small);
  color: var(--muted);
}

.booking-card__footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: var(--spacing-3);
  flex-wrap: wrap;
}

.booking-card__actions {
  display: flex;
  gap: var(--spacing-2);
  align-items: center;
}
</style>
