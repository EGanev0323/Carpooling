import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '../stores/auth.store'

const routes = [
  {
    path: '/',
    name: 'Home',
    component: () => import('../pages/HomePage.vue'),
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('../pages/LoginPage.vue'),
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('../pages/RegisterPage.vue'),
  },
  {
    path: '/trips',
    name: 'SearchResults',
    component: () => import('../pages/SearchResultsPage.vue'),
  },
  {
    path: '/trips/create',
    name: 'CreateTrip',
    component: () => import('../pages/CreateTripPage.vue'),
    meta: { requiresAuth: true, requiresDriver: true },
  },
  {
    path: '/trips/:id',
    name: 'TripDetail',
    component: () => import('../pages/TripDetailPage.vue'),
  },
  {
    path: '/trips/:id/edit',
    name: 'EditTrip',
    component: () => import('../pages/EditTripPage.vue'),
    meta: { requiresAuth: true },
  },
  {
    path: '/trips/:id/bookings',
    name: 'TripBookings',
    component: () => import('../pages/TripBookingsPage.vue'),
    meta: { requiresAuth: true },
  },
  {
    path: '/my-trips',
    name: 'MyTrips',
    component: () => import('../pages/MyTripsPage.vue'),
    meta: { requiresAuth: true },
  },
  {
    path: '/my-bookings',
    name: 'MyBookings',
    component: () => import('../pages/MyBookingsPage.vue'),
    meta: { requiresAuth: true },
  },
  {
    path: '/users/:id',
    name: 'Profile',
    component: () => import('../pages/ProfilePage.vue'),
  },
  {
    path: '/profile/edit',
    name: 'EditProfile',
    component: () => import('../pages/EditProfilePage.vue'),
    meta: { requiresAuth: true },
  },
  {
    path: '/cars',
    name: 'Cars',
    component: () => import('../pages/CarsPage.vue'),
    meta: { requiresAuth: true, requiresDriver: true },
  },
  {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    component: () => import('../pages/NotFoundPage.vue'),
  },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
  scrollBehavior() {
    return { top: 0 }
  },
})

router.beforeEach(async (to) => {
  const authStore = useAuthStore()

  // Fetch user on first navigation if we haven't yet
  if (authStore.user === null && !authStore.loading) {
    await authStore.fetchMe()
  }

  if (to.meta.requiresAuth && !authStore.isAuthenticated) {
    return { name: 'Login', query: { redirect: to.fullPath } }
  }

  if (to.meta.requiresDriver && !authStore.isDriver) {
    return { name: 'Home' }
  }
})

export default router
