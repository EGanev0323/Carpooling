package bg.tu_sofia.carpooling.seed;

import bg.tu_sofia.carpooling.auth.domain.Role;
import bg.tu_sofia.carpooling.auth.domain.RoleName;
import bg.tu_sofia.carpooling.auth.domain.User;
import bg.tu_sofia.carpooling.auth.repository.RoleRepository;
import bg.tu_sofia.carpooling.auth.repository.UserRepository;
import bg.tu_sofia.carpooling.bookings.domain.Booking;
import bg.tu_sofia.carpooling.bookings.repository.BookingRepository;
import bg.tu_sofia.carpooling.cars.domain.Car;
import bg.tu_sofia.carpooling.cars.repository.CarRepository;
import bg.tu_sofia.carpooling.geo.domain.City;
import bg.tu_sofia.carpooling.geo.repository.CityRepository;
import bg.tu_sofia.carpooling.rides.domain.Ride;
import bg.tu_sofia.carpooling.rides.repository.RideRepository;
import bg.tu_sofia.carpooling.rides.service.PolylineEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.HashSet;
import java.util.Set;

@Component
@ConditionalOnProperty(name = "app.seed.enabled", havingValue = "true")
@Order(1)
public class DataSeeder implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);
    private static final String DEFAULT_PASSWORD = "Parola123!";
    private static final String AMENITIES_CAR1 = "{\"ac\":true,\"pets\":false,\"smoke\":false,\"luggage\":\"medium\"}";
    private static final String AMENITIES_CAR2 = "{\"ac\":true,\"pets\":true,\"smoke\":false,\"luggage\":\"small\"}";

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final CityRepository cityRepository;
    private final CarRepository carRepository;
    private final RideRepository rideRepository;
    private final BookingRepository bookingRepository;
    private final PasswordEncoder passwordEncoder;

    public DataSeeder(UserRepository userRepository,
                      RoleRepository roleRepository,
                      CityRepository cityRepository,
                      CarRepository carRepository,
                      RideRepository rideRepository,
                      BookingRepository bookingRepository,
                      PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.cityRepository = cityRepository;
        this.carRepository = carRepository;
        this.rideRepository = rideRepository;
        this.bookingRepository = bookingRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (userRepository.count() > 1) {
            log.info("Seed data already present — skipping.");
            return;
        }

        log.info("Seeding development data...");

        Role userRole = roleRepository.findByName(RoleName.USER)
                .orElseThrow(() -> new IllegalStateException("USER role not found in DB"));
        Role driverRole = roleRepository.findByName(RoleName.DRIVER)
                .orElseThrow(() -> new IllegalStateException("DRIVER role not found in DB"));

        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);

        User driver1 = createUser("ivan.petrov@example.com", "Иван", "Петров",
                "+359888111222", Set.of(userRole, driverRole), now);
        User driver2 = createUser("maria.georgieva@example.com", "Мария", "Георгиева",
                "+359877333444", Set.of(userRole, driverRole), now);
        User passenger1 = createUser("georgi.ivanov@example.com", "Георги", "Иванов",
                "+359899555666", Set.of(userRole), now);
        User passenger2 = createUser("elena.kostadinova@example.com", "Елена", "Костадинова",
                "+359888777888", Set.of(userRole), now);
        User passenger3 = createUser("petar.nikolov@example.com", "Петър", "Николов",
                "+359877999000", Set.of(userRole), now);

        Car car1 = createCar(driver1, "Toyota", "Corolla", (short) 2020, "Бял",
                "СА1234АА", (short) 5, AMENITIES_CAR1);
        Car car2 = createCar(driver2, "Volkswagen", "Golf", (short) 2019, "Сив",
                "ПВ5678ВВ", (short) 4, AMENITIES_CAR2);

        City sofia = cityRepository.findBySlug("sofia")
                .orElseThrow(() -> new IllegalStateException("City 'sofia' not found"));
        City plovdiv = cityRepository.findBySlug("plovdiv")
                .orElseThrow(() -> new IllegalStateException("City 'plovdiv' not found"));
        City varna = cityRepository.findBySlug("varna")
                .orElseThrow(() -> new IllegalStateException("City 'varna' not found"));
        City burgas = cityRepository.findBySlug("burgas")
                .orElseThrow(() -> new IllegalStateException("City 'burgas' not found"));
        City ruse = cityRepository.findBySlug("ruse")
                .orElseThrow(() -> new IllegalStateException("City 'ruse' not found"));
        City staraZagora = cityRepository.findBySlug("stara-zagora")
                .orElseThrow(() -> new IllegalStateException("City 'stara-zagora' not found"));

        // 8 rides with departure_at 3-14 days in the future
        Ride ride1 = createRide(driver1, car1, sofia, plovdiv, (short) 3, new BigDecimal("15.00"), now.plusDays(3));
        Ride ride2 = createRide(driver2, car2, sofia, varna, (short) 2, new BigDecimal("40.00"), now.plusDays(5));
        Ride ride3 = createRide(driver1, car1, plovdiv, sofia, (short) 4, new BigDecimal("15.00"), now.plusDays(6));
        Ride ride4 = createRide(driver2, car2, sofia, burgas, (short) 3, new BigDecimal("35.00"), now.plusDays(7));
        createRide(driver1, car1, varna, sofia, (short) 3, new BigDecimal("38.00"), now.plusDays(8));
        createRide(driver2, car2, sofia, staraZagora, (short) 2, new BigDecimal("25.00"), now.plusDays(10));
        createRide(driver1, car1, plovdiv, varna, (short) 4, new BigDecimal("30.00"), now.plusDays(12));
        createRide(driver2, car2, sofia, ruse, (short) 2, new BigDecimal("28.00"), now.plusDays(14));

        // 4 bookings
        createBooking(passenger1, ride1, (short) 1, "CONFIRMED");
        createBooking(passenger2, ride1, (short) 1, "PENDING");
        createBooking(passenger1, ride2, (short) 1, "CONFIRMED");
        createBooking(passenger3, ride4, (short) 1, "PENDING");

        log.info("Seed data created successfully.");
    }

    private User createUser(String email, String firstName, String lastName,
                            String phone, Set<Role> roles, OffsetDateTime now) {
        User user = User.builder()
                .email(email)
                .passwordHash(passwordEncoder.encode(DEFAULT_PASSWORD))
                .firstName(firstName)
                .lastName(lastName)
                .phone(phone)
                .status("ACTIVE")
                .emailVerifiedAt(now)
                .roles(new HashSet<>(roles))
                .build();
        return userRepository.save(user);
    }

    private Car createCar(User owner, String make, String model, short year,
                          String color, String licensePlate, short seats, String amenities) {
        Car car = Car.builder()
                .owner(owner)
                .make(make)
                .model(model)
                .year(year)
                .color(color)
                .licensePlate(licensePlate)
                .seats(seats)
                .amenities(amenities)
                .build();
        return carRepository.save(car);
    }

    private Ride createRide(User driver, Car car, City origin, City destination,
                            short totalSeats, BigDecimal pricePerSeat, OffsetDateTime departureAt) {
        String polyline = PolylineEncoder.encode(origin, destination);

        double lat1 = origin.getLatitude().doubleValue();
        double lng1 = origin.getLongitude().doubleValue();
        double lat2 = destination.getLatitude().doubleValue();
        double lng2 = destination.getLongitude().doubleValue();
        double distanceKm = haversineDistance(lat1, lng1, lat2, lng2);
        long travelMinutes = Math.round((distanceKm / 60.0) * 60);
        OffsetDateTime arrivalEstimate = departureAt.plusMinutes(travelMinutes);

        Ride ride = Ride.builder()
                .driver(driver)
                .car(car)
                .originCity(origin)
                .destinationCity(destination)
                .departureAt(departureAt)
                .arrivalAtEstimate(arrivalEstimate)
                .totalSeats(totalSeats)
                .pricePerSeat(pricePerSeat)
                .routePolyline(polyline)
                .status("ACTIVE")
                .build();
        return rideRepository.save(ride);
    }

    private void createBooking(User passenger, Ride ride, short seats, String status) {
        Booking booking = Booking.builder()
                .ride(ride)
                .passenger(passenger)
                .seats(seats)
                .status(status)
                .build();
        bookingRepository.save(booking);
    }

    private double haversineDistance(double lat1, double lng1, double lat2, double lng2) {
        final double earthRadiusKm = 6371.0;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLng = Math.toRadians(lng2 - lng1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLng / 2) * Math.sin(dLng / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return earthRadiusKm * c;
    }
}
