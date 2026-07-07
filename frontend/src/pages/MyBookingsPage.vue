<script setup>
import { onMounted } from 'vue'
import { useAuthStore } from '../stores/auth.store'
import { useBookingsStore } from '../stores/bookings.store'
import { useToast } from 'primevue/usetoast'
import LoadingSpinner from '../components/LoadingSpinner.vue'
import EmptyState from '../components/EmptyState.vue'
import BookingCard from '../components/BookingCard.vue'

const authStore = useAuthStore()
const bookingsStore = useBookingsStore()
const toast = useToast()

onMounted(async () => {
  await bookingsStore.fetchMyBookings(authStore.user.id)
})

async function handleCancel(bookingId) {
  try {
    await bookingsStore.cancelBooking(bookingId)
    toast.add({ severity: 'info', summary: 'Резервацията е отменена', life: 3000 })
  } catch (err) {
    toast.add({
      severity: 'error',
      summary: 'Грешка при отмяна',
      detail: err.response?.data?.message,
      life: 3000,
    })
  }
}
</script>

<template>
  <div class="my-bookings-page">
    <div class="page-inner">
      <h1 class="page-title">Моите резервации</h1>

      <LoadingSpinner v-if="bookingsStore.loading" />

      <EmptyState
        v-else-if="bookingsStore.myBookings.length === 0"
        message="Нямате направени резервации"
        icon="pi pi-ticket"
      />

      <div v-else class="bookings-grid">
        <BookingCard
          v-for="booking in bookingsStore.myBookings"
          :key="booking.id"
          :booking="booking"
          @cancel="handleCancel"
        />
      </div>
    </div>
  </div>
</template>

<style scoped>
.my-bookings-page {
  background: var(--p-surface-50);
  min-height: calc(100vh - 128px);
}

.page-inner {
  max-width: 900px;
  margin: 0 auto;
  padding: 2rem 1.5rem;
}

.page-title {
  font-size: 1.75rem;
  font-weight: 700;
  margin-bottom: 1.5rem;
}

.bookings-grid {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}
</style>
