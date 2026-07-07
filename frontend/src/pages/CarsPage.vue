<script setup>
import { ref, onMounted } from 'vue'
import { useCarsStore } from '../stores/cars.store'
import { useToast } from 'primevue/usetoast'
import { useConfirm } from 'primevue/useconfirm'
import LoadingSpinner from '../components/LoadingSpinner.vue'
import EmptyState from '../components/EmptyState.vue'
import CarCard from '../components/CarCard.vue'
import CarFormDialog from '../components/CarFormDialog.vue'

const carsStore = useCarsStore()
const toast = useToast()
const confirm = useConfirm()

const showDialog = ref(false)
const editingCar = ref(null)

onMounted(async () => {
  await carsStore.fetchMyCars()
})

function openAddDialog() {
  editingCar.value = null
  showDialog.value = true
}

function openEditDialog(car) {
  editingCar.value = { ...car }
  showDialog.value = true
}

async function handleSaved(carData) {
  try {
    if (editingCar.value?.id) {
      await carsStore.updateCar(editingCar.value.id, carData)
      toast.add({ severity: 'success', summary: 'Колата е обновена', life: 3000 })
    } else {
      await carsStore.addCar(carData)
      toast.add({ severity: 'success', summary: 'Колата е добавена', life: 3000 })
    }
  } catch (err) {
    toast.add({
      severity: 'error',
      summary: 'Грешка',
      detail: err.response?.data?.message ?? 'Неуспешна операция',
      life: 3000,
    })
  }
}

function handleDelete(carId) {
  confirm.require({
    message: 'Сигурни ли сте, че искате да изтриете тази кола?',
    header: 'Изтриване на кола',
    icon: 'pi pi-exclamation-triangle',
    acceptLabel: 'Изтрий',
    rejectLabel: 'Отказ',
    acceptClass: 'p-button-danger',
    accept: async () => {
      try {
        await carsStore.deleteCar(carId)
        toast.add({ severity: 'info', summary: 'Колата е изтрита', life: 3000 })
      } catch {
        toast.add({ severity: 'error', summary: 'Грешка при изтриване', life: 3000 })
      }
    },
  })
}
</script>

<template>
  <div class="cars-page">
    <div class="page-inner">
      <div class="page-header">
        <h1>Моите коли</h1>
        <Button label="Добави кола" icon="pi pi-plus" @click="openAddDialog" />
      </div>

      <LoadingSpinner v-if="carsStore.loading" />

      <template v-else-if="carsStore.cars.length === 0">
        <EmptyState
          message="Нямате добавени коли"
          icon="pi pi-car"
        />
        <div style="text-align: center; margin-top: 1rem">
          <Button label="Добави първата си кола" icon="pi pi-plus" @click="openAddDialog" />
        </div>
      </template>

      <div v-else class="cars-grid">
        <CarCard
          v-for="car in carsStore.cars"
          :key="car.id"
          :car="car"
          @edit="openEditDialog"
          @delete="handleDelete"
        />
      </div>
    </div>

    <CarFormDialog
      v-model:visible="showDialog"
      :car="editingCar"
      @saved="handleSaved"
      @cancel="showDialog = false"
    />
  </div>
</template>

<style scoped>
.cars-page {
  background: var(--p-surface-50);
  min-height: calc(100vh - 128px);
}

.page-inner {
  max-width: 1000px;
  margin: 0 auto;
  padding: 2rem 1.5rem;
}

.page-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 2rem;
  flex-wrap: wrap;
  gap: 1rem;
}

.page-header h1 {
  font-size: 1.75rem;
  font-weight: 700;
}

.cars-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(260px, 1fr));
  gap: 1.25rem;
}
</style>
