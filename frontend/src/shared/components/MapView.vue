<script setup lang="ts">
import { ref, onMounted, onUnmounted, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import type { RideResponse } from '@/types'

// Leaflet types — imported lazily below
import type * as L from 'leaflet'

interface Props {
  rides: RideResponse[]
  selectedRide?: RideResponse
  height?: string
}

const props = withDefaults(defineProps<Props>(), {
  height: '450px',
})

const { t, locale } = useI18n()

const mapContainer = ref<HTMLDivElement | null>(null)

// Runtime Leaflet references — populated after dynamic import
let leaflet: typeof L | null = null
let map: L.Map | null = null

// Track layers so we can remove them on re-render
let polylineLayers: L.Polyline[] = []
let markerLayers: L.Marker[] = []

// ──────────────────────────────────────────────
// Google Encoded Polyline decoder
// ──────────────────────────────────────────────
function decodePolyline(encoded: string): [number, number][] {
  const points: [number, number][] = []
  let index = 0
  let lat = 0
  let lng = 0
  while (index < encoded.length) {
    let shift = 0
    let result = 0
    let b: number
    do {
      b = encoded.charCodeAt(index++) - 63
      result |= (b & 0x1f) << shift
      shift += 5
    } while (b >= 0x20)
    lat += result & 1 ? ~(result >> 1) : result >> 1
    shift = result = 0
    do {
      b = encoded.charCodeAt(index++) - 63
      result |= (b & 0x1f) << shift
      shift += 5
    } while (b >= 0x20)
    lng += result & 1 ? ~(result >> 1) : result >> 1
    points.push([lat / 1e5, lng / 1e5])
  }
  return points
}

// ──────────────────────────────────────────────
// Custom circle SVG marker factory
// ──────────────────────────────────────────────
function makeCircleIcon(L: typeof import('leaflet'), color: string): L.DivIcon {
  const svg = `<svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 16 16">
    <circle cx="8" cy="8" r="6" fill="${color}" stroke="#fff" stroke-width="2"/>
  </svg>`
  return L.divIcon({
    html: svg,
    className: '',
    iconSize: [16, 16],
    iconAnchor: [8, 8],
    popupAnchor: [0, -10],
  })
}

// ──────────────────────────────────────────────
// Clear all map layers
// ──────────────────────────────────────────────
function clearLayers(): void {
  polylineLayers.forEach(p => p.remove())
  markerLayers.forEach(m => m.remove())
  polylineLayers = []
  markerLayers = []
}

// ──────────────────────────────────────────────
// Draw all rides onto the map
// ──────────────────────────────────────────────
function drawRides(L: typeof import('leaflet')): void {
  if (!map) return
  clearLayers()

  const allBounds: [number, number][] = []

  for (const ride of props.rides) {
    const isSelected = props.selectedRide?.publicId === ride.publicId
    const originCity = locale.value === 'bg' ? ride.originCity.nameBg : ride.originCity.nameEn
    const destCity = locale.value === 'bg' ? ride.destinationCity.nameBg : ride.destinationCity.nameEn
    const popupContent = `${originCity} → ${destCity}, ${ride.pricePerSeat} лв.`

    // Draw polyline if available
    if (ride.routePolyline) {
      const latlngs = decodePolyline(ride.routePolyline)
      if (latlngs.length > 0) {
        const polyline = L.polyline(latlngs, {
          color: isSelected ? '#F2C641' : '#3F7CAC',
          weight: isSelected ? 5 : 3,
          opacity: 0.85,
        })
        polyline.addTo(map)
        polylineLayers.push(polyline)
        allBounds.push(...latlngs)
      }
    }

    // Origin marker — yellow circle
    const originLatLng: [number, number] = [
      ride.originCity.latitude,
      ride.originCity.longitude,
    ]
    const originMarker = L.marker(originLatLng, {
      icon: makeCircleIcon(L, '#F2C641'),
      title: originCity,
    })
    originMarker.bindPopup(popupContent)
    originMarker.addTo(map)
    markerLayers.push(originMarker)
    allBounds.push(originLatLng)

    // Destination marker — blue circle
    const destLatLng: [number, number] = [
      ride.destinationCity.latitude,
      ride.destinationCity.longitude,
    ]
    const destMarker = L.marker(destLatLng, {
      icon: makeCircleIcon(L, '#3F7CAC'),
      title: destCity,
    })
    destMarker.bindPopup(popupContent)
    destMarker.addTo(map)
    markerLayers.push(destMarker)
    allBounds.push(destLatLng)
  }

  // Fit bounds around selected ride, otherwise all rides
  if (props.selectedRide) {
    const sr = props.selectedRide
    const boundsForSelected: [number, number][] = []

    if (sr.routePolyline) {
      boundsForSelected.push(...decodePolyline(sr.routePolyline))
    } else {
      boundsForSelected.push(
        [sr.originCity.latitude, sr.originCity.longitude],
        [sr.destinationCity.latitude, sr.destinationCity.longitude],
      )
    }

    if (boundsForSelected.length > 0) {
      map.fitBounds(L.latLngBounds(boundsForSelected), { padding: [40, 40] })
    }
  } else if (allBounds.length > 0) {
    map.fitBounds(L.latLngBounds(allBounds), { padding: [30, 30] })
  }
}

// ──────────────────────────────────────────────
// Lifecycle
// ──────────────────────────────────────────────
onMounted(async () => {
  if (!mapContainer.value) return

  // Dynamic import — SSR-safe, avoids Vite SSR issues
  const L = await import('leaflet')
  leaflet = L

  // Fix default icon path broken by Vite asset hashing
  // (Leaflet's default icons use _getIconUrl which breaks under bundlers)
  const defaultIcon = L.icon({
    iconUrl: 'https://unpkg.com/leaflet@1.9.4/dist/images/marker-icon.png',
    iconRetinaUrl: 'https://unpkg.com/leaflet@1.9.4/dist/images/marker-icon-2x.png',
    shadowUrl: 'https://unpkg.com/leaflet@1.9.4/dist/images/marker-shadow.png',
    iconSize: [25, 41],
    iconAnchor: [12, 41],
  })
  L.Marker.prototype.options.icon = defaultIcon

  map = L.map(mapContainer.value, {
    zoomControl: true,
    attributionControl: true,
  }).setView([42.7, 25.5], 7) // Bulgaria center

  L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
    attribution: '&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors',
    maxZoom: 18,
  }).addTo(map)

  if (props.rides.length > 0) {
    drawRides(L)
  }
})

onUnmounted(() => {
  map?.remove()
  map = null
  leaflet = null
})

watch(
  () => props.rides,
  () => {
    if (leaflet && map) {
      drawRides(leaflet)
    }
  },
  { deep: true },
)

watch(
  () => props.selectedRide,
  () => {
    if (leaflet && map) {
      drawRides(leaflet)
    }
  },
)
</script>

<template>
  <div class="map-view-wrapper" :style="{ height: props.height }">
    <div ref="mapContainer" class="map-view-container" />
    <div v-if="!mapContainer" class="map-view-loading">
      <i class="pi pi-spin pi-spinner" />
      <span>{{ t('map.loading') }}</span>
    </div>
  </div>
</template>

<style>
/* Leaflet CSS must be global — scoped will not work for leaflet internals */
@import 'leaflet/dist/leaflet.css';
</style>

<style scoped>
.map-view-wrapper {
  width: 100%;
  border-radius: 12px;
  overflow: hidden;
  position: relative;
  background: var(--p-surface-100, #f3f4f6);
}

.map-view-container {
  width: 100%;
  height: 100%;
}

.map-view-loading {
  position: absolute;
  inset: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: var(--spacing-2);
  color: var(--muted);
  font-size: var(--fs-small);
}
</style>
