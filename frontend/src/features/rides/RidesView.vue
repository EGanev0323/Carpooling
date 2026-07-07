<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { useToast } from 'primevue/usetoast'
import { useCitiesStore } from '@/stores/cities'
import { ridesApi } from '@/shared/api/rides'
import RideCard from '@/shared/components/RideCard.vue'
import type { RideResponse, City } from '@/types'

const { t, locale } = useI18n()
const toast = useToast()
const citiesStore = useCitiesStore()

const rides = ref<RideResponse[]>([])
const loading = ref(false)
const totalRecords = ref(0)
const totalPages = ref(0)
const currentPage = ref(0)
const pageSize = 12

const selectedOrigin = ref<City | null>(null)
const selectedDestination = ref<City | null>(null)
const selectedDate = ref<Date | null>(null)

const cityOptions = computed(() =>
  citiesStore.cities.map(c => ({
    label: locale.value === 'bg' ? c.nameBg : c.nameEn,
    value: c,
  }))
)

async function searchRides(page = 0): Promise<void> {
  loading.value = true
  try {
    const params: {
      originCityId?: number
      destinationCityId?: number
      date?: string
      page: number
      size: number
    } = { page, size: pageSize }

    if (selectedOrigin.value) params.originCityId = selectedOrigin.value.id
    if (selectedDestination.value) params.destinationCityId = selectedDestination.value.id
    if (selectedDate.value) {
      params.date = selectedDate.value.toISOString().slice(0, 10)
    }

    const { data } = await ridesApi.search(params)
    rides.value = data.content
    totalRecords.value = data.totalElements
    totalPages.value = data.totalPages
    currentPage.value = data.page
  } catch {
    toast.add({
      severity: 'error',
      summary: t('common.notFound'),
      detail: t('auth.errors.generic'),
      life: 4000,
    })
  } finally {
    loading.value = false
  }
}

function onPageChange(event: { page: number }): void {
  searchRides(event.page)
}

function onSearch(): void {
  searchRides(0)
}

function clearFilters(): void {
  selectedOrigin.value = null
  selectedDestination.value = null
  selectedDate.value = null
  searchRides(0)
}

onMounted(async () => {
  await citiesStore.fetchCities()
  await searchRides(0)
})
</script>

<template>
  <div class="rides-view">
    <div class="rides-view__header">
      <h1 class="rides-view__title">{{ t('rides.title') }}</h1>
    </div>

    <div class="rides-view__search-panel">
      <div class="search-form">
        <div class="search-form__field">
          <label class="search-form__label">{{ t('rides.from') }}</label>
          <Select
            v-model="selectedOrigin"
            :options="cityOptions"
            optionLabel="label"
            optionValue="value"
            :placeholder="t('rides.from')"
            filter
            showClear
            class="search-form__select"
          />
        </div>

        <div class="search-form__field">
          <label class="search-form__label">{{ t('rides.to') }}</label>
          <Select
            v-model="selectedDestination"
            :options="cityOptions"
            optionLabel="label"
            optionValue="value"
            :placeholder="t('rides.to')"
            filter
            showClear
            class="search-form__select"
          />
        </div>

        <div class="search-form__field">
          <label class="search-form__label">{{ t('rides.date') }}</label>
          <DatePicker
            v-model="selectedDate"
            :placeholder="t('rides.date')"
            dateFormat="dd.mm.yy"
            showClear
            class="search-form__datepicker"
          />
        </div>

        <div class="search-form__actions">
          <Button
            :label="t('rides.searchButton')"
            icon="pi pi-search"
            @click="onSearch"
            :loading="loading"
          />
          <Button
            :label="t('common.cancel')"
            icon="pi pi-times"
            severity="secondary"
            text
            @click="clearFilters"
          />
        </div>
      </div>
    </div>

    <div v-if="loading" class="rides-view__skeletons">
      <div v-for="n in 6" :key="n" class="skeleton-card">
        <Skeleton height="220px" border-radius="12px" />
      </div>
    </div>

    <template v-else>
      <div v-if="rides.length === 0" class="rides-view__empty">
        <i class="pi pi-search empty-icon" />
        <p class="empty-text">{{ t('rides.noResults') }}</p>
        <Button
          :label="t('rides.searchButton')"
          text
          icon="pi pi-refresh"
          @click="clearFilters"
        />
      </div>

      <template v-else>
        <p class="rides-view__count">
          {{ totalRecords }} {{ t('trips.found') }}
        </p>
        <div class="rides-grid">
          <RideCard
            v-for="ride in rides"
            :key="ride.publicId"
            :ride="ride"
          />
        </div>

        <Paginator
          v-if="totalPages > 1"
          :rows="pageSize"
          :totalRecords="totalRecords"
          :first="currentPage * pageSize"
          @page="onPageChange"
          class="rides-view__paginator"
        />
      </template>
    </template>
  </div>
</template>

<style scoped>
.rides-view {
  padding: var(--spacing-4) 0;
}

.rides-view__header {
  margin-bottom: var(--spacing-6);
}

.rides-view__title {
  font-size: var(--fs-h1);
  font-weight: 700;
  margin: 0;
}

.rides-view__search-panel {
  background: var(--p-surface-50, var(--p-surface-0));
  border: 1px solid var(--rule);
  border-radius: 12px;
  padding: var(--spacing-5);
  margin-bottom: var(--spacing-6);
}

.search-form {
  display: flex;
  flex-wrap: wrap;
  gap: var(--spacing-4);
  align-items: flex-end;
}

.search-form__field {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-1);
  flex: 1;
  min-width: 180px;
}

.search-form__label {
  font-size: var(--fs-small);
  font-weight: 500;
  color: var(--muted);
}

.search-form__select,
.search-form__datepicker {
  width: 100%;
}

.search-form__actions {
  display: flex;
  gap: var(--spacing-2);
  align-items: center;
  padding-top: 1.4rem;
}

.rides-view__count {
  color: var(--muted);
  font-size: var(--fs-small);
  margin-bottom: var(--spacing-4);
}

.rides-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
  gap: var(--spacing-4);
}

.rides-view__skeletons {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
  gap: var(--spacing-4);
}

.skeleton-card {
  border-radius: 12px;
  overflow: hidden;
}

.rides-view__empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 40vh;
  gap: var(--spacing-4);
  color: var(--muted);
  text-align: center;
}

.empty-icon {
  font-size: 4rem;
}

.empty-text {
  font-size: var(--fs-h3);
  margin: 0;
}

.rides-view__paginator {
  margin-top: var(--spacing-6);
}
</style>
