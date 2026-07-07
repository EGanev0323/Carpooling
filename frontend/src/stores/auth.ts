import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { apiClient } from '@/shared/api/http'

export interface User {
  id: string
  email: string
  firstName: string
  lastName: string
  username?: string
  avatarUrl: string | null
  roles: string[]
  isDriver?: boolean
}

export const useAuthStore = defineStore('auth', () => {
  const user = ref<User | null>(null)
  const loading = ref(false)

  const isLoggedIn = computed(() => user.value !== null)
  const isAdmin = computed(() => user.value?.roles?.includes('ADMIN') ?? false)
  const isDriver = computed(() =>
    user.value?.roles?.includes('DRIVER') ?? user.value?.isDriver ?? false
  )

  /** Fetches the currently authenticated user from the server. */
  async function fetchMe(): Promise<void> {
    loading.value = true
    try {
      const { data } = await apiClient.get<User>('/auth/me')
      user.value = data
    } catch {
      user.value = null
    } finally {
      loading.value = false
    }
  }

  /** Logs in with the given credentials, then fetches the current user. */
  async function login(credentials: { email?: string; username?: string; password: string }): Promise<void> {
    loading.value = true
    try {
      await apiClient.post('/auth/login', credentials)
      await fetchMe()
    } finally {
      loading.value = false
    }
  }

  /** Registers a new account. Does not auto-login — backend requires email verification. */
  async function register(data: {
    firstName: string
    lastName: string
    email: string
    password: string
    phoneNumber?: string
    username?: string
    isDriver?: boolean
  }): Promise<void> {
    loading.value = true
    try {
      await apiClient.post('/auth/register', data)
    } finally {
      loading.value = false
    }
  }

  /** Logs the current user out and clears the local state. */
  async function logout(): Promise<void> {
    try {
      await apiClient.post('/auth/logout')
    } finally {
      user.value = null
      window.location.href = '/'
    }
  }

  return { user, loading, isLoggedIn, isAdmin, isDriver, fetchMe, login, register, logout }
})
