<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { useToast } from 'primevue/usetoast'
import { useCitiesStore } from '@/stores/cities'
import { ridesApi } from '@/shared/api/rides'
import { carsApi } from '@/shared/api/cars'
import type { CarResponse, City } from '@/types'

const router = useRouter()
const { t, locale } = useI18n()
const toast = useToast()
const citiesStore = useCitiesStore()

const cars = ref<CarResponse[]>([])
const carsLoading = ref(false)
const submitting = ref(false)
const validationError = ref('')

const selectedOrigin = ref<City | null>(null)
const selectedDestination = ref<City | null>(null)
const departureDate = ref<Date | null>(null)
const selectedCar = ref<CarResponse | null>(null)
const totalSeats = ref(1)
const pricePerSeat = ref(10)
const description = ref('')

const minDate = computed(() => {
  const tomorrow = new Date()
  tomorrow.setDate(tomorrow.getDate() + 1)
  tomorrow.setHours(0, 0, 0, 0)
  return tomorrow
})

const maxSeats = computed(() => {
  if (!selectedCar.value) return 8
  return Math.max(1, selectedCar.value.seats - 1)
})

const cityOptions = computed(() =>
  citiesStore.cities.map(c => ({
    label: locale.value === 'bg' ? c.nameBg : c.nameEn,
    value: c,
  }))
)

const carOptions = computed(() =>
  cars.value.map(car => ({
    label: `${car.make} ${car.model} (${car.year}) — ${car.seats} места`,
    value: car,
  }))
)

watch(selectedCar, car => {
  if (car && totalSeats.value > car.seats - 1) {
    totalSeats.value = Math.max(1, car.seats - 1)
  }
})

async function loadCars(): Promise<void> {
  carsLoading.value = true
  try {
    const { data } = await carsApi.getMy()
    cars.value = data
  } catch {
    toast.add({
      severity: 'error',
      summary: t('auth.errors.generic'),
      life: 4000,
    })
  } finally {
    carsLoading.value = false
  }
}

function validate(): boolean {
  if (!selectedOrigin.value) {
    validationError.value = `${t('rides.from')} е задължително`
    return false
  }
  if (!selectedDestination.value) {
    validationError.value = `${t('rides.to')} е задължително`
    return false
  }
  if (selectedOrigin.value.id === selectedDestination.value.id) {
    validationError.value = 'Началният и крайният град не могат да са еднакви'
    return false
  }
  if (!departureDate.value) {
    validationError.value = `${t('rides.date')} е задължително`
    return false
  }
  if (!selectedCar.value) {
    validationError.value = `${t('cars.model')} е задължително`
    return false
  }
  if (totalSeats.value < 1 || totalSeats.value > maxSeats.value) {
    validationError.value = `Местата трябва да са между 1 и ${maxSeats.value}`
    return false
  }
  if (pricePerSeat.value < 1 || pricePerSeat.value > 200) {
    validationError.value = 'Цената трябва да е между 1 и 200 лв.'
    return false
  }
  if (description.value.length > 500) {
    validationError.value = 'Описанието не може да е повече от 500 символа'
    return false
  }
  validationError.value = ''
  return true
}

async function submit(): Promise<void> {
  if (!validate()) return

  submitting.value = true
  try {
    const { data } = await ridesApi.create({
      originCityId: selectedOrigin.value!.id,
      destinationCityId: selectedDestination.value!.id,
      departureAt: departureDate.value!.toISOString(),
      carId: selectedCar.value!.id,
      totalSeats: totalSeats.value,
      pricePerSeat: pricePerSeat.value,
      description: description.value || undefined,
    })
    toast.add({
      severity: 'success',
      summary: t('rides.createTitle'),
      detail: 'Маршрутът е публикуван успешно',
      life: 3000,
    })
    router.push(`/trips/${data.publicId}`)
  } catch {
    toast.add({
      severity: 'error',
      summary: t('auth.errors.generic'),
      life: 4000,
    })
  } finally {
    submitting.value = false
  }
}

onMounted(async () => {
  await citiesStore.fetchCities()
  await loadCars()
})
</script>

<template>
  <div class="create-trip">
    <div class="create-trip__header">
      <h1 class="create-trip__title">{{ t('rides.createTitle') }}</h1>
    </div>

    <Card class="create-trip__card">
      <template #content>
        <div v-if="cars.length === 0 && !carsLoading" class="create-trip__no-car">
          <i class="pi pi-car" style="font-size: 2.5rem; color: var(--muted)" />
          <p>{{ t('cars.noDriver') }}</p>
          <Button
            :label="t('cars.add')"
            icon="pi pi-plus"
            @click="router.push('/cars')"
          />
        </div>

        <form v-else class="create-trip__form" @submit.prevent="submit">
          <Message v-if="validationError" severity="error" :closable="false">
            {{ validationError }}
          </Message>

          <div class="form-row">
            <div class="form-field">
              <label class="form-label">{{ t('rides.from') }} *</label>
              <Select
                v-model="selectedOrigin"
                :options="cityOptions"
                optionLabel="label"
                optionValue="value"
                :placeholder="t('rides.from')"
                filter
                class="form-control"
              />
            </div>

            <div class="form-field">
              <label class="form-label">{{ t('rides.to') }} *</label>
              <Select
                v-model="selectedDestination"
                :options="cityOptions"
                optionLabel="label"
                optionValue="value"
                :placeholder="t('rides.to')"
                filter
                class="form-control"
              />
            </div>
          </div>

          <div class="form-row">
            <div class="form-field">
              <label class="form-label">{{ t('rides.date') }} *</label>
              <DatePicker
                v-model="departureDate"
                :minDate="minDate"
                showTime
                hourFormat="24"
                dateFormat="dd.mm.yy"
                :placeholder="t('rides.date')"
                class="form-control"
              />
            </div>

            <div class="form-field">
              <label class="form-label">{{ t('cars.model') }} *</label>
              <Select
                v-model="selectedCar"
                :options="carOptions"
                optionLabel="label"
                optionValue="value"
                :placeholder="t('cars.model')"
                :loading="carsLoading"
                class="form-control"
              />
            </div>
          </div>

          <div class="form-row">
            <div class="form-field">
              <label class="form-label">{{ t('rides.seats') }} *</label>
              <InputNumber
                v-model="totalSeats"
                :min="1"
                :max="maxSeats"
                showButtons
                buttonLayout="horizontal"
                :step="1"
                class="form-control"
              />
              <small class="form-hint">Максимум {{ maxSeats }} (без шофьора)</small>
            </div>

            <div class="form-field">
              <label class="form-label">{{ t('rides.pricePerSeat') }} *</label>
              <InputNumber
                v-model="pricePerSeat"
                :min="1"
                :max="200"
                suffix=" лв."
                showButtons
                buttonLayout="horizontal"
                :step="1"
                class="form-control"
              />
            </div>
          </div>

          <div class="form-field">
            <label class="form-label">{{ t('bookings.message') }}</label>
            <Textarea
              v-model="description"
              :placeholder="'Описание на маршрута (незадължително)'"
              rows="4"
              :maxlength="500"
              class="form-control"
            />
            <small class="form-hint">{{ description.length }}/500</small>
          </div>

          <div class="create-trip__actions">
            <Button
              :label="t('common.cancel')"
              severity="secondary"
              text
              icon="pi pi-times"
              @click="router.push('/rides')"
            />
            <Button
              type="submit"
              :label="t('rides.createTitle')"
              icon="pi pi-check"
              :loading="submitting"
            />
          </div>
        </form>
      </template>
    </Card>
  </div>
</template>

<style scoped>
.create-trip {
  padding: var(--spacing-4) 0;
  max-width: 760px;
  margin: 0 auto;
}

.create-trip__header {
  margin-bottom: var(--spacing-5);
}

.create-trip__title {
  font-size: var(--fs-h1);
  font-weight: 700;
  margin: 0;
}

.create-trip__no-car {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: var(--spacing-4);
  padding: var(--spacing-8) var(--spacing-4);
  color: var(--muted);
  text-align: center;
}

.create-trip__form {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-4);
}

.form-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: var(--spacing-4);
}

@media (max-width: 600px) {
  .form-row {
    grid-template-columns: 1fr;
  }
}

.form-field {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-1);
}

.form-label {
  font-size: var(--fs-small);
  font-weight: 500;
  color: var(--muted);
}

.form-hint {
  font-size: 0.75rem;
  color: var(--muted);
}

.form-control {
  width: 100%;
}

.create-trip__actions {
  display: flex;
  justify-content: flex-end;
  gap: var(--spacing-3);
  padding-top: var(--spacing-2);
}
</style>
