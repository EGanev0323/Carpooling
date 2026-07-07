import axios from 'axios'

export const apiClient = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL ?? '/api/v1',
  withCredentials: true,
  headers: { 'Content-Type': 'application/json' }
})

// CSRF interceptor: reads XSRF-TOKEN cookie, sets X-XSRF-TOKEN header on mutating requests
apiClient.interceptors.request.use(config => {
  const match = document.cookie.match(/XSRF-TOKEN=([^;]+)/)
  if (match) {
    config.headers['X-XSRF-TOKEN'] = decodeURIComponent(match[1])
  }
  return config
})

// 401 interceptor: on unauthorized, redirect to /login
apiClient.interceptors.response.use(
  res => res,
  async (error: unknown) => {
    const err = error as { config?: { _retry?: boolean }; response?: { status: number } }
    const originalRequest = err.config
    if (err.response?.status === 401 && originalRequest && !originalRequest._retry) {
      originalRequest._retry = true
      try {
        await apiClient.post('/auth/refresh')
        return apiClient(originalRequest as Parameters<typeof apiClient>[0])
      } catch {
        window.location.href = '/login'
      }
    }
    return Promise.reject(error)
  }
)
