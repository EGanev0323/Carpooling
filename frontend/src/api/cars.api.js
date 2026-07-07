import client from './client'

export const carsApi = {
  getMyCars: () => client.get('/cars'),
  create: (data) => client.post('/cars', data),
  update: (id, data) => client.put(`/cars/${id}`, data),
  delete: (id) => client.delete(`/cars/${id}`),
}
