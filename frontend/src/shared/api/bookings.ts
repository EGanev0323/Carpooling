import { apiClient } from './http'
import type { BookingResponse, CreateBookingData } from '@/types'

export const bookingsApi = {
  create: (data: CreateBookingData) =>
    apiClient.post<BookingResponse>('/bookings', data),

  getMy: () =>
    apiClient.get<BookingResponse[]>('/bookings/my'),

  getRideBookings: (ridePublicId: string) =>
    apiClient.get<BookingResponse[]>(`/bookings/ride/${ridePublicId}`),

  confirm: (publicId: string) =>
    apiClient.post(`/bookings/${publicId}/confirm`),

  reject: (publicId: string) =>
    apiClient.post(`/bookings/${publicId}/reject`),

  cancel: (publicId: string) =>
    apiClient.post(`/bookings/${publicId}/cancel`),
}
