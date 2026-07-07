<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { useToast } from 'primevue/usetoast'
import { ridesApi } from '@/shared/api/rides'
import { bookingsApi } from '@/shared/api/bookings'
import { useAuthStore } from '@/stores/auth'
import type { RideResponse, BookingResponse } from '@/types'

const route = useRoute()
const router = useRouter()
const { t, locale } = useI18n()
const toast = useToast()
const authStore = useAuthStore()

const ride = ref<RideResponse | null>(null)
const myBooking = ref<BookingResponse | null>(null)
const loading = ref(false)
const notFound = ref(false)

const bookingSeats = ref(1)
const bookingMessage = ref('')
const bookingLoading = ref(false)

const publicId = computed(() => route.params.id as string)

const isDriver = computed(() =>
  authStore.isLoggedIn && ride.value?.driver.publicId === authStore.user?.id
)

const canBook = computed(() =>
  authStore.isLoggedIn &&
  !isDriver.value &&
  ride.value?.status === 'ACTIVE' &&
  (ride.value?.availableSeats ?? 0) > 0 &&
  myBooking.value === null
)

const cityName = (city: { nameBg: string; nameEn: string }): string =>
  locale.value === 'bg' ? city.nameBg : city.nameEn

const formattedDeparture = computed(() => {
  if (!ride.value) return ''
  const fmt = new Intl.DateTimeFormat(locale.value === 'bg' ? 'bg-BG' : 'en-GB', {
    timeZone: 'Europe/Sofia',
    weekday: 'long',
    day: '2-digit',
    month: 'long',
    year: 'numeric',
    hour: '2-digit',
    minute: '2-digit',
  })
  return fmt.format(new Date(ride.value.departureAt))
})

const starsArray = computed(() => {
  const rating = ride.value?.avgRating ?? 0
  return Array.from({ length: 5 }, (_, i) => i < Math.round(rating))
})

const statusSeverity = computed(() => {
  switch (ride.value?.status) {
    case 'CANCELLED': return 'danger'
    case 'COMPLETED': return 'success'
    default: return 'info'
  }
})

async function loadRide(): Promise<void> {
  loading.value = true
  notFound.value = false
  try {
    const { data } = await ridesApi.getOne(publicId.value)
    ride.value = data

    if (authStore.isLoggedIn && !isDriver.value) {
      await loadMyBooking()
    }
  } catch (err: unknown) {
    const error = err as { response?: { status: number } }
    if (error.response?.status === 404) {
      notFound.value = true
    } else {
      toast.add({
        severity: 'error',
        summary: t('auth.errors.generic'),
        life: 4000,
      })
    }
  } finally {
    loading.value = false
  }
}

async function loadMyBooking(): Promise<void> {
  try {
    const { data } = await bookingsApi.getMy()
    myBooking.value = data.find(b => b.ridePublicId === publicId.value) ?? null
  } catch {
    // silently ignore — booking info is optional
  }
}

async function submitBooking(): Promise<void> {
  if (!ride.value) return
  bookingLoading.value = true
  try {
    const { data } = await bookingsApi.create({
      ridePublicId: ride.value.publicId,
      seats: bookingSeats.value,
      message: bookingMessage.value || undefined,
    })
    myBooking.value = data
    toast.add({
      severity: 'success',
      summary: t('bookings.bookButton'),
      detail: t('bookings.status.PENDING'),
      life: 4000,
    })
  } catch {
    toast.add({
      severity: 'error',
      summary: t('auth.errors.generic'),
      life: 4000,
    })
  } finally {
    bookingLoading.value = false
  }
}

function goToBookings(): void {
  router.push(`/trips/${publicId.value}/bookings`)
}

onMounted(loadRide)
</script>

<template>
  <div class="trip-detail">
    <div v-if="loading" class="trip-detail__loading">
      <ProgressSpinner />
    </div>

    <div v-else-if="notFound" class="trip-detail__not-found">
      <i class="pi pi-exclamation-circle not-found-icon" />
      <p>{{ t('common.notFound') }}</p>
      <Button :label="t('common.back')" icon="pi pi-arrow-left" text @click="router.push('/rides')" />
    </div>

    <template v-else-if="ride">
      <div class="trip-detail__topbar">
        <Button
          :label="t('common.back')"
          icon="pi pi-arrow-left"
          text
          @click="router.push('/rides')"
        />
        <Tag
          v-if="ride.status !== 'ACTIVE'"
          :value="t(`bookings.status.${ride.status}`)"
          :severity="statusSeverity"
        />
      </div>

      <div class="trip-detail__layout">
        <div class="trip-detail__main">
          <Card class="trip-detail__card">
            <template #content>
              <div class="trip-route">
                <div class="trip-route__city trip-route__city--origin">
                  <i class="pi pi-map-marker" />
                  <div>
                    <div class="route-label">{{ t('rides.from') }}</div>
                    <div class="route-city">{{ cityName(ride.originCity) }}</div>
                  </div>
                </div>

                <div class="trip-route__separator">
                  <div class="route-line" />
                  <i class="pi pi-arrow-right" />
                  <div class="route-line" />
                </div>

                <div class="trip-route__city trip-route__city--dest">
                  <i class="pi pi-map-marker" />
                  <div>
                    <div class="route-label">{{ t('rides.to') }}</div>
                    <div class="route-city">{{ cityName(ride.destinationCity) }}</div>
                  </div>
                </div>
              </div>

              <Divider />

              <div class="trip-detail__info-grid">
                <div class="info-item">
                  <i class="pi pi-clock info-item__icon" />
                  <div>
                    <div class="info-item__label">{{ t('rides.date') }}</div>
                    <div class="info-item__value">{{ formattedDeparture }}</div>
                  </div>
                </div>

                <div class="info-item">
                  <i class="pi pi-users info-item__icon" />
                  <div>
                    <div class="info-item__label">{{ t('rides.seats') }}</div>
                    <div class="info-item__value">
                      {{ ride.availableSeats }}/{{ ride.totalSeats }}
                      {{ t('rides.available') }}
                    </div>
                  </div>
                </div>

                <div class="info-item">
                  <i class="pi pi-car info-item__icon" />
                  <div>
                    <div class="info-item__label">{{ t('cars.model') }}</div>
                    <div class="info-item__value">
                      {{ ride.car.make }} {{ ride.car.model }} ({{ ride.car.year }}) &bull;
                      {{ ride.car.color }}
                    </div>
                  </div>
                </div>

                <div class="info-item">
                  <i class="pi pi-tag info-item__icon" />
                  <div>
                    <div class="info-item__label">{{ t('rides.pricePerSeat') }}</div>
                    <div class="info-item__value price-value">
                      {{ ride.pricePerSeat }} лв.
                    </div>
                  </div>
                </div>
              </div>

              <template v-if="ride.description">
                <Divider />
                <div class="trip-detail__description">
                  <p class="description-label">{{ t('bookings.message') }}</p>
                  <p class="description-text">{{ ride.description }}</p>
                </div>
              </template>
            </template>
          </Card>
        </div>

        <div class="trip-detail__sidebar">
          <Card class="driver-card">
            <template #title>
              <span class="driver-card__title">Шофьор</span>
            </template>
            <template #content>
              <div class="driver-card__body">
                <Avatar
                  :image="ride.driver.avatarUrl ?? undefined"
                  :label="!ride.driver.avatarUrl ? ride.driver.firstName.charAt(0) : undefined"
                  shape="circle"
                  size="xlarge"
                />
                <div class="driver-card__info">
                  <span class="driver-card__name">
                    {{ ride.driver.firstName }} {{ ride.driver.lastName }}
                  </span>
                  <div v-if="ride.avgRating !== null" class="driver-card__stars">
                    <i
                      v-for="(filled, idx) in starsArray"
                      :key="idx"
                      :class="filled ? 'pi pi-star-fill' : 'pi pi-star'"
                      class="star-icon"
                    />
                    <span class="rating-value">{{ ride.avgRating.toFixed(1) }}</span>
                  </div>
                  <span v-else class="rating-none">Без оценки</span>
                </div>
              </div>
            </template>
          </Card>

          <Card v-if="isDriver" class="action-card">
            <template #content>
              <Button
                :label="'Управлявай резервации'"
                icon="pi pi-list"
                class="action-card__btn"
                @click="goToBookings"
              />
            </template>
          </Card>

          <Card v-else-if="authStore.isLoggedIn" class="action-card">
            <template #title>
              <span>{{ t('bookings.bookButton') }}</span>
            </template>
            <template #content>
              <div v-if="myBooking" class="booking-status">
                <p class="booking-status__label">Статус на резервацията ти:</p>
                <Tag
                  :value="t(`bookings.status.${myBooking.status}`)"
                  :severity="
                    myBooking.status === 'CONFIRMED' ? 'success' :
                    myBooking.status === 'PENDING' ? 'warn' : 'danger'
                  "
                />
              </div>

              <form v-else-if="canBook" class="booking-form" @submit.prevent="submitBooking">
                <div class="booking-form__field">
                  <label class="booking-form__label">{{ t('bookings.seats') }}</label>
                  <InputNumber
                    v-model="bookingSeats"
                    :min="1"
                    :max="ride.availableSeats"
                    showButtons
                    buttonLayout="horizontal"
                    :step="1"
                    class="booking-form__input"
                  />
                </div>
                <div class="booking-form__field">
                  <label class="booking-form__label">{{ t('bookings.message') }}</label>
                  <Textarea
                    v-model="bookingMessage"
                    :placeholder="t('bookings.message')"
                    rows="3"
                    class="booking-form__textarea"
                  />
                </div>
                <Button
                  type="submit"
                  :label="t('bookings.bookButton')"
                  icon="pi pi-check"
                  :loading="bookingLoading"
                  class="booking-form__submit"
                />
              </form>

              <p v-else-if="ride.status === 'ACTIVE' && ride.availableSeats === 0" class="no-seats">
                Няма свободни места
              </p>
              <p v-else-if="ride.status !== 'ACTIVE'" class="no-seats">
                {{ t(`bookings.status.${ride.status}`) }}
              </p>
            </template>
          </Card>

          <Card v-else class="action-card">
            <template #content>
              <p class="login-prompt">Влез в профила си, за да резервираш места.</p>
              <Button
                :label="t('nav.login')"
                icon="pi pi-sign-in"
                class="action-card__btn"
                @click="router.push('/login')"
              />
            </template>
          </Card>
        </div>
      </div>
    </template>
  </div>
</template>

<style scoped>
.trip-detail {
  padding: var(--spacing-4) 0;
}

.trip-detail__loading {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 40vh;
}

.trip-detail__not-found {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 40vh;
  gap: var(--spacing-4);
  color: var(--muted);
}

.not-found-icon {
  font-size: 4rem;
}

.trip-detail__topbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: var(--spacing-5);
}

.trip-detail__layout {
  display: grid;
  grid-template-columns: 1fr 340px;
  gap: var(--spacing-5);
  align-items: start;
}

@media (max-width: 900px) {
  .trip-detail__layout {
    grid-template-columns: 1fr;
  }
}

.trip-route {
  display: flex;
  align-items: center;
  gap: var(--spacing-4);
  margin-bottom: var(--spacing-2);
}

.trip-route__city {
  display: flex;
  align-items: center;
  gap: var(--spacing-2);
  flex: 1;
}

.trip-route__city .pi {
  font-size: 1.5rem;
  color: var(--muted);
}

.trip-route__city--dest .pi {
  color: var(--mark);
}

.route-label {
  font-size: var(--fs-small);
  color: var(--muted);
}

.route-city {
  font-size: 1.25rem;
  font-weight: 700;
}

.trip-route__separator {
  display: flex;
  align-items: center;
  gap: var(--spacing-2);
  color: var(--muted);
  flex-shrink: 0;
}

.route-line {
  width: 32px;
  height: 1px;
  background: var(--rule);
}

.trip-detail__info-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: var(--spacing-4);
}

@media (max-width: 600px) {
  .trip-detail__info-grid {
    grid-template-columns: 1fr;
  }
}

.info-item {
  display: flex;
  align-items: flex-start;
  gap: var(--spacing-3);
}

.info-item__icon {
  font-size: 1.25rem;
  color: var(--mark);
  margin-top: 2px;
}

.info-item__label {
  font-size: var(--fs-small);
  color: var(--muted);
}

.info-item__value {
  font-weight: 500;
}

.price-value {
  font-size: 1.1rem;
  font-weight: 700;
  color: var(--mark);
}

.description-label {
  font-size: var(--fs-small);
  color: var(--muted);
  margin: 0 0 var(--spacing-2);
}

.description-text {
  margin: 0;
  line-height: 1.6;
}

.driver-card {
  margin-bottom: var(--spacing-4);
}

.driver-card__title {
  font-weight: 600;
  font-size: var(--fs-body);
}

.driver-card__body {
  display: flex;
  align-items: center;
  gap: var(--spacing-4);
}

.driver-card__info {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-1);
}

.driver-card__name {
  font-weight: 600;
  font-size: 1.05rem;
}

.driver-card__stars {
  display: flex;
  align-items: center;
  gap: 2px;
}

.star-icon {
  font-size: 0.875rem;
  color: #f59e0b;
}

.rating-value {
  font-size: var(--fs-small);
  color: var(--muted);
  margin-left: var(--spacing-1);
}

.rating-none {
  font-size: var(--fs-small);
  color: var(--muted);
}

.action-card {
  margin-bottom: var(--spacing-4);
}

.action-card__btn {
  width: 100%;
}

.booking-form {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-3);
}

.booking-form__field {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-1);
}

.booking-form__label {
  font-size: var(--fs-small);
  font-weight: 500;
  color: var(--muted);
}

.booking-form__input {
  width: 100%;
}

.booking-form__textarea {
  width: 100%;
}

.booking-form__submit {
  width: 100%;
}

.booking-status {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-2);
}

.booking-status__label {
  font-size: var(--fs-small);
  color: var(--muted);
  margin: 0;
}

.no-seats {
  color: var(--muted);
  text-align: center;
  margin: 0;
}

.login-prompt {
  color: var(--muted);
  font-size: var(--fs-small);
  margin: 0 0 var(--spacing-3);
}
</style>
