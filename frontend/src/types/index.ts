export interface City {
  id: number
  slug: string
  nameBg: string
  nameEn: string
  latitude: number
  longitude: number
}

export interface CarResponse {
  id: number
  make: string
  model: string
  year: number
  color: string
  licensePlate: string
  seats: number
  amenities: string
  isOwner: boolean
}

export interface RideDriver {
  publicId: string
  firstName: string
  lastName: string
  avatarUrl: string | null
}

export interface RideCar {
  make: string
  model: string
  year: number
  color: string
  seats: number
}

export type RideStatus = 'ACTIVE' | 'COMPLETED' | 'CANCELLED'

export interface RideResponse {
  publicId: string
  driver: RideDriver
  car: RideCar
  originCity: City
  destinationCity: City
  departureAt: string
  arrivalAtEstimate: string | null
  totalSeats: number
  availableSeats: number
  pricePerSeat: number
  routePolyline: string | null
  description: string | null
  status: RideStatus
  avgRating: number | null
}

export interface RidePassenger {
  publicId: string
  firstName: string
  lastName: string
}

export interface BookingRideSummary {
  publicId: string
  originCity: City
  destinationCity: City
  departureAt: string
  pricePerSeat: number
  driverName: string
}

export type BookingStatus = 'PENDING' | 'CONFIRMED' | 'REJECTED' | 'CANCELLED' | 'NO_SHOW'

export interface BookingResponse {
  publicId: string
  ridePublicId: string
  passenger: RidePassenger
  seats: number
  status: BookingStatus
  message: string | null
  createdAt: string
  rideSummary: BookingRideSummary
}

export interface PageResponse<T> {
  content: T[]
  totalElements: number
  totalPages: number
  page: number
}

export interface CarFormData {
  make: string
  model: string
  year: number
  color: string
  licensePlate: string
  seats: number
  amenities: string
}

export interface RideSearchParams {
  originCityId?: number
  destinationCityId?: number
  date?: string
  page?: number
  size?: number
}

export interface CreateRideData {
  originCityId: number
  destinationCityId: number
  departureAt: string
  carId: number
  totalSeats: number
  pricePerSeat: number
  description?: string
}

export interface CreateBookingData {
  ridePublicId: string
  seats: number
  message?: string
}

export interface ChatParticipant {
  id: number
  firstName: string
  lastName: string
  avatarUrl: string | null
}

export interface ChatChannel {
  id: number
  ridePublicId: string
  originCity: string
  destinationCity: string
  driver: ChatParticipant
  passenger: ChatParticipant
  createdAt: string
  unreadCount: number
}

export interface ChatMessage {
  id: number
  channelId: number
  sender: ChatParticipant
  content: string
  createdAt: string
  readAt: string | null
}
