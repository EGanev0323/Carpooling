<script setup lang="ts">
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { useAuthStore } from '@/stores/auth'
import { useUiStore } from '@/stores/ui'

const router = useRouter()
const { t } = useI18n()
const authStore = useAuthStore()
const uiStore = useUiStore()

const mobileMenuOpen = ref(false)
const userMenuRef = ref<{ toggle: (event: Event) => void } | null>(null)

const userMenuItems = computed(() => [
  {
    label: t('nav.myTrips'),
    icon: 'pi pi-car',
    command: () => router.push('/my-trips')
  },
  {
    label: t('nav.myBookings'),
    icon: 'pi pi-ticket',
    command: () => router.push('/my-bookings')
  },
  ...(authStore.isDriver
    ? [
        {
          label: t('nav.myCars'),
          icon: 'pi pi-list',
          command: () => router.push('/cars')
        }
      ]
    : []),
  {
    label: t('nav.profile'),
    icon: 'pi pi-user',
    command: () => router.push(`/users/${authStore.user?.id}`)
  },
  { separator: true },
  {
    label: t('nav.logout'),
    icon: 'pi pi-sign-out',
    command: () => authStore.logout()
  }
])

function toggleUserMenu(event: Event) {
  userMenuRef.value?.toggle(event)
}

function toggleMobile() {
  mobileMenuOpen.value = !mobileMenuOpen.value
}
</script>

<template>
  <nav class="app-header">
    <div class="header-inner">
      <RouterLink to="/" class="header-logo">
        <i class="pi pi-car" style="font-size: 1.4rem" />
        <span>Карпулинг</span>
      </RouterLink>

      <div class="header-center desktop-only">
        <RouterLink to="/trips" class="nav-link">
          <i class="pi pi-search" />
          {{ t('nav.searchTrip') }}
        </RouterLink>
        <RouterLink v-if="authStore.isDriver" to="/trips/create" class="nav-link">
          <i class="pi pi-plus" />
          {{ t('nav.publishTrip') }}
        </RouterLink>
      </div>

      <div class="header-right desktop-only">
        <Button
          text
          :icon="uiStore.theme === 'dark' ? 'pi pi-sun' : 'pi pi-moon'"
          :aria-label="uiStore.theme === 'dark' ? 'Switch to light mode' : 'Switch to dark mode'"
          @click="uiStore.toggleTheme()"
          class="theme-toggle-btn"
        />

        <template v-if="authStore.isLoggedIn">
          <Button
            text
            class="user-menu-btn"
            @click="toggleUserMenu"
            :aria-label="authStore.user?.email"
          >
            <i class="pi pi-user" />
            <span>{{ authStore.user?.firstName ?? authStore.user?.email }}</span>
            <i class="pi pi-chevron-down" style="font-size: 0.75rem" />
          </Button>
          <Menu ref="userMenuRef" :model="userMenuItems" popup />
        </template>
        <template v-else>
          <Button
            :label="t('nav.login')"
            text
            icon="pi pi-sign-in"
            @click="router.push('/login')"
          />
          <Button
            :label="t('nav.register')"
            icon="pi pi-user-plus"
            @click="router.push('/register')"
          />
        </template>
      </div>

      <Button
        class="mobile-only"
        text
        icon="pi pi-bars"
        @click="toggleMobile"
        aria-label="Menu"
      />
    </div>

    <div v-if="mobileMenuOpen" class="mobile-menu">
      <RouterLink to="/trips" class="mobile-nav-link" @click="mobileMenuOpen = false">
        <i class="pi pi-search" /> {{ t('nav.searchTrip') }}
      </RouterLink>
      <RouterLink v-if="authStore.isDriver" to="/trips/create" class="mobile-nav-link" @click="mobileMenuOpen = false">
        <i class="pi pi-plus" /> {{ t('nav.publishTrip') }}
      </RouterLink>
      <template v-if="authStore.isLoggedIn">
        <RouterLink to="/my-trips" class="mobile-nav-link" @click="mobileMenuOpen = false">
          <i class="pi pi-car" /> {{ t('nav.myTrips') }}
        </RouterLink>
        <RouterLink to="/my-bookings" class="mobile-nav-link" @click="mobileMenuOpen = false">
          <i class="pi pi-ticket" /> {{ t('nav.myBookings') }}
        </RouterLink>
        <RouterLink v-if="authStore.isDriver" to="/cars" class="mobile-nav-link" @click="mobileMenuOpen = false">
          <i class="pi pi-list" /> {{ t('nav.myCars') }}
        </RouterLink>
        <RouterLink :to="`/users/${authStore.user?.id}`" class="mobile-nav-link" @click="mobileMenuOpen = false">
          <i class="pi pi-user" /> {{ t('nav.profile') }}
        </RouterLink>
        <a class="mobile-nav-link danger" @click="authStore.logout(); mobileMenuOpen = false">
          <i class="pi pi-sign-out" /> {{ t('nav.logout') }}
        </a>
      </template>
      <template v-else>
        <RouterLink to="/login" class="mobile-nav-link" @click="mobileMenuOpen = false">
          <i class="pi pi-sign-in" /> {{ t('nav.login') }}
        </RouterLink>
        <RouterLink to="/register" class="mobile-nav-link" @click="mobileMenuOpen = false">
          <i class="pi pi-user-plus" /> {{ t('nav.register') }}
        </RouterLink>
      </template>
      <div class="mobile-theme-toggle">
        <Button
          text
          :icon="uiStore.theme === 'dark' ? 'pi pi-sun' : 'pi pi-moon'"
          :label="uiStore.theme === 'dark' ? 'Light mode' : 'Dark mode'"
          @click="uiStore.toggleTheme()"
        />
      </div>
    </div>
  </nav>
</template>

<style scoped>
.app-header {
  background: var(--p-surface-0);
  border-bottom: 1px solid var(--rule);
  position: sticky;
  top: 0;
  z-index: 100;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.08);
}

.header-inner {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 var(--spacing-6);
  height: 64px;
  display: flex;
  align-items: center;
  gap: var(--spacing-6);
}

.header-logo {
  display: flex;
  align-items: center;
  gap: var(--spacing-2);
  text-decoration: none;
  font-size: 1.1rem;
  font-weight: 700;
  color: var(--mark);
  white-space: nowrap;
}

.header-center {
  flex: 1;
  display: flex;
  align-items: center;
  gap: var(--spacing-2);
}

.header-right {
  display: flex;
  align-items: center;
  gap: var(--spacing-2);
}

.nav-link {
  display: flex;
  align-items: center;
  gap: var(--spacing-1);
  text-decoration: none;
  color: var(--text);
  padding: var(--spacing-2) var(--spacing-3);
  border-radius: 6px;
  font-size: var(--fs-small);
  transition: background 0.15s;
}

.nav-link:hover,
.nav-link.router-link-active {
  background: var(--rule);
  color: var(--mark);
  text-decoration: none;
}

.user-menu-btn {
  display: flex;
  align-items: center;
  gap: var(--spacing-1);
}

.theme-toggle-btn {
  opacity: 0.7;
  transition: opacity 0.15s;
}

.theme-toggle-btn:hover {
  opacity: 1;
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
  padding: var(--spacing-2) var(--spacing-4) var(--spacing-4);
  border-top: 1px solid var(--rule);
  background: var(--p-surface-0);
}

.mobile-nav-link {
  display: flex;
  align-items: center;
  gap: var(--spacing-2);
  text-decoration: none;
  color: var(--text);
  padding: var(--spacing-3) var(--spacing-2);
  border-bottom: 1px solid var(--rule);
  cursor: pointer;
  font-size: var(--fs-body);
}

.mobile-nav-link.danger {
  color: var(--warn);
}

.mobile-theme-toggle {
  padding-top: var(--spacing-3);
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
