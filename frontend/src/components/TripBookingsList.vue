<script setup>
import BookingStatusBadge from './BookingStatusBadge.vue'
import UserAvatar from './UserAvatar.vue'

defineProps({
  bookings: {
    type: Array,
    default: () => [],
  },
})

const emit = defineEmits(['approve', 'reject', 'cancel'])

function passengerName(booking) {
  const p = booking.passenger
  if (!p) return 'Непознат'
  return p.firstName && p.lastName ? `${p.firstName} ${p.lastName}` : p.username
}
</script>

<template>
  <div class="bookings-list">
    <div v-if="bookings.length === 0" class="empty-notice">
      <i class="pi pi-inbox" /> Няма резервации за това пътуване
    </div>

    <div v-for="booking in bookings" :key="booking.id" class="booking-item">
      <div class="booking-passenger">
        <UserAvatar :user="booking.passenger" size="2.5rem" />
        <div class="passenger-info">
          <span class="passenger-name">{{ passengerName(booking) }}</span>
          <span class="passenger-seats">
            <i class="pi pi-users" /> {{ booking.seatsBooked }} места
          </span>
        </div>
      </div>

      <div v-if="booking.message" class="booking-message">
        <i class="pi pi-comment" /> {{ booking.message }}
      </div>

      <div class="booking-footer">
        <BookingStatusBadge :status="booking.status" />
        <div class="booking-actions">
          <Button
            v-if="booking.status === 'PENDING'"
            label="Одобри"
            icon="pi pi-check"
            size="small"
            severity="success"
            @click="emit('approve', booking.id)"
          />
          <Button
            v-if="booking.status === 'PENDING' || booking.status === 'APPROVED'"
            label="Откажи"
            icon="pi pi-times"
            size="small"
            severity="danger"
            text
            @click="emit('reject', booking.id)"
          />
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.bookings-list {
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
}

.empty-notice {
  text-align: center;
  color: var(--p-text-muted-color);
  padding: 2rem;
  display: flex;
  align-items: center;
  gap: 0.5rem;
  justify-content: center;
}

.booking-item {
  background: var(--p-surface-50);
  border: 1px solid var(--p-surface-200);
  border-radius: 8px;
  padding: 1rem;
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
}

.booking-passenger {
  display: flex;
  align-items: center;
  gap: 0.75rem;
}

.passenger-info {
  display: flex;
  flex-direction: column;
  gap: 0.2rem;
}

.passenger-name {
  font-weight: 600;
}

.passenger-seats {
  font-size: 0.85rem;
  color: var(--p-text-muted-color);
  display: flex;
  align-items: center;
  gap: 0.3rem;
}

.booking-message {
  font-size: 0.875rem;
  color: var(--p-text-muted-color);
  display: flex;
  align-items: flex-start;
  gap: 0.4rem;
  font-style: italic;
}

.booking-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 0.5rem;
}

.booking-actions {
  display: flex;
  gap: 0.5rem;
}
</style>
