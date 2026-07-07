import { apiClient } from './http'
import type { RideResponse, PageResponse, RideSearchParams, CreateRideData } from '@/types'

export const ridesApi = {
  search: (params: RideSearchParams) =>
    apiClient.get<PageResponse<RideResponse>>('/rides', { params }),

  getOne: (publicId: string) =>
    apiClient.get<RideResponse>(`/rides/${publicId}`),

  create: (data: CreateRideData) =>
    apiClient.post<RideResponse>('/rides', data),

  cancel: (publicId: string) =>
    apiClient.post(`/rides/${publicId}/cancel`),

  getMy: () =>
    apiClient.get<RideResponse[]>('/rides/my'),
}
