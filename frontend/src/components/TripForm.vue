<script setup>
import { ref, watch, onMounted } from 'vue'
import { useCarsStore } from '../stores/cars.store'

const props = defineProps({
  modelValue: {
    type: Object,
    default: () => ({}),
  },
  loading: Boolean,
})

const emit = defineEmits(['update:modelValue', 'submit'])

const carsStore = useCarsStore()

const form = ref({
  originCity: '',
  originAddress: '',
  destinationCity: '',
  destinationAddress: '',
  departureTime: null,
  estimatedArrival: null,
  pricePerSeat: null,
  totalSeats: 1,
  carId: null,
  description: '',
  smokingAllowed: false,
  petsAllowed: false,
  ...props.modelValue,
})

watch(
  () => props.modelValue,
  (val) => {
    if (val) {
      form.value = {
        ...form.value,
        ...val,
        departureTime: val.departureTime ? new Date(val.departureTime) : null,
        estimatedArrival: val.estimatedArrival ? new Date(val.estimatedArrival) : null,
      }
    }
  },
  { immediate: true }
)

watch(form, (val) => emit('update:modelValue', val), { deep: true })

const carOptions = ref([])
onMounted(async () => {
  await carsStore.fetchMyCars()
  carOptions.value = carsStore.cars.map((c) => ({
    label: `${c.make} ${c.model} (${c.year}) — ${c.licensePlate}`,
    value: c.id,
  }))
})

function handleSubmit() {
  emit('submit', { ...form.value })
}
</script>

<template>
  <form @submit.prevent="handleSubmit" class="trip-form">
    <!-- Route -->
    <Panel header="Маршрут" toggleable>
      <div class="field-row">
        <div class="field">
          <label for="originCity">Начален град *</label>
          <InputText
            id="originCity"
            v-model="form.originCity"
            placeholder="София"
            class="w-full"
            required
          />
        </div>
        <div class="field">
          <label for="originAddress">Точен адрес</label>
          <InputText
            id="originAddress"
            v-model="form.originAddress"
            placeholder="бул. Витоша 1"
            class="w-full"
          />
        </div>
      </div>

      <div class="field-row">
        <div class="field">
          <label for="destinationCity">Краен град *</label>
          <InputText
            id="destinationCity"
            v-model="form.destinationCity"
            placeholder="Пловдив"
            class="w-full"
            required
          />
        </div>
        <div class="field">
          <label for="destinationAddress">Точен адрес</label>
          <InputText
            id="destinationAddress"
            v-model="form.destinationAddress"
            placeholder="ул. Главна 5"
            class="w-full"
          />
        </div>
      </div>
    </Panel>

    <!-- Time -->
    <Panel header="Дата и час" toggleable>
      <div class="field-row">
        <div class="field">
          <label for="departureTime">Тръгване *</label>
          <DatePicker
            id="departureTime"
            v-model="form.departureTime"
            :showTime="true"
            :showSeconds="false"
            dateFormat="dd.mm.yy"
            hourFormat="24"
            placeholder="Дата и час"
            fluid
            required
          />
        </div>
        <div class="field">
          <label for="estimatedArrival">Прибл. пристигане</label>
          <DatePicker
            id="estimatedArrival"
            v-model="form.estimatedArrival"
            :showTime="true"
            :showSeconds="false"
            dateFormat="dd.mm.yy"
            hourFormat="24"
            placeholder="Дата и час"
            fluid
          />
        </div>
      </div>
    </Panel>

    <!-- Seats & price -->
    <Panel header="Места и цена" toggleable>
      <div class="field-row">
        <div class="field">
          <label for="pricePerSeat">Цена на място (лв.) *</label>
          <InputNumber
            id="pricePerSeat"
            v-model="form.pricePerSeat"
            :min="0"
            :minFractionDigits="0"
            :maxFractionDigits="2"
            placeholder="10.00"
            fluid
          />
        </div>
        <div class="field">
          <label for="totalSeats">Общо места *</label>
          <InputNumber
            id="totalSeats"
            v-model="form.totalSeats"
            :min="1"
            :max="8"
            :showButtons="true"
            fluid
          />
        </div>
      </div>
    </Panel>

    <!-- Car -->
    <Panel header="Автомобил" toggleable>
      <div class="field">
        <label for="car">Избери кола</label>
        <Select
          id="car"
          v-model="form.carId"
          :options="carOptions"
          optionLabel="label"
          optionValue="value"
          placeholder="Изберете автомобил"
          fluid
          :emptyMessage="'Нямате добавени коли'"
        />
        <small>
          <RouterLink to="/cars">Управление на коли</RouterLink>
        </small>
      </div>
    </Panel>

    <!-- Extra -->
    <Panel header="Допълнително" toggleable>
      <div class="field">
        <label for="description">Описание</label>
        <Textarea
          id="description"
          v-model="form.description"
          :rows="3"
          placeholder="Добавете информация за пътуването..."
          fluid
        />
      </div>

      <div class="toggles-row">
        <div class="toggle-item">
          <ToggleSwitch v-model="form.smokingAllowed" inputId="smokingAllowed" />
          <label for="smokingAllowed">
            <i class="pi pi-ban" /> Позволено пушене
          </label>
        </div>
        <div class="toggle-item">
          <ToggleSwitch v-model="form.petsAllowed" inputId="petsAllowed" />
          <label for="petsAllowed">
            <i class="pi pi-heart" /> Позволени домашни любимци
          </label>
        </div>
      </div>
    </Panel>

    <div class="form-actions">
      <Button
        type="submit"
        :label="loading ? 'Запазване...' : 'Запази пътуването'"
        icon="pi pi-check"
        :loading="loading"
        :disabled="!form.originCity || !form.destinationCity || !form.departureTime"
        class="submit-btn"
      />
    </div>
  </form>
</template>

<style scoped>
.trip-form {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.field-row {
  display: flex;
  gap: 1rem;
  margin-bottom: 0.75rem;
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

.field small a {
  color: var(--p-primary-color);
  text-decoration: none;
  font-size: 0.8rem;
}

.toggles-row {
  display: flex;
  gap: 2rem;
  flex-wrap: wrap;
}

.toggle-item {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  font-size: 0.9rem;
}

.form-actions {
  display: flex;
  justify-content: flex-end;
  padding-top: 0.5rem;
}

.submit-btn {
  min-width: 200px;
}

@media (max-width: 640px) {
  .field-row {
    flex-direction: column;
  }
}
</style>
