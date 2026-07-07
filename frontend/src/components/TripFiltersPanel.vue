<script setup>
const props = defineProps({
  modelValue: {
    type: Object,
    default: () => ({}),
  },
})

const emit = defineEmits(['update:modelValue'])

function update(key, value) {
  emit('update:modelValue', { ...props.modelValue, [key]: value })
}
</script>

<template>
  <div class="filters-panel">
    <h3 class="filters-title">
      <i class="pi pi-filter" /> Филтри
    </h3>

    <div class="filter-group">
      <label>Цена на място (лв.)</label>
      <div class="price-range">
        <InputNumber
          :modelValue="modelValue.minPrice"
          @update:modelValue="update('minPrice', $event)"
          placeholder="От"
          :min="0"
          fluid
        />
        <span>—</span>
        <InputNumber
          :modelValue="modelValue.maxPrice"
          @update:modelValue="update('maxPrice', $event)"
          placeholder="До"
          :min="0"
          fluid
        />
      </div>
    </div>

    <div class="filter-group">
      <label>Допълнителни</label>
      <div class="checkbox-group">
        <div class="checkbox-item">
          <Checkbox
            inputId="smokingAllowed"
            :modelValue="modelValue.smokingAllowed"
            @update:modelValue="update('smokingAllowed', $event)"
            binary
          />
          <label for="smokingAllowed">
            <i class="pi pi-ban" /> Позволено пушене
          </label>
        </div>
        <div class="checkbox-item">
          <Checkbox
            inputId="petsAllowed"
            :modelValue="modelValue.petsAllowed"
            @update:modelValue="update('petsAllowed', $event)"
            binary
          />
          <label for="petsAllowed">
            <i class="pi pi-heart" /> Позволени домашни любимци
          </label>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.filters-panel {
  background: var(--p-surface-0);
  border: 1px solid var(--p-surface-200);
  border-radius: 8px;
  padding: 1.25rem;
  display: flex;
  flex-direction: column;
  gap: 1.25rem;
}

.filters-title {
  font-size: 0.95rem;
  font-weight: 600;
  display: flex;
  align-items: center;
  gap: 0.4rem;
}

.filter-group {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.filter-group label {
  font-size: 0.85rem;
  font-weight: 500;
  color: var(--p-text-muted-color);
}

.price-range {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.checkbox-group {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.checkbox-item {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  font-size: 0.9rem;
}
</style>
