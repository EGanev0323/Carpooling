package bg.tu_sofia.carpooling.bookings;

import bg.tu_sofia.carpooling.auth.domain.Role;
import bg.tu_sofia.carpooling.auth.domain.RoleName;
import bg.tu_sofia.carpooling.auth.domain.User;
import bg.tu_sofia.carpooling.auth.repository.UserRepository;
import bg.tu_sofia.carpooling.auth.service.AuditService;
import bg.tu_sofia.carpooling.bookings.api.dto.BookingRequest;
import bg.tu_sofia.carpooling.bookings.api.dto.BookingResponse;
import bg.tu_sofia.carpooling.bookings.domain.Booking;
import bg.tu_sofia.carpooling.bookings.repository.BookingRepository;
import bg.tu_sofia.carpooling.bookings.service.BookingService;
import bg.tu_sofia.carpooling.cars.domain.Car;
import bg.tu_sofia.carpooling.common.exception.BusinessException;
import bg.tu_sofia.carpooling.common.exception.UnauthorizedException;
import bg.tu_sofia.carpooling.geo.domain.City;
import bg.tu_sofia.carpooling.rides.domain.Ride;
import bg.tu_sofia.carpooling.rides.repository.RideRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookingServiceTest {

    @Mock private RideRepository rideRepository;
    @Mock private BookingRepository bookingRepository;
    @Mock private UserRepository userRepository;
    @Mock private AuditService auditService;

    @InjectMocks
    private BookingService bookingService;

    private User driver;
    private User passenger;
    private Ride ride;
    private City sofia;
    private City plovdiv;
    private Car car;

    @BeforeEach
    void setUp() {
        Role driverRole = new Role((short) 2, RoleName.DRIVER);
        Role userRole = new Role((short) 1, RoleName.USER);

        driver = User.builder()
                .id(1L)
                .publicId(UUID.randomUUID())
                .email("driver@example.com")
                .passwordHash("hash")
                .firstName("Ivan")
                .lastName("Petrov")
                .phone("+359888111222")
                .status("ACTIVE")
                .roles(Set.of(userRole, driverRole))
                .build();

        passenger = User.builder()
                .id(2L)
                .publicId(UUID.randomUUID())
                .email("passenger@example.com")
                .passwordHash("hash")
                .firstName("Georgi")
                .lastName("Ivanov")
                .phone("+359888222333")
                .status("ACTIVE")
                .roles(Set.of(userRole))
                .build();

        sofia = City.builder()
                .id(1)
                .slug("sofia")
                .nameBg("София")
                .nameEn("Sofia")
                .latitude(new BigDecimal("42.6975"))
                .longitude(new BigDecimal("23.3242"))
                .isActive(true)
                .build();

        plovdiv = City.builder()
                .id(2)
                .slug("plovdiv")
                .nameBg("Пловдив")
                .nameEn("Plovdiv")
                .latitude(new BigDecimal("42.1354"))
                .longitude(new BigDecimal("24.7453"))
                .isActive(true)
                .build();

        car = Car.builder()
                .id(10L)
                .owner(driver)
                .make("Toyota")
                .model("Corolla")
                .year((short) 2020)
                .seats((short) 5)
                .licensePlate("СА1234АА")
                .amenities("{}")
                .build();

        ride = Ride.builder()
                .id(100L)
                .publicId(UUID.randomUUID())
                .driver(driver)
                .car(car)
                .originCity(sofia)
                .destinationCity(plovdiv)
                .departureAt(OffsetDateTime.now(ZoneOffset.UTC).plusDays(3))
                .totalSeats((short) 3)
                .pricePerSeat(new BigDecimal("15.00"))
                .status("ACTIVE")
                .build();
    }

    @Test
    void shouldCreateBookingWhenSeatsAvailable() {
        UUID ridePublicId = ride.getPublicId();
        BookingRequest request = new BookingRequest(ridePublicId, 1, "Please confirm", null);

        when(userRepository.findById(2L)).thenReturn(Optional.of(passenger));
        when(rideRepository.findByPublicId(ridePublicId)).thenReturn(Optional.of(ride));
        when(bookingRepository.existsByRideIdAndPassengerIdAndStatusIn(100L, 2L, List.of("PENDING", "CONFIRMED")))
                .thenReturn(false);
        when(rideRepository.findByIdForUpdate(100L)).thenReturn(Optional.of(ride));
        when(bookingRepository.sumBookedSeats(100L)).thenReturn(1);

        Booking savedBooking = Booking.builder()
                .id(1L)
                .publicId(UUID.randomUUID())
                .ride(ride)
                .passenger(passenger)
                .seats((short) 1)
                .status("PENDING")
                .build();
        when(bookingRepository.save(any(Booking.class))).thenReturn(savedBooking);
        doNothing().when(auditService).log(anyLong(), any(), any(), anyLong(), any());

        BookingResponse response = bookingService.createBooking(2L, request);

        assertThat(response).isNotNull();
        assertThat(response.status()).isEqualTo("PENDING");
        verify(bookingRepository).save(any(Booking.class));
    }

    @Test
    void shouldRejectWhenNoSeatsAvailable() {
        UUID ridePublicId = ride.getPublicId();
        BookingRequest request = new BookingRequest(ridePublicId, 3, null, null);

        when(userRepository.findById(2L)).thenReturn(Optional.of(passenger));
        when(rideRepository.findByPublicId(ridePublicId)).thenReturn(Optional.of(ride));
        when(bookingRepository.existsByRideIdAndPassengerIdAndStatusIn(100L, 2L, List.of("PENDING", "CONFIRMED")))
                .thenReturn(false);
        when(rideRepository.findByIdForUpdate(100L)).thenReturn(Optional.of(ride));
        when(bookingRepository.sumBookedSeats(100L)).thenReturn(2); // only 1 available

        assertThatThrownBy(() -> bookingService.createBooking(2L, request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Not enough seats");
    }

    @Test
    void shouldRejectWhenPassengerIsDriver() {
        UUID ridePublicId = ride.getPublicId();
        BookingRequest request = new BookingRequest(ridePublicId, 1, null, null);

        when(userRepository.findById(1L)).thenReturn(Optional.of(driver));
        when(rideRepository.findByPublicId(ridePublicId)).thenReturn(Optional.of(ride));

        assertThatThrownBy(() -> bookingService.createBooking(1L, request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("cannot book");
    }

    @Test
    void shouldRejectDuplicateBooking() {
        UUID ridePublicId = ride.getPublicId();
        BookingRequest request = new BookingRequest(ridePublicId, 1, null, null);

        when(userRepository.findById(2L)).thenReturn(Optional.of(passenger));
        when(rideRepository.findByPublicId(ridePublicId)).thenReturn(Optional.of(ride));
        when(bookingRepository.existsByRideIdAndPassengerIdAndStatusIn(100L, 2L, List.of("PENDING", "CONFIRMED")))
                .thenReturn(true);

        assertThatThrownBy(() -> bookingService.createBooking(2L, request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("already have");
    }

    @Test
    void shouldConfirmPendingBooking() {
        UUID bookingPublicId = UUID.randomUUID();
        Booking booking = Booking.builder()
                .id(1L)
                .publicId(bookingPublicId)
                .ride(ride)
                .passenger(passenger)
                .seats((short) 1)
                .status("PENDING")
                .build();

        when(bookingRepository.findByPublicId(bookingPublicId)).thenReturn(Optional.of(booking));

        Booking confirmed = Booking.builder()
                .id(1L)
                .publicId(bookingPublicId)
                .ride(ride)
                .passenger(passenger)
                .seats((short) 1)
                .status("CONFIRMED")
                .build();
        when(bookingRepository.save(any(Booking.class))).thenReturn(confirmed);
        doNothing().when(auditService).log(anyLong(), any(), any(), anyLong(), any());

        BookingResponse response = bookingService.confirmBooking(bookingPublicId, 1L);

        assertThat(response.status()).isEqualTo("CONFIRMED");
        verify(bookingRepository).save(any(Booking.class));
    }

    @Test
    void shouldRejectConfirmedBooking_whenNotDriver() {
        UUID bookingPublicId = UUID.randomUUID();
        Booking booking = Booking.builder()
                .id(1L)
                .publicId(bookingPublicId)
                .ride(ride)
                .passenger(passenger)
                .seats((short) 1)
                .status("PENDING")
                .build();

        when(bookingRepository.findByPublicId(bookingPublicId)).thenReturn(Optional.of(booking));

        // userId=2 is the passenger, not the driver (driver is id=1)
        assertThatThrownBy(() -> bookingService.confirmBooking(bookingPublicId, 2L))
                .isInstanceOf(UnauthorizedException.class);
    }

    @Test
    void shouldCancelBookingByPassenger() {
        UUID bookingPublicId = UUID.randomUUID();
        Booking booking = Booking.builder()
                .id(1L)
                .publicId(bookingPublicId)
                .ride(ride)
                .passenger(passenger)
                .seats((short) 1)
                .status("CONFIRMED")
                .build();

        when(bookingRepository.findByPublicId(bookingPublicId)).thenReturn(Optional.of(booking));

        Booking cancelled = Booking.builder()
                .id(1L)
                .publicId(bookingPublicId)
                .ride(ride)
                .passenger(passenger)
                .seats((short) 1)
                .status("CANCELLED")
                .build();
        when(bookingRepository.save(any(Booking.class))).thenReturn(cancelled);
        doNothing().when(auditService).log(anyLong(), any(), any(), anyLong(), any());

        BookingResponse response = bookingService.cancelBooking(bookingPublicId, 2L);

        assertThat(response.status()).isEqualTo("CANCELLED");
    }
}
