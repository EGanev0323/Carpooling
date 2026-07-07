<script setup>
defineProps({
  car: {
    type: Object,
    required: true,
  },
})

const emit = defineEmits(['edit', 'delete'])
</script>

<template>
  <Card class="car-card">
    <template #header>
      <div class="car-header">
        <i class="pi pi-car car-icon" />
      </div>
    </template>
    <template #title>
      {{ car.make }} {{ car.model }}
      <span class="car-year">({{ car.year }})</span>
    </template>
    <template #content>
      <div class="car-details">
        <div class="detail-row">
          <i class="pi pi-palette" />
          <span>{{ car.color }}</span>
        </div>
        <div class="detail-row">
          <i class="pi pi-id-card" />
          <span>{{ car.licensePlate }}</span>
        </div>
        <div class="detail-row">
          <i class="pi pi-users" />
          <span>{{ car.totalSeats }} места</span>
        </div>
      </div>
    </template>
    <template #footer>
      <div class="car-actions">
        <Button
          label="Редактирай"
          icon="pi pi-pencil"
          size="small"
          text
          @click="emit('edit', car)"
        />
        <Button
          label="Изтрий"
          icon="pi pi-trash"
          size="small"
          severity="danger"
          text
          @click="emit('delete', car.id)"
        />
      </div>
    </template>
  </Card>
</template>

<style scoped>
.car-card {
  border: 1px solid var(--p-surface-200);
}

.car-header {
  background: var(--p-primary-50, #eff6ff);
  padding: 1.5rem;
  display: flex;
  justify-content: center;
  border-radius: 8px 8px 0 0;
}

.car-icon {
  font-size: 3rem;
  color: var(--p-primary-color);
}

.car-year {
  font-size: 0.85rem;
  color: var(--p-text-muted-color);
  font-weight: normal;
}

.car-details {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
  font-size: 0.9rem;
}

.detail-row {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  color: var(--p-text-muted-color);
}

.car-actions {
  display: flex;
  gap: 0.5rem;
  justify-content: flex-end;
}
</style>
