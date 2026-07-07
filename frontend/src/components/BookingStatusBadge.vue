<script setup>
import { computed } from 'vue'

const props = defineProps({
  status: {
    type: String,
    required: true,
  },
})

const severityMap = {
  PENDING: 'warn',
  APPROVED: 'success',
  REJECTED: 'danger',
  CANCELLED: 'secondary',
}

const labelMap = {
  PENDING: 'Изчакващо',
  APPROVED: 'Одобрено',
  REJECTED: 'Отхвърлено',
  CANCELLED: 'Отменено',
}

const severity = computed(() => severityMap[props.status] ?? 'info')
const label = computed(() => labelMap[props.status] ?? props.status)
</script>

<template>
  <Tag :severity="severity" :value="label" />
</template>
