<script setup>
import { computed } from 'vue'

const props = defineProps({
  status: {
    type: String,
    required: true,
  },
})

const severityMap = {
  SCHEDULED: 'info',
  ACTIVE: 'success',
  COMPLETED: 'secondary',
  CANCELLED: 'danger',
}

const labelMap = {
  SCHEDULED: 'Планирано',
  ACTIVE: 'Активно',
  COMPLETED: 'Завършено',
  CANCELLED: 'Отменено',
}

const severity = computed(() => severityMap[props.status] ?? 'info')
const label = computed(() => labelMap[props.status] ?? props.status)
</script>

<template>
  <Tag :severity="severity" :value="label" />
</template>
