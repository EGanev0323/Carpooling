<script setup>
import { onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useBookingsStore } from '../stores/bookings.store'
import { useToast } from 'primevue/usetoast'
import LoadingSpinner from '../components/LoadingSpinner.vue'
import TripBookingsList from '../components/TripBookingsList.vue'

const route = useRoute()
const router = useRouter()
const bookingsStore = useBookingsStore()
const toast = useToast()

onMounted(async () => {
  await bookingsStore.fetchTripBookings(route.params.id)
})

async function handleApprove(id) {
  try {
    await bookingsStore.approveBooking(id)
    toast.add({ severity: 'success', summary: 'Резервацията е одобрена', life: 3000 })
  } catch {
    toast.add({ severity: 'error', summary: 'Грешка', life: 3000 })
  }
}

async function handleReject(id) {
  try {
    await bookingsStore.rejectBooking(id)
    toast.add({ severity: 'info', summary: 'Резервацията е отхвърлена', life: 3000 })
  } catch {
    toast.add({ severity: 'error', summary: 'Грешка', life: 3000 })
  }
}
</script>

<template>
  <div class="trip-bookings-page">
    <div class="page-inner">
      <div class="page-header">
        <Button
          text
          icon="pi pi-arrow-left"
          label="Назад към пътуването"
          @click="router.push(`/trips/${route.params.id}`)"
        />
        <h1>Резервации за пътуването</h1>
      </div>

      <LoadingSpinner v-if="bookingsStore.loading" />

      <TripBookingsList
        v-else
        :bookings="bookingsStore.tripBookings"
        @approve="handleApprove"
        @reject="handleReject"
      />
    </div>
  </div>
</template>

<style scoped>
.trip-bookings-page {
  background: var(--p-surface-50);
  min-height: calc(100vh - 128px);
}

.page-inner {
  max-width: 800px;
  margin: 0 auto;
  padding: 2rem 1.5rem;
}

.page-header {
  display: flex;
  align-items: center;
  gap: 1rem;
  margin-bottom: 1.5rem;
  flex-wrap: wrap;
}

.page-header h1 {
  font-size: 1.5rem;
  font-weight: 700;
}
</style>
