<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useTripsStore } from '../stores/trips.store'
import { useAuthStore } from '../stores/auth.store'
import { useBookingsStore } from '../stores/bookings.store'
import { tripsApi } from '../api/trips.api'
import { useToast } from 'primevue/usetoast'
import LoadingSpinner from '../components/LoadingSpinner.vue'
import TripStatusBadge from '../components/TripStatusBadge.vue'
import TripBookingsList from '../components/TripBookingsList.vue'
import BookSeatsDialog from '../components/BookSeatsDialog.vue'
import RatingCard from '../components/RatingCard.vue'
import RatingForm from '../components/RatingForm.vue'
import UserAvatar from '../components/UserAvatar.vue'
import UserRatingSummary from '../components/UserRatingSummary.vue'

const route = useRoute()
const router = useRouter()
const tripsStore = useTripsStore()
const authStore = useAuthStore()
const bookingsStore = useBookingsStore()
const toast = useToast()

const ratings = ref([])
const showBookDialog = ref(false)
const ratingsLoading = ref(false)

const trip = computed(() => tripsStore.currentTrip)

const isOwnTrip = computed(
  () => authStore.user && trip.value?.driver?.id === authStore.user?.id
)

const canBook = computed(
  () =>
    authStore.isAuthenticated &&
    !isOwnTrip.value &&
    trip.value?.status === 'SCHEDULED' &&
    availableSeats.value > 0
)

const availableSeats = computed(
  () => (trip.value?.totalSeats ?? 0) - (trip.value?.bookedSeats ?? 0)
)

const departureFormatted = computed(() => {
  if (!trip.value?.departureTime) return ''
  return new Date(trip.value.departureTime).toLocaleString('bg-BG', {
    weekday: 'long',
    day: '2-digit',
    month: 'long',
    year: 'numeric',
    hour: '2-digit',
    minute: '2-digit',
  })
})

const arrivalFormatted = computed(() => {
  if (!trip.value?.estimatedArrival) return null
  return new Date(trip.value.estimatedArrival).toLocaleString('bg-BG', {
    hour: '2-digit',
    minute: '2-digit',
  })
})

const driverName = computed(() => {
  const d = trip.value?.driver
  if (!d) return 'Непознат'
  return d.firstName && d.lastName ? `${d.firstName} ${d.lastName}` : d.username
})

onMounted(async () => {
  await tripsStore.fetchTrip(route.params.id)
  if (isOwnTrip.value) {
    await bookingsStore.fetchTripBookings(route.params.id)
  }
  await loadRatings()
})

async function loadRatings() {
  ratingsLoading.value = true
  try {
    const res = await tripsApi.getRatings(route.params.id)
    ratings.value = res.data
  } catch {
    // ratings optional
  } finally {
    ratingsLoading.value = false
  }
}

async function handleBook({ seatsBooked, message }) {
  showBookDialog.value = false
  try {
    await bookingsStore.bookTrip(route.params.id, { seatsBooked, message })
    toast.add({
      severity: 'success',
      summary: 'Успех',
      detail: 'Резервацията е изпратена за одобрение',
      life: 4000,
    })
    await tripsStore.fetchTrip(route.params.id)
  } catch (err) {
    toast.add({
      severity: 'error',
      summary: 'Грешка',
      detail: err.response?.data?.message ?? 'Неуспешна резервация',
      life: 4000,
    })
  }
}

async function handleApprove(bookingId) {
  try {
    await bookingsStore.approveBooking(bookingId)
    toast.add({ severity: 'success', summary: 'Одобрено', life: 3000 })
    await tripsStore.fetchTrip(route.params.id)
  } catch {
    toast.add({ severity: 'error', summary: 'Грешка', life: 3000 })
  }
}

async function handleReject(bookingId) {
  try {
    await bookingsStore.rejectBooking(bookingId)
    toast.add({ severity: 'info', summary: 'Отхвърлено', life: 3000 })
    await tripsStore.fetchTrip(route.params.id)
  } catch {
    toast.add({ severity: 'error', summary: 'Грешка', life: 3000 })
  }
}
</script>

<template>
  <div class="trip-detail-page">
    <LoadingSpinner v-if="tripsStore.loading" />

    <template v-else-if="trip">
      <div class="page-inner">
        <!-- Back + actions -->
        <div class="page-top-bar">
          <Button
            text
            icon="pi pi-arrow-left"
            label="Назад"
            @click="router.back()"
          />
          <div v-if="isOwnTrip" class="owner-actions">
            <Button
              label="Редактирай"
              icon="pi pi-pencil"
              outlined
              size="small"
              @click="router.push(`/trips/${trip.id}/edit`)"
            />
          </div>
        </div>

        <div class="detail-layout">
          <!-- Main info -->
          <div class="detail-main">
            <!-- Route card -->
            <Card class="route-card">
              <template #content>
                <div class="route-header">
                  <TripStatusBadge :status="trip.status" />
                  <span class="trip-date">{{ departureFormatted }}</span>
                </div>

                <div class="route-display">
                  <div class="route-point">
                    <div class="point-dot origin-dot" />
                    <div class="point-info">
                      <span class="point-city">{{ trip.originCity }}</span>
                      <span v-if="trip.originAddress" class="point-address">{{ trip.originAddress }}</span>
                    </div>
                  </div>
                  <div class="route-line-vertical" />
                  <div class="route-point">
                    <div class="point-dot dest-dot" />
                    <div class="point-info">
                      <span class="point-city">{{ trip.destinationCity }}</span>
                      <span v-if="trip.destinationAddress" class="point-address">{{ trip.destinationAddress }}</span>
                      <span v-if="arrivalFormatted" class="point-arrival">Прибл. пристигане: {{ arrivalFormatted }}</span>
                    </div>
                  </div>
                </div>

                <div class="trip-stats">
                  <div class="stat">
                    <i class="pi pi-wallet" />
                    <span class="stat-value">{{ trip.pricePerSeat }} лв.</span>
                    <span class="stat-label">на място</span>
                  </div>
                  <div class="stat">
                    <i class="pi pi-users" />
                    <span class="stat-value">{{ availableSeats }}</span>
                    <span class="stat-label">свободни места</span>
                  </div>
                  <div v-if="trip.smokingAllowed !== undefined" class="stat">
                    <i class="pi pi-ban" />
                    <span class="stat-label">{{ trip.smokingAllowed ? 'Пушенето разрешено' : 'Непушещи' }}</span>
                  </div>
                  <div v-if="trip.petsAllowed !== undefined" class="stat">
                    <i class="pi pi-heart" />
                    <span class="stat-label">{{ trip.petsAllowed ? 'Домашни любимци OK' : 'Без домашни любимци' }}</span>
                  </div>
                </div>

                <div v-if="trip.description" class="trip-description">
                  <i class="pi pi-info-circle" />
                  <p>{{ trip.description }}</p>
                </div>

                <!-- Car details -->
                <div v-if="trip.car" class="car-info">
                  <i class="pi pi-car" />
                  <span>{{ trip.car.make }} {{ trip.car.model }} ({{ trip.car.year }}) — {{ trip.car.color }} — {{ trip.car.licensePlate }}</span>
                </div>
              </template>
            </Card>

            <!-- Book button -->
            <div v-if="!isOwnTrip" class="book-section">
              <Button
                v-if="canBook"
                label="Резервирай места"
                icon="pi pi-ticket"
                size="large"
                class="w-full"
                @click="showBookDialog = true"
              />
              <Message v-else-if="!authStore.isAuthenticated" severity="info" :closable="false">
                <RouterLink to="/login">Влезте</RouterLink> за да резервирате места
              </Message>
              <Message v-else-if="availableSeats === 0" severity="warn" :closable="false">
                Няма свободни места
              </Message>
            </div>

            <!-- Driver's bookings -->
            <Card v-if="isOwnTrip" class="bookings-section">
              <template #title>
                <i class="pi pi-list" /> Резервации
              </template>
              <template #content>
                <TripBookingsList
                  :bookings="bookingsStore.tripBookings"
                  @approve="handleApprove"
                  @reject="handleReject"
                />
              </template>
            </Card>

            <!-- Ratings -->
            <Card class="ratings-section">
              <template #title>
                <i class="pi pi-star" /> Отзиви
              </template>
              <template #content>
                <div v-if="ratingsLoading" class="ratings-loading">
                  <i class="pi pi-spin pi-spinner" /> Зарежда се...
                </div>
                <div v-else-if="ratings.length === 0" class="ratings-empty">
                  Все още няма отзиви за това пътуване
                </div>
                <div v-else class="ratings-list">
                  <RatingCard
                    v-for="rating in ratings"
                    :key="rating.id"
                    :rating="rating"
                  />
                </div>

                <div
                  v-if="authStore.isAuthenticated && !isOwnTrip && trip.status === 'COMPLETED'"
                  class="rating-form-wrapper"
                >
                  <Divider />
                  <h4>Добавете оценка</h4>
                  <RatingForm
                    :tripId="trip.id"
                    :ratedUser="trip.driver"
                    @submitted="loadRatings"
                  />
                </div>
              </template>
            </Card>
          </div>

          <!-- Driver sidebar -->
          <aside class="driver-sidebar">
            <Card class="driver-card">
              <template #title>Шофьор</template>
              <template #content>
                <div class="driver-info">
                  <UserAvatar :user="trip.driver" size="4rem" />
                  <div class="driver-details">
                    <RouterLink
                      :to="`/users/${trip.driver?.id}`"
                      class="driver-name"
                    >
                      {{ driverName }}
                    </RouterLink>
                    <UserRatingSummary v-if="trip.driver" :user="trip.driver" />
                  </div>
                </div>
              </template>
            </Card>
          </aside>
        </div>
      </div>
    </template>

    <EmptyState
      v-else
      message="Пътуването не е намерено"
      icon="pi pi-exclamation-triangle"
    />

    <BookSeatsDialog
      v-model:visible="showBookDialog"
      :trip="trip"
      @confirm="handleBook"
      @cancel="showBookDialog = false"
    />
  </div>
</template>

<style scoped>
.trip-detail-page {
  background: var(--p-surface-50);
  min-height: calc(100vh - 128px);
}

.page-inner {
  max-width: 1100px;
  margin: 0 auto;
  padding: 1.5rem;
}

.page-top-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1.5rem;
}

.owner-actions {
  display: flex;
  gap: 0.5rem;
}

.detail-layout {
  display: grid;
  grid-template-columns: 1fr 280px;
  gap: 1.5rem;
  align-items: start;
}

.detail-main {
  display: flex;
  flex-direction: column;
  gap: 1.25rem;
}

/* Route card */
.route-header {
  display: flex;
  align-items: center;
  gap: 1rem;
  margin-bottom: 1.5rem;
  flex-wrap: wrap;
}

.trip-date {
  font-size: 0.9rem;
  color: var(--p-text-muted-color);
}

.route-display {
  position: relative;
  padding-left: 1.5rem;
  margin-bottom: 1.5rem;
}

.route-point {
  display: flex;
  align-items: flex-start;
  gap: 0.75rem;
  position: relative;
  padding: 0.25rem 0;
}

.point-dot {
  width: 14px;
  height: 14px;
  border-radius: 50%;
  flex-shrink: 0;
  margin-top: 0.25rem;
}

.origin-dot {
  background: var(--p-primary-color);
}

.dest-dot {
  background: var(--p-red-500);
}

.route-line-vertical {
  width: 2px;
  height: 2rem;
  background: var(--p-surface-300);
  margin-left: 6px;
  margin: 0.25rem 0 0.25rem 6px;
}

.point-info {
  display: flex;
  flex-direction: column;
  gap: 0.15rem;
}

.point-city {
  font-weight: 700;
  font-size: 1.15rem;
}

.point-address,
.point-arrival {
  font-size: 0.85rem;
  color: var(--p-text-muted-color);
}

.trip-stats {
  display: flex;
  gap: 1.5rem;
  flex-wrap: wrap;
  padding: 1rem 0;
  border-top: 1px solid var(--p-surface-100);
  border-bottom: 1px solid var(--p-surface-100);
  margin-bottom: 1rem;
}

.stat {
  display: flex;
  align-items: center;
  gap: 0.4rem;
  font-size: 0.9rem;
}

.stat-value {
  font-weight: 700;
  color: var(--p-primary-color);
}

.stat-label {
  color: var(--p-text-muted-color);
}

.trip-description {
  display: flex;
  gap: 0.5rem;
  align-items: flex-start;
  font-size: 0.9rem;
  color: var(--p-text-muted-color);
  margin-bottom: 0.75rem;
}

.car-info {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  font-size: 0.875rem;
  color: var(--p-text-muted-color);
  background: var(--p-surface-50);
  padding: 0.5rem 0.75rem;
  border-radius: 6px;
}

.book-section {
  padding: 0.5rem 0;
}

.ratings-loading,
.ratings-empty {
  color: var(--p-text-muted-color);
  font-size: 0.9rem;
  text-align: center;
  padding: 1rem;
}

.ratings-list {
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
}

.rating-form-wrapper {
  margin-top: 1rem;
}

.rating-form-wrapper h4 {
  margin-bottom: 0.75rem;
  font-size: 0.95rem;
}

/* Driver sidebar */
.driver-sidebar {
  position: sticky;
  top: 80px;
}

.driver-info {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 0.75rem;
  text-align: center;
}

.driver-details {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 0.4rem;
}

.driver-name {
  font-size: 1.05rem;
  font-weight: 600;
  text-decoration: none;
  color: var(--p-primary-color);
}

.driver-name:hover {
  text-decoration: underline;
}

@media (max-width: 768px) {
  .detail-layout {
    grid-template-columns: 1fr;
  }

  .driver-sidebar {
    position: static;
    order: -1;
  }
}
</style>
