import { apiClient } from './http'
import type { City } from '@/types'

export const citiesApi = {
  getAll: () => apiClient.get<City[]>('/cities'),
}
