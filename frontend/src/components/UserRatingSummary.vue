<script setup>
import { computed } from 'vue'

const props = defineProps({
  user: {
    type: Object,
    required: true,
  },
})

const avgRating = computed(() => {
  const r = props.user?.averageRating ?? props.user?.avgRating ?? 0
  return parseFloat(r).toFixed(1)
})

const ratingCount = computed(() => props.user?.ratingCount ?? props.user?.ratingsCount ?? 0)
const tripsCount = computed(() => props.user?.tripsCount ?? props.user?.completedTrips ?? 0)

const stars = computed(() => {
  const val = Math.round(parseFloat(avgRating.value))
  return Array.from({ length: 5 }, (_, i) => i < val)
})
</script>

<template>
  <div class="rating-summary">
    <div class="stars">
      <i
        v-for="(filled, i) in stars"
        :key="i"
        :class="filled ? 'pi pi-star-fill' : 'pi pi-star'"
        class="star-icon"
      />
    </div>
    <span class="rating-value">{{ avgRating }}</span>
    <span class="rating-count">({{ ratingCount }} отзива)</span>
    <span v-if="tripsCount" class="trips-count">
      <i class="pi pi-car" /> {{ tripsCount }} пътувания
    </span>
  </div>
</template>

<style scoped>
.rating-summary {
  display: flex;
  align-items: center;
  gap: 0.4rem;
  flex-wrap: wrap;
  font-size: 0.9rem;
}

.stars {
  display: flex;
  gap: 0.1rem;
}

.star-icon {
  color: var(--p-yellow-500);
  font-size: 0.9rem;
}

.rating-value {
  font-weight: 600;
}

.rating-count,
.trips-count {
  color: var(--p-text-muted-color);
}
</style>
