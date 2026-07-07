import { apiClient } from './http'
import type { CarResponse, CarFormData } from '@/types'

export const carsApi = {
  getMy: () => apiClient.get<CarResponse[]>('/cars/my'),
  add: (data: CarFormData) => apiClient.post<CarResponse>('/cars', data),
  update: (id: number, data: Partial<CarFormData>) => apiClient.put<CarResponse>(`/cars/${id}`, data),
  delete: (id: number) => apiClient.delete(`/cars/${id}`),
}
