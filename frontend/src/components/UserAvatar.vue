<script setup>
import { computed } from 'vue'

const props = defineProps({
  user: {
    type: Object,
    default: null,
  },
  size: {
    type: String,
    default: '2.5rem',
  },
})

const initials = computed(() => {
  if (!props.user) return '?'
  const first = props.user.firstName?.[0] ?? ''
  const last = props.user.lastName?.[0] ?? ''
  return (first + last).toUpperCase() || props.user.username?.[0]?.toUpperCase() || '?'
})
</script>

<template>
  <div
    class="user-avatar"
    :style="{ width: size, height: size, fontSize: `calc(${size} * 0.4)` }"
  >
    <img
      v-if="user?.avatarUrl"
      :src="user.avatarUrl"
      :alt="initials"
      class="avatar-img"
    />
    <span v-else class="avatar-initials">{{ initials }}</span>
  </div>
</template>

<style scoped>
.user-avatar {
  border-radius: 50%;
  overflow: hidden;
  background: var(--p-primary-color);
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  font-weight: 600;
}

.avatar-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.avatar-initials {
  user-select: none;
}
</style>
