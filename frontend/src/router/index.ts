import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/',
      redirect: '/rides'
    },
    {
      path: '/rides',
      name: 'Rides',
      component: () => import('@/features/rides/RidesView.vue')
    },
    {
      path: '/login',
      name: 'Login',
      component: () => import('@/features/auth/LoginView.vue')
    },
    {
      path: '/register',
      name: 'Register',
      component: () => import('@/features/auth/RegisterView.vue')
    },
    {
      path: '/verify-email',
      name: 'VerifyEmail',
      component: () => import('@/features/auth/VerifyEmailView.vue')
    },
    {
      path: '/trips',
      redirect: '/rides'
    },
    {
      path: '/trips/create',
      name: 'CreateTrip',
      component: () => import('@/features/rides/CreateTripView.vue'),
      meta: { requiresAuth: true, requiresDriver: true }
    },
    {
      path: '/trips/:id',
      name: 'TripDetail',
      component: () => import('@/features/rides/TripDetailView.vue')
    },
    {
      path: '/trips/:id/edit',
      name: 'EditTrip',
      component: () => import('@/features/rides/EditTripView.vue'),
      meta: { requiresAuth: true }
    },
    {
      path: '/trips/:id/bookings',
      name: 'TripBookings',
      component: () => import('@/features/bookings/TripBookingsView.vue'),
      meta: { requiresAuth: true }
    },
    {
      path: '/my-trips',
      name: 'MyTrips',
      component: () => import('@/features/rides/MyTripsView.vue'),
      meta: { requiresAuth: true }
    },
    {
      path: '/my-bookings',
      name: 'MyBookings',
      component: () => import('@/features/bookings/MyBookingsView.vue'),
      meta: { requiresAuth: true }
    },
    {
      path: '/users/:id',
      name: 'Profile',
      component: () => import('@/features/profile/ProfileView.vue')
    },
    {
      path: '/profile/edit',
      name: 'EditProfile',
      component: () => import('@/features/profile/EditProfileView.vue'),
      meta: { requiresAuth: true }
    },
    {
      path: '/cars',
      name: 'Cars',
      component: () => import('@/features/profile/CarsView.vue'),
      meta: { requiresAuth: true, requiresDriver: true }
    },
    {
      path: '/chat',
      name: 'Chat',
      component: () => import('@/features/chat/ChatView.vue'),
      meta: { requiresAuth: true }
    },
    {
      path: '/chat/:channelId',
      name: 'ChatChannel',
      component: () => import('@/features/chat/ChatView.vue'),
      meta: { requiresAuth: true }
    },
    {
      path: '/:pathMatch(.*)*',
      redirect: '/'
    }
  ],
  scrollBehavior() {
    return { top: 0 }
  }
})

router.beforeEach(async to => {
  const authStore = useAuthStore()

  if (authStore.user === null && !authStore.loading) {
    await authStore.fetchMe()
  }

  if (to.meta.requiresAuth && !authStore.isLoggedIn) {
    return { name: 'Login', query: { redirect: to.fullPath } }
  }

  if (to.meta.requiresDriver && !authStore.isDriver) {
    return { name: 'Rides' }
  }
})

export default router
