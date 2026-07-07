import axios from 'axios'

const client = axios.create({
  baseURL: 'http://localhost:8080/api',
  withCredentials: true,
})

// Response interceptor: on 401, redirect to /login
client.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      if (window.location.pathname !== '/login') {
        window.location.href = '/login'
      }
    }
    return Promise.reject(error)
  }
)

export default client
