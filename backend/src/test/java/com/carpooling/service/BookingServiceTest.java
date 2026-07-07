package com.carpooling.service;

import com.carpooling.dto.BookingRequest;
import com.carpooling.dto.BookingResponse;
import com.carpooling.entity.*;
import com.carpooling.exception.BusinessException;
import com.carpooling.exception.UnauthorizedException;
import com.carpooling.mapper.BookingMapper;
import com.carpooling.repository.BookingRepository;
import com.carpooling.repository.TripRepository;
import com.carpooling.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookingServiceTest {

    @Mock private BookingRepository bookingRepository;
    @Mock private TripRepository tripRepository;
    @Mock private UserRepository userRepository;
    @Mock private BookingMapper bookingMapper;
    @Mock private Authentication auth;

    @InjectMocks
    private BookingService bookingService;

    private User driver;
    private User passenger;
    private Trip trip;
    private BookingRequest bookingRequest;

    @BeforeEach
    void setUp() {
        driver = User.builder().id(1L).username("driver").isDriver(true).isActive(true)
                .avgRating(BigDecimal.ZERO).ratingCount(0).tripsAsDriver(0).tripsAsPassenger(0).build();

        passenger = User.builder().id(2L).username("passenger").isPassenger(true).isActive(true)
                .avgRating(BigDecimal.ZERO).ratingCount(0).tripsAsDriver(0).tripsAsPassenger(0).build();

        trip = Trip.builder()
                .id(10L)
                .driver(driver)
                .status(TripStatus.SCHEDULED)
                .totalSeats(4)
                .availableSeats(4)
                .pricePerSeat(BigDecimal.valueOf(10))
                .departureTime(LocalDateTime.now().plusDays(1))
                .originCity("Sofia")
                .destinationCity("Plovdiv")
                .createdAt(OffsetDateTime.now())
                .updatedAt(OffsetDateTime.now())
                .build();

        bookingRequest = new BookingRequest(2, "Please approve");
    }

    @Test
    void bookTrip_whenTripNotScheduled_throwsBusinessException() {
        trip.setStatus(TripStatus.CANCELLED);
        when(auth.getName()).thenReturn("passenger");
        when(userRepository.findByUsername("passenger")).thenReturn(Optional.of(passenger));
        when(tripRepository.findByIdWithLock(10L)).thenReturn(Optional.of(trip));

        assertThatThrownBy(() -> bookingService.bookTrip(10L, bookingRequest, auth))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("not available");
    }

    @Test
    void bookTrip_whenDriverBooksOwnTrip_throwsBusinessException() {
        when(auth.getName()).thenReturn("driver");
        when(userRepository.findByUsername("driver")).thenReturn(Optional.of(driver));
        when(tripRepository.findByIdWithLock(10L)).thenReturn(Optional.of(trip));

        assertThatThrownBy(() -> bookingService.bookTrip(10L, bookingRequest, auth))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("cannot book");
    }

    @Test
    void bookTrip_whenNotEnoughSeats_throwsBusinessException() {
        trip.setAvailableSeats(1);
        when(auth.getName()).thenReturn("passenger");
        when(userRepository.findByUsername("passenger")).thenReturn(Optional.of(passenger));
        when(tripRepository.findByIdWithLock(10L)).thenReturn(Optional.of(trip));

        assertThatThrownBy(() -> bookingService.bookTrip(10L, bookingRequest, auth))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("seats");
    }

    @Test
    void bookTrip_withAlreadyActiveBooking_throwsBusinessException() {
        when(auth.getName()).thenReturn("passenger");
        when(userRepository.findByUsername("passenger")).thenReturn(Optional.of(passenger));
        when(tripRepository.findByIdWithLock(10L)).thenReturn(Optional.of(trip));
        when(bookingRepository.existsByTripIdAndPassengerIdAndStatusNot(
                10L, 2L, BookingStatus.CANCELLED)).thenReturn(true);

        assertThatThrownBy(() -> bookingService.bookTrip(10L, bookingRequest, auth))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("already have");
    }

    @Test
    void rejectBooking_whenCallerIsNotDriver_throwsUnauthorizedException() {
        Booking booking = Booking.builder()
                .id(1L)
                .trip(trip)
                .passenger(passenger)
                .status(BookingStatus.PENDING)
                .seatsBooked(1)
                .createdAt(OffsetDateTime.now())
                .updatedAt(OffsetDateTime.now())
                .build();

        when(auth.getName()).thenReturn("passenger");
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));
        when(userRepository.findByUsername("passenger")).thenReturn(Optional.of(passenger));

        assertThatThrownBy(() -> bookingService.rejectBooking(1L, auth))
                .isInstanceOf(UnauthorizedException.class);
    }

    @Test
    void approveBooking_decrementsAvailableSeats() {
        Booking booking = Booking.builder()
                .id(1L)
                .trip(trip)
                .passenger(passenger)
                .status(BookingStatus.PENDING)
                .seatsBooked(2)
                .createdAt(OffsetDateTime.now())
                .updatedAt(OffsetDateTime.now())
                .build();

        BookingResponse mockResponse = mock(BookingResponse.class);

        when(auth.getName()).thenReturn("driver");
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));
        when(userRepository.findByUsername("driver")).thenReturn(Optional.of(driver));
        when(tripRepository.findByIdWithLock(10L)).thenReturn(Optional.of(trip));
        when(bookingRepository.save(any())).thenReturn(booking);
        when(bookingMapper.toResponse(any())).thenReturn(mockResponse);

        bookingService.approveBooking(1L, auth);

        verify(tripRepository).save(argThat(t -> t.getAvailableSeats() == 2));
    }
}
