<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { useToast } from 'primevue/usetoast'
import { useConfirm } from 'primevue/useconfirm'
import { useRouter } from 'vue-router'
import { ridesApi } from '@/shared/api/rides'
import RideCard from '@/shared/components/RideCard.vue'
import type { RideResponse } from '@/types'

const { t } = useI18n()
const toast = useToast()
const confirm = useConfirm()
const router = useRouter()

const rides = ref<RideResponse[]>([])
const loading = ref(false)
const cancellingId = ref<string | null>(null)

async function loadRides(): Promise<void> {
  loading.value = true
  try {
    const { data } = await ridesApi.getMy()
    rides.value = data
  } catch {
    toast.add({
      severity: 'error',
      summary: t('auth.errors.generic'),
      life: 4000,
    })
  } finally {
    loading.value = false
  }
}

function confirmCancel(ride: RideResponse): void {
  confirm.require({
    message: `Сигурен ли си, че искаш да отмениш маршрута ${ride.originCity.nameBg} → ${ride.destinationCity.nameBg}?`,
    header: t('rides.cancel'),
    icon: 'pi pi-exclamation-triangle',
    acceptLabel: t('common.confirm'),
    rejectLabel: t('common.cancel'),
    acceptClass: 'p-button-danger',
    accept: () => doCancel(ride.publicId),
  })
}

async function doCancel(publicId: string): Promise<void> {
  cancellingId.value = publicId
  try {
    await ridesApi.cancel(publicId)
    const idx = rides.value.findIndex(r => r.publicId === publicId)
    if (idx !== -1) {
      rides.value[idx] = { ...rides.value[idx], status: 'CANCELLED' }
    }
    toast.add({
      severity: 'success',
      summary: t('rides.cancel'),
      detail: 'Маршрутът е отменен',
      life: 3000,
    })
  } catch {
    toast.add({
      severity: 'error',
      summary: t('auth.errors.generic'),
      life: 4000,
    })
  } finally {
    cancellingId.value = null
  }
}

onMounted(loadRides)
</script>

<template>
  <div class="my-trips">
    <div class="my-trips__header">
      <h1 class="my-trips__title">{{ t('rides.myTitle') }}</h1>
      <Button
        :label="t('rides.createTitle')"
        icon="pi pi-plus"
        @click="router.push('/trips/create')"
      />
    </div>

    <div v-if="loading" class="my-trips__skeletons">
      <Skeleton v-for="n in 3" :key="n" height="220px" border-radius="12px" />
    </div>

    <div v-else-if="rides.length === 0" class="my-trips__empty">
      <i class="pi pi-car empty-icon" />
      <p class="empty-text">{{ t('rides.noResults') }}</p>
      <Button
        :label="t('rides.createTitle')"
        icon="pi pi-plus"
        @click="router.push('/trips/create')"
      />
    </div>

    <div v-else class="my-trips__list">
      <div v-for="ride in rides" :key="ride.publicId" class="my-trips__item">
        <RideCard :ride="ride" />
        <div class="my-trips__actions">
          <Button
            :label="'Резервации'"
            icon="pi pi-list"
            severity="secondary"
            outlined
            @click="router.push(`/trips/${ride.publicId}/bookings`)"
          />
          <Button
            v-if="ride.status === 'ACTIVE'"
            :label="t('rides.cancel')"
            icon="pi pi-times"
            severity="danger"
            outlined
            :loading="cancellingId === ride.publicId"
            @click="confirmCancel(ride)"
          />
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.my-trips {
  padding: var(--spacing-4) 0;
}

.my-trips__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: var(--spacing-6);
}

.my-trips__title {
  font-size: var(--fs-h1);
  font-weight: 700;
  margin: 0;
}

.my-trips__skeletons {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-4);
}

.my-trips__empty {
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

.my-trips__list {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-5);
}

.my-trips__item {
  display: grid;
  grid-template-columns: 1fr auto;
  gap: var(--spacing-4);
  align-items: start;
}

@media (max-width: 700px) {
  .my-trips__item {
    grid-template-columns: 1fr;
  }
}

.my-trips__actions {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-2);
  padding-top: var(--spacing-2);
}
</style>
