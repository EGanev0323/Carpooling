<script setup>
const props = defineProps({
  totalPages: {
    type: Number,
    required: true,
  },
  modelValue: {
    type: Number,
    default: 0,
  },
  rowsPerPage: {
    type: Number,
    default: 10,
  },
})

const emit = defineEmits(['update:modelValue'])

function onPageChange(event) {
  emit('update:modelValue', event.page)
}
</script>

<template>
  <div v-if="totalPages > 1" class="pagination-bar">
    <Paginator
      :rows="rowsPerPage"
      :totalRecords="totalPages * rowsPerPage"
      :first="modelValue * rowsPerPage"
      @page="onPageChange"
      template="FirstPageLink PrevPageLink PageLinks NextPageLink LastPageLink"
    />
  </div>
</template>

<style scoped>
.pagination-bar {
  display: flex;
  justify-content: center;
  padding: 1.5rem 0;
}
</style>
