<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { useToast } from 'primevue/usetoast'
import { useConfirm } from 'primevue/useconfirm'
import { carsApi } from '@/shared/api/cars'
import type { CarResponse, CarFormData } from '@/types'

const { t } = useI18n()
const toast = useToast()
const confirm = useConfirm()

const cars = ref<CarResponse[]>([])
const loading = ref(false)
const dialogVisible = ref(false)
const editingCar = ref<CarResponse | null>(null)
const submitting = ref(false)
const deletingId = ref<number | null>(null)

const CURRENT_YEAR = new Date().getFullYear()
const MIN_YEAR = 1970

const emptyForm = (): CarFormData => ({
  make: '',
  model: '',
  year: CURRENT_YEAR,
  color: '',
  licensePlate: '',
  seats: 5,
  amenities: '',
})

const form = reactive<CarFormData>(emptyForm())
const formError = ref('')

function openAddDialog(): void {
  editingCar.value = null
  Object.assign(form, emptyForm())
  formError.value = ''
  dialogVisible.value = true
}

function openEditDialog(car: CarResponse): void {
  editingCar.value = car
  Object.assign(form, {
    make: car.make,
    model: car.model,
    year: car.year,
    color: car.color,
    licensePlate: car.licensePlate,
    seats: car.seats,
    amenities: car.amenities,
  })
  formError.value = ''
  dialogVisible.value = true
}

function validateForm(): boolean {
  if (!form.make.trim()) { formError.value = `${t('cars.make')} е задължително`; return false }
  if (!form.model.trim()) { formError.value = `${t('cars.model')} е задължително`; return false }
  if (form.year < MIN_YEAR || form.year > CURRENT_YEAR) {
    formError.value = `${t('cars.year')} трябва да е между ${MIN_YEAR} и ${CURRENT_YEAR}`
    return false
  }
  if (!form.color.trim()) { formError.value = `${t('cars.color')} е задължително`; return false }
  if (!form.licensePlate.trim()) { formError.value = `${t('cars.licensePlate')} е задължително`; return false }
  if (form.seats < 2 || form.seats > 9) {
    formError.value = `${t('cars.seats')} трябва да е между 2 и 9`
    return false
  }
  formError.value = ''
  return true
}

async function submitForm(): Promise<void> {
  if (!validateForm()) return
  submitting.value = true
  try {
    if (editingCar.value) {
      const { data } = await carsApi.update(editingCar.value.id, { ...form })
      const idx = cars.value.findIndex(c => c.id === editingCar.value!.id)
      if (idx !== -1) cars.value[idx] = data
      toast.add({ severity: 'success', summary: t('common.save'), life: 3000 })
    } else {
      const { data } = await carsApi.add({ ...form })
      cars.value.push(data)
      toast.add({ severity: 'success', summary: t('cars.add'), life: 3000 })
    }
    dialogVisible.value = false
  } catch {
    toast.add({ severity: 'error', summary: t('auth.errors.generic'), life: 4000 })
  } finally {
    submitting.value = false
  }
}

function confirmDelete(car: CarResponse): void {
  confirm.require({
    message: `Сигурен ли си, че искаш да изтриеш ${car.make} ${car.model}?`,
    header: t('cars.delete'),
    icon: 'pi pi-exclamation-triangle',
    acceptLabel: t('common.confirm'),
    rejectLabel: t('common.cancel'),
    acceptClass: 'p-button-danger',
    accept: () => doDelete(car.id),
  })
}

async function doDelete(id: number): Promise<void> {
  deletingId.value = id
  try {
    await carsApi.delete(id)
    cars.value = cars.value.filter(c => c.id !== id)
    toast.add({ severity: 'success', summary: t('cars.delete'), life: 3000 })
  } catch {
    toast.add({ severity: 'error', summary: t('auth.errors.generic'), life: 4000 })
  } finally {
    deletingId.value = null
  }
}

async function loadCars(): Promise<void> {
  loading.value = true
  try {
    const { data } = await carsApi.getMy()
    cars.value = data
  } catch {
    toast.add({ severity: 'error', summary: t('auth.errors.generic'), life: 4000 })
  } finally {
    loading.value = false
  }
}

function parseAmenities(amenities: string): string[] {
  try {
    const parsed = JSON.parse(amenities)
    if (Array.isArray(parsed)) return parsed as string[]
    return []
  } catch {
    return amenities ? [amenities] : []
  }
}

onMounted(loadCars)
</script>

<template>
  <div class="cars-view">
    <div class="cars-view__header">
      <h1 class="cars-view__title">{{ t('cars.title') }}</h1>
      <Button
        :label="t('cars.add')"
        icon="pi pi-plus"
        @click="openAddDialog"
      />
    </div>

    <div v-if="loading" class="cars-view__skeletons">
      <Skeleton v-for="n in 2" :key="n" height="180px" border-radius="12px" />
    </div>

    <div v-else-if="cars.length === 0" class="cars-view__empty">
      <i class="pi pi-car empty-icon" />
      <p class="empty-text">{{ t('cars.noDriver') }}</p>
      <Button :label="t('cars.add')" icon="pi pi-plus" @click="openAddDialog" />
    </div>

    <div v-else class="cars-grid">
      <Card v-for="car in cars" :key="car.id" class="car-card">
        <template #title>
          <div class="car-card__title">
            <span>{{ car.make }} {{ car.model }}</span>
            <span class="car-card__year">{{ car.year }}</span>
          </div>
        </template>

        <template #content>
          <div class="car-card__info">
            <div class="car-info-item">
              <i class="pi pi-palette" />
              <span>{{ car.color }}</span>
            </div>
            <div class="car-info-item">
              <i class="pi pi-id-card" />
              <span>{{ car.licensePlate }}</span>
            </div>
            <div class="car-info-item">
              <i class="pi pi-users" />
              <span>{{ car.seats }} {{ t('cars.seats') }}</span>
            </div>
          </div>

          <div v-if="parseAmenities(car.amenities).length" class="car-card__amenities">
            <Tag
              v-for="amenity in parseAmenities(car.amenities)"
              :key="amenity"
              :value="amenity"
              severity="secondary"
              class="amenity-tag"
            />
          </div>
        </template>

        <template #footer>
          <div class="car-card__actions">
            <Button
              :label="t('common.save')"
              icon="pi pi-pencil"
              severity="secondary"
              outlined
              size="small"
              @click="openEditDialog(car)"
            />
            <Button
              :label="t('cars.delete')"
              icon="pi pi-trash"
              severity="danger"
              outlined
              size="small"
              :loading="deletingId === car.id"
              @click="confirmDelete(car)"
            />
          </div>
        </template>
      </Card>
    </div>

    <Dialog
      v-model:visible="dialogVisible"
      :header="editingCar ? t('common.save') : t('cars.add')"
      :modal="true"
      :closable="true"
      style="width: min(520px, 95vw)"
    >
      <form class="car-form" @submit.prevent="submitForm">
        <Message v-if="formError" severity="error" :closable="false">
          {{ formError }}
        </Message>

        <div class="car-form__row">
          <div class="car-form__field">
            <label class="car-form__label">{{ t('cars.make') }} *</label>
            <InputText v-model="form.make" :placeholder="t('cars.make')" />
          </div>
          <div class="car-form__field">
            <label class="car-form__label">{{ t('cars.model') }} *</label>
            <InputText v-model="form.model" :placeholder="t('cars.model')" />
          </div>
        </div>

        <div class="car-form__row">
          <div class="car-form__field">
            <label class="car-form__label">{{ t('cars.year') }} *</label>
            <InputNumber
              v-model="form.year"
              :min="MIN_YEAR"
              :max="CURRENT_YEAR"
              :useGrouping="false"
            />
          </div>
          <div class="car-form__field">
            <label class="car-form__label">{{ t('cars.seats') }} *</label>
            <InputNumber
              v-model="form.seats"
              :min="2"
              :max="9"
              showButtons
              buttonLayout="horizontal"
              :step="1"
            />
          </div>
        </div>

        <div class="car-form__field">
          <label class="car-form__label">{{ t('cars.color') }} *</label>
          <InputText v-model="form.color" :placeholder="t('cars.color')" />
        </div>

        <div class="car-form__field">
          <label class="car-form__label">{{ t('cars.licensePlate') }} *</label>
          <InputText v-model="form.licensePlate" :placeholder="t('cars.licensePlate')" />
        </div>

        <div class="car-form__field">
          <label class="car-form__label">Удобства (JSON масив или текст)</label>
          <InputText v-model="form.amenities" placeholder='["AC", "Music", "Pet friendly"]' />
        </div>

        <div class="car-form__actions">
          <Button
            :label="t('common.cancel')"
            severity="secondary"
            text
            @click="dialogVisible = false"
          />
          <Button
            type="submit"
            :label="editingCar ? t('common.save') : t('cars.add')"
            :loading="submitting"
          />
        </div>
      </form>
    </Dialog>
  </div>
</template>

<style scoped>
.cars-view {
  padding: var(--spacing-4) 0;
}

.cars-view__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: var(--spacing-6);
}

.cars-view__title {
  font-size: var(--fs-h1);
  font-weight: 700;
  margin: 0;
}

.cars-view__skeletons {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-4);
}

.cars-view__empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 40vh;
  gap: var(--spacing-4);
  color: var(--muted);
  text-align: center;
}

.empty-icon {
  font-size: 4rem;
}

.empty-text {
  font-size: var(--fs-h3);
  margin: 0;
}

.cars-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: var(--spacing-4);
}

.car-card__title {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
}

.car-card__year {
  font-size: var(--fs-small);
  color: var(--muted);
  font-weight: 400;
}

.car-card__info {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-2);
  margin-bottom: var(--spacing-3);
}

.car-info-item {
  display: flex;
  align-items: center;
  gap: var(--spacing-2);
  font-size: var(--fs-small);
  color: var(--muted);
}

.car-card__amenities {
  display: flex;
  flex-wrap: wrap;
  gap: var(--spacing-1);
  margin-bottom: var(--spacing-2);
}

.amenity-tag {
  font-size: 0.7rem;
}

.car-card__actions {
  display: flex;
  gap: var(--spacing-2);
}

.car-form {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-3);
  padding-top: var(--spacing-2);
}

.car-form__row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: var(--spacing-3);
}

.car-form__field {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-1);
}

.car-form__label {
  font-size: var(--fs-small);
  font-weight: 500;
  color: var(--muted);
}

.car-form__actions {
  display: flex;
  justify-content: flex-end;
  gap: var(--spacing-2);
  padding-top: var(--spacing-2);
}
</style>
