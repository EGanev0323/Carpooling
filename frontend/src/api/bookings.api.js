import client from './client'

export const bookingsApi = {
  getById: (id) => client.get(`/bookings/${id}`),
  approve: (id) => client.patch(`/bookings/${id}/approve`),
  reject: (id) => client.patch(`/bookings/${id}/reject`),
  cancel: (id) => client.patch(`/bookings/${id}/cancel`),
}
