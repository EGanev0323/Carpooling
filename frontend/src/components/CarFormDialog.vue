<script setup>
import { ref, watch } from 'vue'

const props = defineProps({
  visible: Boolean,
  car: {
    type: Object,
    default: null,
  },
})

const emit = defineEmits(['update:visible', 'saved', 'cancel'])

const form = ref(emptyForm())

function emptyForm() {
  return {
    make: '',
    model: '',
    year: new Date().getFullYear(),
    color: '',
    licensePlate: '',
    totalSeats: 4,
  }
}

watch(
  () => props.car,
  (car) => {
    if (car) {
      form.value = { ...car }
    } else {
      form.value = emptyForm()
    }
  },
  { immediate: true }
)

function handleSave() {
  emit('saved', { ...form.value })
  emit('update:visible', false)
}

function handleCancel() {
  emit('cancel')
  emit('update:visible', false)
  form.value = emptyForm()
}
</script>

<template>
  <Dialog
    :visible="visible"
    @update:visible="emit('update:visible', $event)"
    :header="car ? 'Редактирай кола' : 'Добави кола'"
    :style="{ width: '480px' }"
    modal
  >
    <div class="car-form">
      <div class="field-row">
        <div class="field">
          <label for="make">Марка *</label>
          <InputText id="make" v-model="form.make" placeholder="BMW" class="w-full" required />
        </div>
        <div class="field">
          <label for="model">Модел *</label>
          <InputText id="model" v-model="form.model" placeholder="320d" class="w-full" required />
        </div>
      </div>

      <div class="field-row">
        <div class="field">
          <label for="year">Година *</label>
          <InputNumber
            id="year"
            v-model="form.year"
            :min="1990"
            :max="new Date().getFullYear() + 1"
            :useGrouping="false"
            fluid
          />
        </div>
        <div class="field">
          <label for="totalSeats">Места *</label>
          <InputNumber
            id="totalSeats"
            v-model="form.totalSeats"
            :min="2"
            :max="9"
            :showButtons="true"
            fluid
          />
        </div>
      </div>

      <div class="field">
        <label for="color">Цвят *</label>
        <InputText id="color" v-model="form.color" placeholder="Черен" class="w-full" required />
      </div>

      <div class="field">
        <label for="licensePlate">Регистрационен номер *</label>
        <InputText
          id="licensePlate"
          v-model="form.licensePlate"
          placeholder="СА 1234 АВ"
          class="w-full"
          required
        />
      </div>
    </div>

    <template #footer>
      <Button label="Отказ" text @click="handleCancel" />
      <Button
        :label="car ? 'Запази' : 'Добави'"
        icon="pi pi-check"
        :disabled="!form.make || !form.model || !form.color || !form.licensePlate"
        @click="handleSave"
      />
    </template>
  </Dialog>
</template>

<style scoped>
.car-form {
  display: flex;
  flex-direction: column;
  gap: 1rem;
  padding: 0.5rem 0;
}

.field-row {
  display: flex;
  gap: 1rem;
}

.field {
  display: flex;
  flex-direction: column;
  gap: 0.4rem;
  flex: 1;
}

.field label {
  font-size: 0.875rem;
  font-weight: 500;
}
</style>
