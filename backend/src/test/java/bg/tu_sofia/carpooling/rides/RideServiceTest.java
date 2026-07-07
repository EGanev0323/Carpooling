package bg.tu_sofia.carpooling.rides;

import bg.tu_sofia.carpooling.auth.domain.Role;
import bg.tu_sofia.carpooling.auth.domain.RoleName;
import bg.tu_sofia.carpooling.auth.domain.User;
import bg.tu_sofia.carpooling.auth.repository.UserRepository;
import bg.tu_sofia.carpooling.auth.service.AuditService;
import bg.tu_sofia.carpooling.bookings.repository.BookingRepository;
import bg.tu_sofia.carpooling.cars.domain.Car;
import bg.tu_sofia.carpooling.cars.repository.CarRepository;
import bg.tu_sofia.carpooling.common.exception.BusinessException;
import bg.tu_sofia.carpooling.geo.domain.City;
import bg.tu_sofia.carpooling.geo.repository.CityRepository;
import bg.tu_sofia.carpooling.rides.api.dto.RideRequest;
import bg.tu_sofia.carpooling.rides.api.dto.RideResponse;
import bg.tu_sofia.carpooling.rides.domain.Ride;
import bg.tu_sofia.carpooling.rides.repository.RideRepository;
import bg.tu_sofia.carpooling.rides.service.PolylineEncoder;
import bg.tu_sofia.carpooling.rides.service.RideService;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RideServiceTest {

    @Mock private RideRepository rideRepository;
    @Mock private CarRepository carRepository;
    @Mock private CityRepository cityRepository;
    @Mock private UserRepository userRepository;
    @Mock private BookingRepository bookingRepository;
    @Mock private AuditService auditService;

    @InjectMocks
    private RideService rideService;

    private User driver;
    private Car car;
    private City sofia;
    private City plovdiv;

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

        sofia = City.builder()
                .id(1)
                .slug("sofia")
                .nameBg("София")
                .nameEn("Sofia")
                .latitude(new BigDecimal("42.697500"))
                .longitude(new BigDecimal("23.324200"))
                .isActive(true)
                .build();

        plovdiv = City.builder()
                .id(2)
                .slug("plovdiv")
                .nameBg("Пловдив")
                .nameEn("Plovdiv")
                .latitude(new BigDecimal("42.135400"))
                .longitude(new BigDecimal("24.745300"))
                .isActive(true)
                .build();
    }

    @Test
    void shouldCreateRideSuccessfully() {
        RideRequest request = new RideRequest(
                1, 2,
                OffsetDateTime.now(ZoneOffset.UTC).plusDays(3),
                10L,
                3,
                new BigDecimal("15.00"),
                "Direct route",
                List.of()
        );

        Ride savedRide = Ride.builder()
                .id(100L)
                .publicId(UUID.randomUUID())
                .driver(driver)
                .car(car)
                .originCity(sofia)
                .destinationCity(plovdiv)
                .departureAt(request.departureAt())
                .totalSeats((short) 3)
                .pricePerSeat(new BigDecimal("15.00"))
                .routePolyline(PolylineEncoder.encode(sofia, plovdiv))
                .status("ACTIVE")
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(driver));
        when(carRepository.findById(10L)).thenReturn(Optional.of(car));
        when(cityRepository.findById(1)).thenReturn(Optional.of(sofia));
        when(cityRepository.findById(2)).thenReturn(Optional.of(plovdiv));
        when(rideRepository.save(any(Ride.class))).thenReturn(savedRide);
        doNothing().when(auditService).log(anyLong(), any(), any(), anyLong(), any());

        RideResponse response = rideService.createRide(1L, request);

        assertThat(response).isNotNull();
        assertThat(response.status()).isEqualTo("ACTIVE");
        assertThat(response.totalSeats()).isEqualTo(3);
        assertThat(response.routePolyline()).isNotBlank();
        verify(rideRepository).save(any(Ride.class));
        verify(auditService).log(eq(1L), eq("RIDE_CREATED"), eq("RIDE"), eq(100L), any());
    }

    @Test
    void shouldRejectRideWhenUserIsNotDriver() {
        Role userRole = new Role((short) 1, RoleName.USER);
        User nonDriver = User.builder()
                .id(3L)
                .publicId(UUID.randomUUID())
                .email("user@example.com")
                .passwordHash("hash")
                .firstName("Elena")
                .lastName("Kostadinova")
                .phone("+359888333444")
                .status("ACTIVE")
                .roles(Set.of(userRole))
                .build();

        RideRequest request = new RideRequest(
                1, 2,
                OffsetDateTime.now(ZoneOffset.UTC).plusDays(3),
                10L, 3, new BigDecimal("15.00"), null, null
        );

        when(userRepository.findById(3L)).thenReturn(Optional.of(nonDriver));

        assertThatThrownBy(() -> rideService.createRide(3L, request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("DRIVER");
    }

    @Test
    void shouldRejectRideWhenCarDoesNotBelongToDriver() {
        User otherDriver = User.builder()
                .id(99L)
                .publicId(UUID.randomUUID())
                .email("other@example.com")
                .passwordHash("hash")
                .firstName("Other")
                .lastName("Driver")
                .phone("+359888999000")
                .status("ACTIVE")
                .roles(Set.of(new Role((short) 2, RoleName.DRIVER)))
                .build();

        Car otherCar = Car.builder()
                .id(10L)
                .owner(otherDriver) // belongs to someone else
                .make("BMW")
                .model("320")
                .year((short) 2021)
                .seats((short) 5)
                .licensePlate("XX1234XX")
                .amenities("{}")
                .build();

        RideRequest request = new RideRequest(
                1, 2,
                OffsetDateTime.now(ZoneOffset.UTC).plusDays(3),
                10L, 3, new BigDecimal("15.00"), null, null
        );

        when(userRepository.findById(1L)).thenReturn(Optional.of(driver));
        when(carRepository.findById(10L)).thenReturn(Optional.of(otherCar));

        assertThatThrownBy(() -> rideService.createRide(1L, request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("does not belong");
    }

    @Test
    void shouldRejectRideWhenTotalSeatsExceedsCarCapacity() {
        RideRequest request = new RideRequest(
                1, 2,
                OffsetDateTime.now(ZoneOffset.UTC).plusDays(3),
                10L,
                5, // car has 5 seats, totalSeats must be < 5
                new BigDecimal("15.00"), null, null
        );

        when(userRepository.findById(1L)).thenReturn(Optional.of(driver));
        when(carRepository.findById(10L)).thenReturn(Optional.of(car));

        assertThatThrownBy(() -> rideService.createRide(1L, request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("car capacity");
    }

    @Test
    void shouldGenerateCorrectPolylineForSofiaToPlodiv() {
        String polyline = PolylineEncoder.encode(sofia, plovdiv);

        assertThat(polyline).isNotBlank();
        // Encoded polyline for Sofia (42.697500, 23.324200) -> Plovdiv (42.135400, 24.745300)
        // Verify it's a non-trivial encoded string
        assertThat(polyline.length()).isGreaterThan(5);
    }

    @Test
    void shouldRejectRideWithDepartureInPast() {
        RideRequest request = new RideRequest(
                1, 2,
                OffsetDateTime.now(ZoneOffset.UTC).minusHours(1), // past
                10L, 3, new BigDecimal("15.00"), null, null
        );

        when(userRepository.findById(1L)).thenReturn(Optional.of(driver));
        when(carRepository.findById(10L)).thenReturn(Optional.of(car));

        assertThatThrownBy(() -> rideService.createRide(1L, request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("future");
    }
}
