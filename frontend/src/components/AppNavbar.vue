<script setup>
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth.store'

const router = useRouter()
const authStore = useAuthStore()

const mobileMenuOpen = ref(false)
const userMenuRef = ref(null)

const userMenuItems = computed(() => [
  {
    label: 'Моите пътувания',
    icon: 'pi pi-car',
    command: () => router.push('/my-trips'),
  },
  {
    label: 'Моите резервации',
    icon: 'pi pi-ticket',
    command: () => router.push('/my-bookings'),
  },
  ...(authStore.isDriver
    ? [
        {
          label: 'Моите коли',
          icon: 'pi pi-list',
          command: () => router.push('/cars'),
        },
      ]
    : []),
  {
    label: 'Профил',
    icon: 'pi pi-user',
    command: () => router.push(`/users/${authStore.user?.id}`),
  },
  { separator: true },
  {
    label: 'Изход',
    icon: 'pi pi-sign-out',
    command: () => authStore.logout(),
  },
])

function toggleUserMenu(event) {
  userMenuRef.value.toggle(event)
}

function toggleMobile() {
  mobileMenuOpen.value = !mobileMenuOpen.value
}
</script>

<template>
  <nav class="app-navbar">
    <div class="navbar-inner">
      <!-- Logo -->
      <RouterLink to="/" class="navbar-logo">
        <i class="pi pi-car" style="font-size: 1.4rem" />
        <span>Споделено пътуване</span>
      </RouterLink>

      <!-- Center links (desktop) -->
      <div class="navbar-center desktop-only">
        <RouterLink to="/trips" class="nav-link">
          <i class="pi pi-search" />
          Търси пътуване
        </RouterLink>
        <RouterLink v-if="authStore.isDriver" to="/trips/create" class="nav-link">
          <i class="pi pi-plus" />
          Публикувай пътуване
        </RouterLink>
      </div>

      <!-- Right side -->
      <div class="navbar-right desktop-only">
        <template v-if="authStore.isAuthenticated">
          <Button
            text
            class="user-menu-btn"
            @click="toggleUserMenu"
            :aria-label="authStore.user?.username"
          >
            <i class="pi pi-user" />
            <span>{{ authStore.user?.firstName || authStore.user?.username }}</span>
            <i class="pi pi-chevron-down" style="font-size: 0.75rem" />
          </Button>
          <Menu ref="userMenuRef" :model="userMenuItems" popup />
        </template>
        <template v-else>
          <Button
            label="Вход"
            text
            icon="pi pi-sign-in"
            @click="router.push('/login')"
          />
          <Button
            label="Регистрация"
            icon="pi pi-user-plus"
            @click="router.push('/register')"
          />
        </template>
      </div>

      <!-- Mobile hamburger -->
      <Button
        class="mobile-only"
        text
        icon="pi pi-bars"
        @click="toggleMobile"
        aria-label="Menu"
      />
    </div>

    <!-- Mobile menu -->
    <div v-if="mobileMenuOpen" class="mobile-menu">
      <RouterLink to="/trips" class="mobile-nav-link" @click="mobileMenuOpen = false">
        <i class="pi pi-search" /> Търси пътуване
      </RouterLink>
      <RouterLink v-if="authStore.isDriver" to="/trips/create" class="mobile-nav-link" @click="mobileMenuOpen = false">
        <i class="pi pi-plus" /> Публикувай пътуване
      </RouterLink>
      <template v-if="authStore.isAuthenticated">
        <RouterLink to="/my-trips" class="mobile-nav-link" @click="mobileMenuOpen = false">
          <i class="pi pi-car" /> Моите пътувания
        </RouterLink>
        <RouterLink to="/my-bookings" class="mobile-nav-link" @click="mobileMenuOpen = false">
          <i class="pi pi-ticket" /> Моите резервации
        </RouterLink>
        <RouterLink v-if="authStore.isDriver" to="/cars" class="mobile-nav-link" @click="mobileMenuOpen = false">
          <i class="pi pi-list" /> Моите коли
        </RouterLink>
        <RouterLink :to="`/users/${authStore.user?.id}`" class="mobile-nav-link" @click="mobileMenuOpen = false">
          <i class="pi pi-user" /> Профил
        </RouterLink>
        <a class="mobile-nav-link danger" @click="authStore.logout(); mobileMenuOpen = false">
          <i class="pi pi-sign-out" /> Изход
        </a>
      </template>
      <template v-else>
        <RouterLink to="/login" class="mobile-nav-link" @click="mobileMenuOpen = false">
          <i class="pi pi-sign-in" /> Вход
        </RouterLink>
        <RouterLink to="/register" class="mobile-nav-link" @click="mobileMenuOpen = false">
          <i class="pi pi-user-plus" /> Регистрация
        </RouterLink>
      </template>
    </div>
  </nav>
</template>

<style scoped>
.app-navbar {
  background: var(--p-surface-0);
  border-bottom: 1px solid var(--p-surface-200);
  position: sticky;
  top: 0;
  z-index: 100;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.08);
}

.navbar-inner {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 1.5rem;
  height: 64px;
  display: flex;
  align-items: center;
  gap: 1.5rem;
}

.navbar-logo {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  text-decoration: none;
  font-size: 1.1rem;
  font-weight: 700;
  color: var(--p-primary-color);
  white-space: nowrap;
}

.navbar-center {
  flex: 1;
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.navbar-right {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.nav-link {
  display: flex;
  align-items: center;
  gap: 0.4rem;
  text-decoration: none;
  color: var(--p-text-color);
  padding: 0.5rem 0.75rem;
  border-radius: 6px;
  font-size: 0.9rem;
  transition: background 0.15s;
}

.nav-link:hover,
.nav-link.router-link-active {
  background: var(--p-surface-100);
  color: var(--p-primary-color);
}

.user-menu-btn {
  display: flex;
  align-items: center;
  gap: 0.4rem;
}

.desktop-only {
  display: flex;
}

.mobile-only {
  display: none;
  margin-left: auto;
}

.mobile-menu {
  display: flex;
  flex-direction: column;
  padding: 0.5rem 1rem 1rem;
  border-top: 1px solid var(--p-surface-200);
  background: var(--p-surface-0);
}

.mobile-nav-link {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  text-decoration: none;
  color: var(--p-text-color);
  padding: 0.75rem 0.5rem;
  border-bottom: 1px solid var(--p-surface-100);
  cursor: pointer;
  font-size: 0.95rem;
}

.mobile-nav-link.danger {
  color: var(--p-red-500);
}

@media (max-width: 768px) {
  .desktop-only {
    display: none !important;
  }
  .mobile-only {
    display: flex !important;
  }
}
</style>
