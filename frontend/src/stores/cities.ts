import { defineStore } from 'pinia'
import { ref } from 'vue'
import { citiesApi } from '@/shared/api/cities'
import type { City } from '@/types'

export const useCitiesStore = defineStore('cities', () => {
  const cities = ref<City[]>([])
  const loaded = ref(false)
  const loading = ref(false)

  /** Fetches cities from the API once; subsequent calls are no-ops. */
  async function fetchCities(): Promise<void> {
    if (loaded.value || loading.value) return
    loading.value = true
    try {
      const { data } = await citiesApi.getAll()
      cities.value = data
      loaded.value = true
    } finally {
      loading.value = false
    }
  }

  return { cities, loaded, loading, fetchCities }
})
