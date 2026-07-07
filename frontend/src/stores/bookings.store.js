import { defineStore } from 'pinia'
import { ref } from 'vue'
import { tripsApi } from '../api/trips.api'
import { bookingsApi } from '../api/bookings.api'
import { usersApi } from '../api/users.api'

export const useBookingsStore = defineStore('bookings', () => {
  const myBookings = ref([])
  const tripBookings = ref([])
  const loading = ref(false)

  async function fetchMyBookings(userId) {
    loading.value = true
    try {
      const response = await usersApi.getBookings(userId)
      myBookings.value = response.data
    } finally {
      loading.value = false
    }
  }

  async function fetchTripBookings(tripId) {
    loading.value = true
    try {
      const response = await tripsApi.getBookings(tripId)
      tripBookings.value = response.data
    } finally {
      loading.value = false
    }
  }

  async function bookTrip(tripId, data) {
    loading.value = true
    try {
      const response = await tripsApi.bookTrip(tripId, data)
      return response.data
    } finally {
      loading.value = false
    }
  }

  async function approveBooking(id) {
    loading.value = true
    try {
      const response = await bookingsApi.approve(id)
      _updateInList(tripBookings.value, response.data)
      _updateInList(myBookings.value, response.data)
      return response.data
    } finally {
      loading.value = false
    }
  }

  async function rejectBooking(id) {
    loading.value = true
    try {
      const response = await bookingsApi.reject(id)
      _updateInList(tripBookings.value, response.data)
      _updateInList(myBookings.value, response.data)
      return response.data
    } finally {
      loading.value = false
    }
  }

  async function cancelBooking(id) {
    loading.value = true
    try {
      const response = await bookingsApi.cancel(id)
      _updateInList(tripBookings.value, response.data)
      _updateInList(myBookings.value, response.data)
      return response.data
    } finally {
      loading.value = false
    }
  }

  function _updateInList(list, updated) {
    const idx = list.findIndex((b) => b.id === updated.id)
    if (idx !== -1) list[idx] = updated
  }

  return {
    myBookings,
    tripBookings,
    loading,
    fetchMyBookings,
    fetchTripBookings,
    bookTrip,
    approveBooking,
    rejectBooking,
    cancelBooking,
  }
})
