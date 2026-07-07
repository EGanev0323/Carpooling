import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { authApi } from '../api/auth.api'
import router from '../router'

export const useAuthStore = defineStore('auth', () => {
  const user = ref(null)
  const loading = ref(false)

  const isAuthenticated = computed(() => !!user.value)
  const isDriver = computed(() => user.value?.isDriver ?? false)

  async function fetchMe() {
    try {
      loading.value = true
      const response = await authApi.me()
      user.value = response.data
    } catch (error) {
      if (error.response?.status === 401) {
        user.value = null
      }
    } finally {
      loading.value = false
    }
  }

  async function login(credentials) {
    loading.value = true
    try {
      await authApi.login(credentials)
      await fetchMe()
    } finally {
      loading.value = false
    }
  }

  async function register(data) {
    loading.value = true
    try {
      await authApi.register(data)
      await fetchMe()
    } finally {
      loading.value = false
    }
  }

  async function logout() {
    try {
      await authApi.logout()
    } finally {
      user.value = null
      router.push('/')
    }
  }

  return {
    user,
    loading,
    isAuthenticated,
    isDriver,
    fetchMe,
    login,
    register,
    logout,
  }
})
