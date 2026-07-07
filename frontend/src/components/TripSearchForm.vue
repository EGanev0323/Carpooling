<script setup>
import { ref, watch } from 'vue'

const props = defineProps({
  modelValue: {
    type: Object,
    default: () => ({}),
  },
})

const emit = defineEmits(['search', 'update:modelValue'])

const form = ref({
  originCity: props.modelValue?.originCity ?? '',
  destinationCity: props.modelValue?.destinationCity ?? '',
  departureDate: props.modelValue?.departureDate ? new Date(props.modelValue.departureDate) : null,
  minSeats: props.modelValue?.minSeats ?? null,
})

watch(
  () => props.modelValue,
  (val) => {
    form.value.originCity = val?.originCity ?? ''
    form.value.destinationCity = val?.destinationCity ?? ''
    form.value.departureDate = val?.departureDate ? new Date(val.departureDate) : null
    form.value.minSeats = val?.minSeats ?? null
  }
)

function formatDate(date) {
  if (!date) return undefined
  const d = new Date(date)
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`
}

function handleSearch() {
  const params = {
    originCity: form.value.originCity || undefined,
    destinationCity: form.value.destinationCity || undefined,
    departureDate: formatDate(form.value.departureDate),
    minSeats: form.value.minSeats || undefined,
  }
  emit('update:modelValue', params)
  emit('search', params)
}
</script>

<template>
  <form class="search-form" @submit.prevent="handleSearch">
    <div class="search-fields">
      <div class="field">
        <label for="originCity">От</label>
        <InputText
          id="originCity"
          v-model="form.originCity"
          placeholder="Начален град"
          class="w-full"
        />
      </div>

      <div class="field-arrow">
        <i class="pi pi-arrow-right" />
      </div>

      <div class="field">
        <label for="destinationCity">До</label>
        <InputText
          id="destinationCity"
          v-model="form.destinationCity"
          placeholder="Краен град"
          class="w-full"
        />
      </div>

      <div class="field">
        <label for="departureDate">Дата</label>
        <DatePicker
          id="departureDate"
          v-model="form.departureDate"
          placeholder="Изберете дата"
          dateFormat="dd.mm.yy"
          :showIcon="true"
          fluid
        />
      </div>

      <div class="field field-seats">
        <label for="minSeats">Мин. места</label>
        <InputNumber
          id="minSeats"
          v-model="form.minSeats"
          :min="1"
          :max="8"
          placeholder="1"
          fluid
        />
      </div>

      <Button
        type="submit"
        label="Търси"
        icon="pi pi-search"
        class="search-btn"
      />
    </div>
  </form>
</template>

<style scoped>
.search-form {
  background: var(--p-surface-0);
  border-radius: 12px;
  padding: 1.25rem 1.5rem;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
}

.search-fields {
  display: flex;
  align-items: flex-end;
  gap: 0.75rem;
  flex-wrap: wrap;
}

.field {
  display: flex;
  flex-direction: column;
  gap: 0.4rem;
  flex: 1;
  min-width: 140px;
}

.field-seats {
  max-width: 110px;
}

.field label {
  font-size: 0.85rem;
  font-weight: 500;
  color: var(--p-text-muted-color);
}

.field-arrow {
  padding-bottom: 0.6rem;
  color: var(--p-text-muted-color);
}

.search-btn {
  white-space: nowrap;
  align-self: flex-end;
}

@media (max-width: 640px) {
  .field-arrow {
    display: none;
  }
  .field {
    min-width: calc(50% - 0.75rem);
  }
  .search-btn {
    width: 100%;
    justify-content: center;
  }
}
</style>
