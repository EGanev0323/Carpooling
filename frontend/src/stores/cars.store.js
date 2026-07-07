import { defineStore } from 'pinia'
import { ref } from 'vue'
import { carsApi } from '../api/cars.api'

export const useCarsStore = defineStore('cars', () => {
  const cars = ref([])
  const loading = ref(false)

  async function fetchMyCars() {
    loading.value = true
    try {
      const response = await carsApi.getMyCars()
      cars.value = response.data
    } finally {
      loading.value = false
    }
  }

  async function addCar(data) {
    loading.value = true
    try {
      const response = await carsApi.create(data)
      cars.value.push(response.data)
      return response.data
    } finally {
      loading.value = false
    }
  }

  async function updateCar(id, data) {
    loading.value = true
    try {
      const response = await carsApi.update(id, data)
      const idx = cars.value.findIndex((c) => c.id === id)
      if (idx !== -1) cars.value[idx] = response.data
      return response.data
    } finally {
      loading.value = false
    }
  }

  async function deleteCar(id) {
    loading.value = true
    try {
      await carsApi.delete(id)
      cars.value = cars.value.filter((c) => c.id !== id)
    } finally {
      loading.value = false
    }
  }

  return {
    cars,
    loading,
    fetchMyCars,
    addCar,
    updateCar,
    deleteCar,
  }
})
