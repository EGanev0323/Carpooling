package bg.tu_sofia.carpooling.rides.service;

import bg.tu_sofia.carpooling.auth.domain.RoleName;
import bg.tu_sofia.carpooling.auth.domain.User;
import bg.tu_sofia.carpooling.auth.repository.UserRepository;
import bg.tu_sofia.carpooling.auth.service.AuditService;
import bg.tu_sofia.carpooling.bookings.repository.BookingRepository;
import bg.tu_sofia.carpooling.cars.domain.Car;
import bg.tu_sofia.carpooling.cars.repository.CarRepository;
import bg.tu_sofia.carpooling.common.exception.BusinessException;
import bg.tu_sofia.carpooling.common.exception.ResourceNotFoundException;
import bg.tu_sofia.carpooling.common.exception.UnauthorizedException;
import bg.tu_sofia.carpooling.geo.domain.City;
import bg.tu_sofia.carpooling.geo.repository.CityRepository;
import bg.tu_sofia.carpooling.rides.api.dto.RideRequest;
import bg.tu_sofia.carpooling.rides.api.dto.RideResponse;
import bg.tu_sofia.carpooling.rides.api.dto.RideResponse.CarSummary;
import bg.tu_sofia.carpooling.rides.api.dto.RideResponse.CitySummary;
import bg.tu_sofia.carpooling.rides.api.dto.RideResponse.DriverSummary;
import bg.tu_sofia.carpooling.rides.api.dto.RideSearchParams;
import bg.tu_sofia.carpooling.rides.domain.Ride;
import bg.tu_sofia.carpooling.rides.domain.RideStop;
import bg.tu_sofia.carpooling.rides.repository.RideRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class RideService {

    private static final double AVERAGE_SPEED_KMH = 60.0;
    private static final double EARTH_RADIUS_KM = 6371.0;

    private final RideRepository rideRepository;
    private final CarRepository carRepository;
    private final CityRepository cityRepository;
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;
    private final AuditService auditService;

    public RideService(RideRepository rideRepository,
                       CarRepository carRepository,
                       CityRepository cityRepository,
                       UserRepository userRepository,
                       BookingRepository bookingRepository,
                       AuditService auditService) {
        this.rideRepository = rideRepository;
        this.carRepository = carRepository;
        this.cityRepository = cityRepository;
        this.userRepository = userRepository;
        this.bookingRepository = bookingRepository;
        this.auditService = auditService;
    }

    @Transactional
    public RideResponse createRide(Long driverId, RideRequest request) {
        User driver = userRepository.findById(driverId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        boolean isDriver = driver.getRoles().stream()
                .anyMatch(r -> r.getName() == RoleName.DRIVER);
        if (!isDriver) {
            throw new BusinessException("Only users with DRIVER role can create rides", 403);
        }

        Car car = carRepository.findById(request.carId())
                .orElseThrow(() -> new ResourceNotFoundException("Car not found: " + request.carId()));

        if (!car.getOwner().getId().equals(driverId)) {
            throw new BusinessException("Car does not belong to the driver", 403);
        }

        if (request.totalSeats() >= car.getSeats()) {
            throw new BusinessException(
                    "totalSeats must be less than car capacity (" + car.getSeats() + ")", 422);
        }

        if (!request.departureAt().isAfter(OffsetDateTime.now(ZoneOffset.UTC))) {
            throw new BusinessException("departureAt must be in the future", 422);
        }

        City originCity = cityRepository.findById(request.originCityId())
                .orElseThrow(() -> new ResourceNotFoundException("Origin city not found: " + request.originCityId()));
        City destinationCity = cityRepository.findById(request.destinationCityId())
                .orElseThrow(() -> new ResourceNotFoundException("Destination city not found: " + request.destinationCityId()));

        String polyline = PolylineEncoder.encode(originCity, destinationCity);
        OffsetDateTime arrivalEstimate = computeArrivalEstimate(originCity, destinationCity, request.departureAt());

        Ride ride = Ride.builder()
                .driver(driver)
                .car(car)
                .originCity(originCity)
                .destinationCity(destinationCity)
                .departureAt(request.departureAt())
                .arrivalAtEstimate(arrivalEstimate)
                .totalSeats((short) request.totalSeats())
                .pricePerSeat(request.pricePerSeat())
                .routePolyline(polyline)
                .description(request.description())
                .status("ACTIVE")
                .build();

        if (request.stops() != null) {
            for (var stopReq : request.stops()) {
                City stopCity = cityRepository.findById(stopReq.cityId())
                        .orElseThrow(() -> new ResourceNotFoundException("Stop city not found: " + stopReq.cityId()));
                RideStop stop = RideStop.builder()
                        .ride(ride)
                        .city(stopCity)
                        .stopOrder((short) stopReq.stopOrder())
                        .arriveAt(stopReq.arriveAt())
                        .build();
                ride.getStops().add(stop);
            }
        }

        Ride saved = rideRepository.save(ride);

        auditService.log(driverId, "RIDE_CREATED", "RIDE", saved.getId(),
                Map.of(
                        "origin", originCity.getNameEn(),
                        "destination", destinationCity.getNameEn(),
                        "departureAt", request.departureAt().toString()
                ));

        return toResponse(saved, 0);
    }

    @Transactional(readOnly = true)
    public Page<RideResponse> searchRides(RideSearchParams params, Pageable pageable) {
        OffsetDateTime dateFrom = null;
        OffsetDateTime dateTo = null;
        if (params.date() != null) {
            dateFrom = params.date().atStartOfDay().atOffset(ZoneOffset.UTC);
            dateTo = params.date().plusDays(1).atStartOfDay().atOffset(ZoneOffset.UTC);
        }

        Page<Ride> page = rideRepository.search(
                params.originCityId(),
                params.destinationCityId(),
                dateFrom,
                dateTo,
                pageable);

        return page.map(ride -> {
            int booked = bookingRepository.sumBookedSeats(ride.getId());
            int available = ride.getTotalSeats() - booked;
            return toResponse(ride, available);
        });
    }

    @Transactional(readOnly = true)
    public RideResponse getRide(UUID publicId, Long requestingUserId) {
        Ride ride = rideRepository.findByPublicId(publicId)
                .orElseThrow(() -> new ResourceNotFoundException("Ride not found: " + publicId));
        int booked = bookingRepository.sumBookedSeats(ride.getId());
        int available = ride.getTotalSeats() - booked;
        return toResponse(ride, available);
    }

    @Transactional
    public void cancelRide(UUID publicId, Long driverId) {
        Ride ride = rideRepository.findByPublicId(publicId)
                .orElseThrow(() -> new ResourceNotFoundException("Ride not found: " + publicId));

        if (!ride.getDriver().getId().equals(driverId)) {
            throw new UnauthorizedException("Only the driver can cancel this ride");
        }

        if (!"ACTIVE".equals(ride.getStatus())) {
            throw new BusinessException("Only ACTIVE rides can be cancelled", 409);
        }

        bookingRepository.cancelAllActiveByRideId(ride.getId());

        ride.setStatus("CANCELLED");
        rideRepository.save(ride);

        auditService.log(driverId, "RIDE_CANCELLED", "RIDE", ride.getId(),
                Map.of(
                        "origin", ride.getOriginCity().getNameEn(),
                        "destination", ride.getDestinationCity().getNameEn()
                ));
    }

    @Transactional(readOnly = true)
    public List<RideResponse> getMyRides(Long driverId) {
        return rideRepository.findByDriverIdOrderByDepartureAtDesc(driverId).stream()
                .map(ride -> {
                    int booked = bookingRepository.sumBookedSeats(ride.getId());
                    int available = ride.getTotalSeats() - booked;
                    return toResponse(ride, available);
                })
                .toList();
    }

    private OffsetDateTime computeArrivalEstimate(City origin, City destination, OffsetDateTime departureAt) {
        double lat1 = origin.getLatitude().doubleValue();
        double lng1 = origin.getLongitude().doubleValue();
        double lat2 = destination.getLatitude().doubleValue();
        double lng2 = destination.getLongitude().doubleValue();

        double distanceKm = haversineDistance(lat1, lng1, lat2, lng2);
        double travelHours = distanceKm / AVERAGE_SPEED_KMH;
        long travelMinutes = Math.round(travelHours * 60);

        return departureAt.plusMinutes(travelMinutes);
    }

    private double haversineDistance(double lat1, double lng1, double lat2, double lng2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLng = Math.toRadians(lng2 - lng1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLng / 2) * Math.sin(dLng / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return EARTH_RADIUS_KM * c;
    }

    private RideResponse toResponse(Ride ride, int availableSeats) {
        User driver = ride.getDriver();
        Car car = ride.getCar();

        DriverSummary driverSummary = new DriverSummary(
                driver.getPublicId(),
                driver.getFirstName(),
                driver.getLastName(),
                driver.getAvatarUrl()
        );

        CarSummary carSummary = new CarSummary(
                car.getMake(),
                car.getModel(),
                car.getYear(),
                car.getColor(),
                car.getSeats(),
                car.getAmenities()
        );

        CitySummary origin = toCitySummary(ride.getOriginCity());
        CitySummary destination = toCitySummary(ride.getDestinationCity());

        return new RideResponse(
                ride.getPublicId(),
                driverSummary,
                carSummary,
                origin,
                destination,
                ride.getDepartureAt(),
                ride.getArrivalAtEstimate(),
                ride.getTotalSeats(),
                availableSeats,
                ride.getPricePerSeat(),
                ride.getRoutePolyline(),
                ride.getDescription(),
                ride.getStatus(),
                ride.getCreatedAt(),
                null  // avgRating populated by ratings feature
        );
    }

    private CitySummary toCitySummary(City city) {
        return new CitySummary(
                city.getId(),
                city.getSlug(),
                city.getNameBg(),
                city.getNameEn(),
                city.getLatitude(),
                city.getLongitude()
        );
    }
}
