<script setup>
import { ref, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useTripsStore } from '../stores/trips.store'
import TripSearchForm from '../components/TripSearchForm.vue'
import TripFiltersPanel from '../components/TripFiltersPanel.vue'
import TripCard from '../components/TripCard.vue'
import LoadingSpinner from '../components/LoadingSpinner.vue'
import EmptyState from '../components/EmptyState.vue'
import PaginationBar from '../components/PaginationBar.vue'

const route = useRoute()
const router = useRouter()
const tripsStore = useTripsStore()

const searchParams = ref({ ...route.query })
const filters = ref({})
const currentPage = ref(0)

async function doSearch(page = 0) {
  const params = {
    ...searchParams.value,
    ...filters.value,
    page,
    size: 10,
  }
  await tripsStore.searchTrips(params)
}

onMounted(() => doSearch())

watch(
  () => route.query,
  (q) => {
    searchParams.value = { ...q }
    currentPage.value = 0
    doSearch(0)
  }
)

function handleSearch(params) {
  searchParams.value = params
  currentPage.value = 0
  router.replace({ path: '/trips', query: params })
  doSearch(0)
}

function handlePageChange(page) {
  currentPage.value = page
  doSearch(page)
  window.scrollTo({ top: 0, behavior: 'smooth' })
}

watch(filters, () => {
  currentPage.value = 0
  doSearch(0)
}, { deep: true })
</script>

<template>
  <div class="search-page">
    <div class="search-header">
      <div class="search-header-inner">
        <TripSearchForm :modelValue="searchParams" @search="handleSearch" />
      </div>
    </div>

    <div class="search-body">
      <aside class="filters-sidebar">
        <TripFiltersPanel v-model="filters" />
      </aside>

      <main class="results-main">
        <div class="results-header">
          <h2 class="results-title">
            <template v-if="searchParams.originCity || searchParams.destinationCity">
              {{ searchParams.originCity || '...' }}
              <i class="pi pi-arrow-right" />
              {{ searchParams.destinationCity || '...' }}
            </template>
            <template v-else>Всички пътувания</template>
          </h2>
          <span v-if="!tripsStore.loading" class="results-count">
            {{ tripsStore.trips.length }} намерени
          </span>
        </div>

        <LoadingSpinner v-if="tripsStore.loading" />

        <template v-else>
          <EmptyState
            v-if="tripsStore.trips.length === 0"
            message="Няма намерени пътувания за тези критерии"
            icon="pi pi-car"
          />

          <div v-else class="trips-grid">
            <TripCard
              v-for="trip in tripsStore.trips"
              :key="trip.id"
              :trip="trip"
            />
          </div>

          <PaginationBar
            :totalPages="tripsStore.totalPages"
            v-model="currentPage"
            @update:modelValue="handlePageChange"
          />
        </template>
      </main>
    </div>
  </div>
</template>

<style scoped>
.search-page {
  min-height: calc(100vh - 128px);
}

.search-header {
  background: var(--p-surface-100);
  border-bottom: 1px solid var(--p-surface-200);
  padding: 1.25rem 1.5rem;
}

.search-header-inner {
  max-width: 1200px;
  margin: 0 auto;
}

.search-body {
  max-width: 1200px;
  margin: 0 auto;
  padding: 1.5rem;
  display: grid;
  grid-template-columns: 260px 1fr;
  gap: 1.5rem;
  align-items: start;
}

.filters-sidebar {
  position: sticky;
  top: 80px;
}

.results-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 1.25rem;
  gap: 1rem;
}

.results-title {
  font-size: 1.25rem;
  font-weight: 600;
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.results-count {
  color: var(--p-text-muted-color);
  font-size: 0.875rem;
  white-space: nowrap;
}

.trips-grid {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

@media (max-width: 768px) {
  .search-body {
    grid-template-columns: 1fr;
  }

  .filters-sidebar {
    position: static;
  }
}
</style>
