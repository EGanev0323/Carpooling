<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth.store'
import { usersApi } from '../api/users.api'
import { useToast } from 'primevue/usetoast'
import LoadingSpinner from '../components/LoadingSpinner.vue'
import EmptyState from '../components/EmptyState.vue'
import UserAvatar from '../components/UserAvatar.vue'
import UserRatingSummary from '../components/UserRatingSummary.vue'
import TripCard from '../components/TripCard.vue'
import RatingCard from '../components/RatingCard.vue'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()
const toast = useToast()

const profileUser = ref(null)
const trips = ref([])
const ratings = ref([])
const loading = ref(false)
const activeTab = ref(0)

const isOwnProfile = computed(
  () => authStore.user?.id == route.params.id
)

const fullName = computed(() => {
  if (!profileUser.value) return ''
  const u = profileUser.value
  return u.firstName && u.lastName ? `${u.firstName} ${u.lastName}` : u.username
})

onMounted(async () => {
  loading.value = true
  try {
    const [userRes, tripsRes, ratingsRes] = await Promise.all([
      usersApi.getById(route.params.id),
      usersApi.getTrips(route.params.id).catch(() => ({ data: [] })),
      usersApi.getRatings(route.params.id).catch(() => ({ data: [] })),
    ])
    profileUser.value = userRes.data
    trips.value = tripsRes.data
    ratings.value = ratingsRes.data
  } catch {
    toast.add({ severity: 'error', summary: 'Грешка при зареждане на профила', life: 3000 })
  } finally {
    loading.value = false
  }
})

const tabItems = [
  { label: 'Пътувания', icon: 'pi pi-car' },
  { label: 'Отзиви', icon: 'pi pi-star' },
]
</script>

<template>
  <div class="profile-page">
    <div class="page-inner">
      <LoadingSpinner v-if="loading" />

      <template v-else-if="profileUser">
        <!-- Profile header -->
        <Card class="profile-header-card">
          <template #content>
            <div class="profile-header">
              <UserAvatar :user="profileUser" size="6rem" />
              <div class="profile-info">
                <h1 class="profile-name">{{ fullName }}</h1>
                <p class="profile-username">@{{ profileUser.username }}</p>
                <UserRatingSummary :user="profileUser" />
                <div class="profile-badges">
                  <Tag v-if="profileUser.isDriver" value="Шофьор" icon="pi pi-car" severity="info" />
                  <Tag v-if="profileUser.isPassenger" value="Пътник" icon="pi pi-user" severity="secondary" />
                </div>
              </div>
              <Button
                v-if="isOwnProfile"
                label="Редактирай профила"
                icon="pi pi-pencil"
                outlined
                @click="router.push('/profile/edit')"
              />
            </div>
          </template>
        </Card>

        <!-- Tabs -->
        <Tabs :value="activeTab" @update:value="activeTab = $event">
          <TabList>
            <Tab :value="0">
              <i class="pi pi-car" /> Пътувания ({{ trips.length }})
            </Tab>
            <Tab :value="1">
              <i class="pi pi-star" /> Отзиви ({{ ratings.length }})
            </Tab>
          </TabList>

          <TabPanels>
            <TabPanel :value="0">
              <EmptyState
                v-if="trips.length === 0"
                message="Няма публикувани пътувания"
                icon="pi pi-car"
              />
              <div v-else class="trips-grid">
                <TripCard
                  v-for="trip in trips"
                  :key="trip.id"
                  :trip="trip"
                />
              </div>
            </TabPanel>

            <TabPanel :value="1">
              <EmptyState
                v-if="ratings.length === 0"
                message="Няма получени отзиви"
                icon="pi pi-star"
              />
              <div v-else class="ratings-list">
                <RatingCard
                  v-for="rating in ratings"
                  :key="rating.id"
                  :rating="rating"
                />
              </div>
            </TabPanel>
          </TabPanels>
        </Tabs>
      </template>

      <EmptyState
        v-else
        message="Потребителят не е намерен"
        icon="pi pi-user"
      />
    </div>
  </div>
</template>

<style scoped>
.profile-page {
  background: var(--p-surface-50);
  min-height: calc(100vh - 128px);
}

.page-inner {
  max-width: 900px;
  margin: 0 auto;
  padding: 2rem 1.5rem;
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}

.profile-header-card {
  border: 1px solid var(--p-surface-200);
}

.profile-header {
  display: flex;
  align-items: flex-start;
  gap: 1.5rem;
  flex-wrap: wrap;
}

.profile-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 0.4rem;
}

.profile-name {
  font-size: 1.5rem;
  font-weight: 700;
}

.profile-username {
  color: var(--p-text-muted-color);
  font-size: 0.9rem;
}

.profile-badges {
  display: flex;
  gap: 0.5rem;
  flex-wrap: wrap;
  margin-top: 0.25rem;
}

.trips-grid {
  display: flex;
  flex-direction: column;
  gap: 1rem;
  padding-top: 1rem;
}

.ratings-list {
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
  padding-top: 1rem;
}
</style>
