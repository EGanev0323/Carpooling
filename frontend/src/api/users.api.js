import client from './client'

export const usersApi = {
  getById: (id) => client.get(`/users/${id}`),
  update: (id, data) => client.put(`/users/${id}`, data),
  getRatings: (id) => client.get(`/users/${id}/ratings`),
  getTrips: (id) => client.get(`/users/${id}/trips`),
  getBookings: (id) => client.get(`/users/${id}/bookings`),
}
