<script setup>
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import UserAvatar from './UserAvatar.vue'

const props = defineProps({
  trip: {
    type: Object,
    required: true,
  },
})

const router = useRouter()

const departureFormatted = computed(() => {
  if (!props.trip.departureTime) return ''
  return new Date(props.trip.departureTime).toLocaleString('bg-BG', {
    day: '2-digit',
    month: '2-digit',
    year: 'numeric',
    hour: '2-digit',
    minute: '2-digit',
  })
})

const availableSeats = computed(
  () => (props.trip.totalSeats ?? 0) - (props.trip.bookedSeats ?? 0)
)

const driverName = computed(() => {
  const d = props.trip.driver
  if (!d) return 'Непознат'
  return d.firstName && d.lastName ? `${d.firstName} ${d.lastName}` : d.username
})

const driverRating = computed(() => {
  const r = props.trip.driver?.averageRating ?? props.trip.driver?.avgRating ?? 0
  return parseFloat(r).toFixed(1)
})
</script>

<template>
  <Card
    class="trip-card"
    style="cursor: pointer"
    @click="router.push(`/trips/${trip.id}`)"
  >
    <template #content>
      <div class="trip-route">
        <div class="city origin">
          <i class="pi pi-map-marker" />
          <span>{{ trip.originCity }}</span>
        </div>
        <div class="route-line">
          <span class="route-dash" />
          <i class="pi pi-arrow-right" />
          <span class="route-dash" />
        </div>
        <div class="city destination">
          <i class="pi pi-map-marker" style="color: var(--p-red-500)" />
          <span>{{ trip.destinationCity }}</span>
        </div>
      </div>

      <div class="trip-meta">
        <span class="meta-item">
          <i class="pi pi-calendar" />
          {{ departureFormatted }}
        </span>
        <span class="meta-item price">
          <i class="pi pi-wallet" />
          {{ trip.pricePerSeat }} лв./място
        </span>
        <span class="meta-item" :class="{ 'no-seats': availableSeats === 0 }">
          <i class="pi pi-users" />
          {{ availableSeats }} свободни места
        </span>
      </div>

      <div class="trip-driver">
        <UserAvatar :user="trip.driver" size="2rem" />
        <span class="driver-name">{{ driverName }}</span>
        <span class="driver-rating">
          <i class="pi pi-star-fill" style="color: var(--p-yellow-500)" />
          {{ driverRating }}
        </span>
      </div>

      <div v-if="trip.smokingAllowed || trip.petsAllowed" class="trip-tags">
        <Tag v-if="trip.smokingAllowed" value="Пушене" severity="warn" icon="pi pi-ban" />
        <Tag v-if="trip.petsAllowed" value="Домашни любимци" severity="info" icon="pi pi-heart" />
      </div>
    </template>
  </Card>
</template>

<style scoped>
.trip-card {
  transition: box-shadow 0.2s, transform 0.2s;
  border: 1px solid var(--p-surface-200);
}

.trip-card:hover {
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.12);
  transform: translateY(-2px);
}

.trip-route {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  margin-bottom: 1rem;
}

.city {
  display: flex;
  align-items: center;
  gap: 0.35rem;
  font-weight: 600;
  font-size: 1.05rem;
}

.route-line {
  flex: 1;
  display: flex;
  align-items: center;
  gap: 0.25rem;
  color: var(--p-text-muted-color);
}

.route-dash {
  flex: 1;
  height: 1px;
  background: var(--p-surface-300);
}

.trip-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 0.75rem;
  margin-bottom: 1rem;
  font-size: 0.875rem;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 0.3rem;
  color: var(--p-text-muted-color);
}

.meta-item.price {
  color: var(--p-primary-color);
  font-weight: 600;
}

.meta-item.no-seats {
  color: var(--p-red-500);
}

.trip-driver {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  font-size: 0.875rem;
  border-top: 1px solid var(--p-surface-100);
  padding-top: 0.75rem;
}

.driver-name {
  font-weight: 500;
  flex: 1;
}

.driver-rating {
  display: flex;
  align-items: center;
  gap: 0.25rem;
  font-weight: 600;
}

.trip-tags {
  display: flex;
  gap: 0.5rem;
  margin-top: 0.75rem;
  flex-wrap: wrap;
}
</style>
