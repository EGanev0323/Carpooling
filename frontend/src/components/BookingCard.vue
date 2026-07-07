<script setup>
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import BookingStatusBadge from './BookingStatusBadge.vue'

const props = defineProps({
  booking: {
    type: Object,
    required: true,
  },
})

const emit = defineEmits(['cancel'])

const router = useRouter()

const trip = computed(() => props.booking.trip)

const departureFormatted = computed(() => {
  if (!trip.value?.departureTime) return ''
  return new Date(trip.value.departureTime).toLocaleString('bg-BG', {
    day: '2-digit',
    month: '2-digit',
    year: 'numeric',
    hour: '2-digit',
    minute: '2-digit',
  })
})

const canCancel = computed(
  () => props.booking.status === 'PENDING' || props.booking.status === 'APPROVED'
)
</script>

<template>
  <Card class="booking-card">
    <template #content>
      <div class="booking-route" @click="router.push(`/trips/${trip?.id}`)" style="cursor: pointer">
        <span class="city">{{ trip?.originCity }}</span>
        <i class="pi pi-arrow-right route-arrow" />
        <span class="city">{{ trip?.destinationCity }}</span>
      </div>

      <div class="booking-meta">
        <span>
          <i class="pi pi-calendar" /> {{ departureFormatted }}
        </span>
        <span>
          <i class="pi pi-users" /> {{ booking.seatsBooked }} места
        </span>
        <span>
          <i class="pi pi-wallet" /> {{ (trip?.pricePerSeat ?? 0) * booking.seatsBooked }} лв.
        </span>
      </div>

      <div class="booking-footer">
        <BookingStatusBadge :status="booking.status" />
        <Button
          v-if="canCancel"
          label="Отмени"
          icon="pi pi-times"
          size="small"
          severity="danger"
          text
          @click="emit('cancel', booking.id)"
        />
      </div>
    </template>
  </Card>
</template>

<style scoped>
.booking-card {
  border: 1px solid var(--p-surface-200);
}

.booking-route {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  margin-bottom: 0.75rem;
}

.city {
  font-weight: 600;
  font-size: 1rem;
}

.route-arrow {
  color: var(--p-text-muted-color);
}

.booking-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 0.75rem;
  font-size: 0.875rem;
  color: var(--p-text-muted-color);
  margin-bottom: 0.75rem;
}

.booking-meta span {
  display: flex;
  align-items: center;
  gap: 0.3rem;
}

.booking-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  border-top: 1px solid var(--p-surface-100);
  padding-top: 0.75rem;
}
</style>
