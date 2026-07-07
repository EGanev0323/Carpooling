<script setup>
import { computed } from 'vue'
import UserAvatar from './UserAvatar.vue'

const props = defineProps({
  rating: {
    type: Object,
    required: true,
  },
})

const dateFormatted = computed(() => {
  if (!props.rating.createdAt) return ''
  return new Date(props.rating.createdAt).toLocaleDateString('bg-BG', {
    day: '2-digit',
    month: '2-digit',
    year: 'numeric',
  })
})

const stars = computed(() =>
  Array.from({ length: 5 }, (_, i) => i < props.rating.score)
)
</script>

<template>
  <div class="rating-card">
    <div class="rating-header">
      <UserAvatar :user="rating.rater" size="2.25rem" />
      <div class="rater-info">
        <span class="rater-name">
          {{ rating.rater?.firstName && rating.rater?.lastName
            ? `${rating.rater.firstName} ${rating.rater.lastName}`
            : rating.rater?.username ?? 'Анонимен' }}
        </span>
        <div class="rating-stars">
          <i
            v-for="(filled, i) in stars"
            :key="i"
            :class="filled ? 'pi pi-star-fill' : 'pi pi-star'"
            class="star"
          />
          <span class="score">{{ rating.score }}/5</span>
        </div>
      </div>
      <span class="rating-date">{{ dateFormatted }}</span>
    </div>

    <p v-if="rating.comment" class="rating-comment">{{ rating.comment }}</p>
  </div>
</template>

<style scoped>
.rating-card {
  background: var(--p-surface-50);
  border: 1px solid var(--p-surface-200);
  border-radius: 8px;
  padding: 1rem;
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.rating-header {
  display: flex;
  align-items: flex-start;
  gap: 0.75rem;
}

.rater-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 0.2rem;
}

.rater-name {
  font-weight: 600;
  font-size: 0.9rem;
}

.rating-stars {
  display: flex;
  align-items: center;
  gap: 0.15rem;
}

.star {
  color: var(--p-yellow-500);
  font-size: 0.8rem;
}

.score {
  font-size: 0.8rem;
  color: var(--p-text-muted-color);
  margin-left: 0.25rem;
}

.rating-date {
  font-size: 0.8rem;
  color: var(--p-text-muted-color);
  white-space: nowrap;
}

.rating-comment {
  font-size: 0.875rem;
  color: var(--p-text-color);
  line-height: 1.5;
  padding-left: 3rem;
}
</style>
