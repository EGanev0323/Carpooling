import { defineStore } from 'pinia'
import { ref } from 'vue'
import { tripsApi } from '../api/trips.api'

export const useTripsStore = defineStore('trips', () => {
  const trips = ref([])
  const currentTrip = ref(null)
  const totalPages = ref(0)
  const loading = ref(false)

  async function searchTrips(params) {
    loading.value = true
    try {
      const response = await tripsApi.search(params)
      const data = response.data
      // Support both paginated { content, totalPages } and plain array responses
      if (Array.isArray(data)) {
        trips.value = data
        totalPages.value = 1
      } else {
        trips.value = data.content ?? data.trips ?? []
        totalPages.value = data.totalPages ?? 1
      }
    } finally {
      loading.value = false
    }
  }

  async function fetchTrip(id) {
    loading.value = true
    try {
      const response = await tripsApi.getById(id)
      currentTrip.value = response.data
    } finally {
      loading.value = false
    }
  }

  async function createTrip(data) {
    loading.value = true
    try {
      const response = await tripsApi.create(data)
      return response.data
    } finally {
      loading.value = false
    }
  }

  async function cancelTrip(id) {
    loading.value = true
    try {
      await tripsApi.cancel(id)
      trips.value = trips.value.filter((t) => t.id !== id)
      if (currentTrip.value?.id === id) {
        currentTrip.value = null
      }
    } finally {
      loading.value = false
    }
  }

  return {
    trips,
    currentTrip,
    totalPages,
    loading,
    searchTrips,
    fetchTrip,
    createTrip,
    cancelTrip,
  }
})
