import client from './client'

export const tripsApi = {
  search: (params) => client.get('/trips', { params }),
  getById: (id) => client.get(`/trips/${id}`),
  create: (data) => client.post('/trips', data),
  update: (id, data) => client.put(`/trips/${id}`, data),
  cancel: (id) => client.delete(`/trips/${id}`),
  getBookings: (tripId) => client.get(`/trips/${tripId}/bookings`),
  getRatings: (tripId) => client.get(`/trips/${tripId}/ratings`),
  submitRating: (tripId, data) => client.post(`/trips/${tripId}/ratings`, data),
  bookTrip: (tripId, data) => client.post(`/trips/${tripId}/bookings`, data),
}
