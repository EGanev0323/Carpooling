<script setup lang="ts">
import { computed } from 'vue'
import { useI18n } from 'vue-i18n'
import { useRouter } from 'vue-router'
import type { RideResponse } from '@/types'

const props = defineProps<{ ride: RideResponse }>()

const { t, locale } = useI18n()
const router = useRouter()

const cityName = (city: { nameBg: string; nameEn: string }): string =>
  locale.value === 'bg' ? city.nameBg : city.nameEn

const formattedDate = computed(() => {
  const fmt = new Intl.DateTimeFormat(locale.value === 'bg' ? 'bg-BG' : 'en-GB', {
    timeZone: 'Europe/Sofia',
    day: '2-digit',
    month: 'short',
    year: 'numeric',
    hour: '2-digit',
    minute: '2-digit',
  })
  return fmt.format(new Date(props.ride.departureAt))
})

const starsArray = computed(() => {
  const rating = props.ride.avgRating ?? 0
  return Array.from({ length: 5 }, (_, i) => i < Math.round(rating))
})

const statusSeverity = computed((): 'danger' | 'secondary' => {
  return props.ride.status === 'CANCELLED' ? 'danger' : 'secondary'
})

function goToDetail(): void {
  router.push(`/trips/${props.ride.publicId}`)
}
</script>

<template>
  <Card class="ride-card" :class="{ 'ride-card--inactive': ride.status !== 'ACTIVE' }">
    <template #header>
      <div v-if="ride.status !== 'ACTIVE'" class="ride-card__status-banner">
        <Tag
          :value="t(`bookings.status.${ride.status}`)"
          :severity="statusSeverity"
        />
      </div>
    </template>

    <template #content>
      <div class="ride-card__route">
        <div class="ride-card__city">
          <i class="pi pi-map-marker" />
          <span class="city-name">{{ cityName(ride.originCity) }}</span>
        </div>
        <div class="ride-card__arrow">
          <i class="pi pi-arrow-right" />
        </div>
        <div class="ride-card__city">
          <i class="pi pi-map-marker ride-card__city--dest" />
          <span class="city-name">{{ cityName(ride.destinationCity) }}</span>
        </div>
      </div>

      <div class="ride-card__date">
        <i class="pi pi-clock" />
        <span>{{ formattedDate }}</span>
      </div>

      <Divider />

      <div class="ride-card__meta">
        <div class="ride-card__driver">
          <Avatar
            :image="ride.driver.avatarUrl ?? undefined"
            :label="!ride.driver.avatarUrl ? ride.driver.firstName.charAt(0) : undefined"
            shape="circle"
            size="normal"
          />
          <div class="ride-card__driver-info">
            <span class="driver-name">{{ ride.driver.firstName }} {{ ride.driver.lastName }}</span>
            <div v-if="ride.avgRating !== null" class="ride-card__stars">
              <i
                v-for="(filled, idx) in starsArray"
                :key="idx"
                :class="filled ? 'pi pi-star-fill' : 'pi pi-star'"
                class="star-icon"
              />
              <span class="rating-value">{{ ride.avgRating.toFixed(1) }}</span>
            </div>
          </div>
        </div>

        <div class="ride-card__car">
          <i class="pi pi-car" />
          <span>{{ ride.car.make }} {{ ride.car.model }} ({{ ride.car.year }})</span>
        </div>
      </div>

      <div class="ride-card__footer">
        <div class="ride-card__price">
          <span class="price-amount">{{ ride.pricePerSeat }} лв.</span>
          <span class="price-label">{{ t('rides.pricePerSeat') }}</span>
        </div>
        <div class="ride-card__seats">
          <i class="pi pi-users" />
          <span>
            {{ ride.availableSeats }}/{{ ride.totalSeats }}
            {{ t('rides.seats') }}
          </span>
        </div>
      </div>
    </template>

    <template #footer>
      <Button
        :label="t('rides.details')"
        icon="pi pi-arrow-right"
        icon-pos="right"
        @click="goToDetail"
        class="ride-card__btn"
        :disabled="ride.status === 'CANCELLED'"
      />
    </template>
  </Card>
</template>

<style scoped>
.ride-card {
  transition: box-shadow 0.18s, transform 0.18s;
  height: 100%;
  display: flex;
  flex-direction: column;
}

.ride-card:hover {
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.15);
  transform: translateY(-2px);
}

.ride-card--inactive {
  opacity: 0.75;
}

.ride-card__status-banner {
  padding: var(--spacing-2) var(--spacing-3);
  display: flex;
  justify-content: flex-end;
}

.ride-card__route {
  display: flex;
  align-items: center;
  gap: var(--spacing-3);
  margin-bottom: var(--spacing-3);
}

.ride-card__city {
  display: flex;
  align-items: center;
  gap: var(--spacing-1);
  flex: 1;
}

.city-name {
  font-weight: 600;
  font-size: var(--fs-body);
}

.ride-card__city--dest {
  color: var(--mark);
}

.ride-card__arrow {
  color: var(--muted);
  flex-shrink: 0;
}

.ride-card__date {
  display: flex;
  align-items: center;
  gap: var(--spacing-2);
  color: var(--muted);
  font-size: var(--fs-small);
  margin-bottom: var(--spacing-2);
}

.ride-card__meta {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-2);
  margin-bottom: var(--spacing-3);
}

.ride-card__driver {
  display: flex;
  align-items: center;
  gap: var(--spacing-3);
}

.ride-card__driver-info {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-1);
}

.driver-name {
  font-weight: 500;
  font-size: var(--fs-body);
}

.ride-card__stars {
  display: flex;
  align-items: center;
  gap: 2px;
}

.star-icon {
  font-size: 0.75rem;
  color: #f59e0b;
}

.rating-value {
  font-size: var(--fs-small);
  color: var(--muted);
  margin-left: var(--spacing-1);
}

.ride-card__car {
  display: flex;
  align-items: center;
  gap: var(--spacing-2);
  color: var(--muted);
  font-size: var(--fs-small);
}

.ride-card__footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.ride-card__price {
  display: flex;
  align-items: baseline;
  gap: var(--spacing-1);
}

.price-amount {
  font-size: 1.25rem;
  font-weight: 700;
  color: var(--mark);
}

.price-label {
  font-size: var(--fs-small);
  color: var(--muted);
}

.ride-card__seats {
  display: flex;
  align-items: center;
  gap: var(--spacing-1);
  font-size: var(--fs-small);
  color: var(--muted);
}

.ride-card__btn {
  width: 100%;
}
</style>
