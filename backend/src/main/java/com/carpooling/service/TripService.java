package com.carpooling.service;

import com.carpooling.dto.BookingResponse;
import com.carpooling.dto.TripRequest;
import com.carpooling.dto.TripResponse;
import com.carpooling.dto.TripSummaryResponse;
import com.carpooling.entity.*;
import com.carpooling.exception.BusinessException;
import com.carpooling.exception.ResourceNotFoundException;
import com.carpooling.exception.UnauthorizedException;
import com.carpooling.mapper.BookingMapper;
import com.carpooling.mapper.TripMapper;
import com.carpooling.repository.BookingRepository;
import com.carpooling.repository.CarRepository;
import com.carpooling.repository.TripRepository;
import com.carpooling.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class TripService {

    private final TripRepository tripRepository;
    private final UserRepository userRepository;
    private final CarRepository carRepository;
    private final BookingRepository bookingRepository;
    private final TripMapper tripMapper;
    private final BookingMapper bookingMapper;

    public TripService(TripRepository tripRepository,
                       UserRepository userRepository,
                       CarRepository carRepository,
                       BookingRepository bookingRepository,
                       TripMapper tripMapper,
                       BookingMapper bookingMapper) {
        this.tripRepository = tripRepository;
        this.userRepository = userRepository;
        this.carRepository = carRepository;
        this.bookingRepository = bookingRepository;
        this.tripMapper = tripMapper;
        this.bookingMapper = bookingMapper;
    }

    @Transactional(readOnly = true)
    public Page<TripSummaryResponse> searchTrips(String origin,
                                                  String destination,
                                                  LocalDate date,
                                                  Integer seats,
                                                  Pageable pageable) {
        return tripRepository.searchTrips(origin, destination, date, seats, pageable)
                .map(tripMapper::toSummary);
    }

    @Transactional
    public TripResponse createTrip(TripRequest request, Authentication auth) {
        User driver = resolveUser(auth);

        if (!Boolean.TRUE.equals(driver.getIsDriver())) {
            throw new BusinessException("Only drivers can create trips");
        }

        Car car = carRepository.findById(request.carId())
                .orElseThrow(() -> new ResourceNotFoundException("Car", request.carId()));

        if (!car.getOwner().getId().equals(driver.getId())) {
            throw new UnauthorizedException("You do not own this car");
        }
        if (!Boolean.TRUE.equals(car.getIsActive())) {
            throw new BusinessException("Car is not active");
        }

        Trip trip = tripMapper.toEntity(request);
        trip.setDriver(driver);
        trip.setCar(car);
        trip.setStatus(TripStatus.SCHEDULED);
        trip.setAvailableSeats(request.totalSeats());

        return tripMapper.toResponse(tripRepository.save(trip));
    }

    @Transactional(readOnly = true)
    public TripResponse getTripById(Long id) {
        Trip trip = tripRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Trip", id));
        return tripMapper.toResponse(trip);
    }

    @Transactional
    public TripResponse updateTrip(Long id, TripRequest request, Authentication auth) {
        Trip trip = tripRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Trip", id));
        User caller = resolveUser(auth);

        if (!trip.getDriver().getId().equals(caller.getId())) {
            throw new UnauthorizedException("You are not the driver of this trip");
        }
        if (trip.getStatus() != TripStatus.SCHEDULED) {
            throw new BusinessException("Only SCHEDULED trips can be updated");
        }

        Car car = carRepository.findById(request.carId())
                .orElseThrow(() -> new ResourceNotFoundException("Car", request.carId()));
        if (!car.getOwner().getId().equals(caller.getId())) {
            throw new UnauthorizedException("You do not own this car");
        }

        trip.setOriginCity(request.originCity());
        trip.setOriginAddress(request.originAddress());
        trip.setDestinationCity(request.destinationCity());
        trip.setDestinationAddress(request.destinationAddress());
        trip.setDepartureTime(request.departureTime());
        trip.setEstimatedArrival(request.estimatedArrival());
        trip.setPricePerSeat(request.pricePerSeat());
        trip.setTotalSeats(request.totalSeats());
        trip.setAvailableSeats(request.totalSeats());
        trip.setCar(car);
        trip.setDescription(request.description());
        if (request.smokingAllowed() != null) trip.setSmokingAllowed(request.smokingAllowed());
        if (request.petsAllowed() != null) trip.setPetsAllowed(request.petsAllowed());

        return tripMapper.toResponse(tripRepository.save(trip));
    }

    @Transactional
    public void cancelTrip(Long id, Authentication auth) {
        Trip trip = tripRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Trip", id));
        User caller = resolveUser(auth);

        if (!trip.getDriver().getId().equals(caller.getId())) {
            throw new UnauthorizedException("You are not the driver of this trip");
        }
        if (trip.getStatus() == TripStatus.COMPLETED || trip.getStatus() == TripStatus.CANCELLED) {
            throw new BusinessException("Trip is already " + trip.getStatus());
        }

        trip.setStatus(TripStatus.CANCELLED);

        // Reject all pending bookings
        bookingRepository.findByTripId(id).forEach(booking -> {
            if (booking.getStatus() == BookingStatus.PENDING) {
                booking.setStatus(BookingStatus.REJECTED);
                bookingRepository.save(booking);
            }
        });

        tripRepository.save(trip);
    }

    @Transactional(readOnly = true)
    public List<BookingResponse> getTripBookings(Long tripId, Authentication auth) {
        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new ResourceNotFoundException("Trip", tripId));
        User caller = resolveUser(auth);

        if (!trip.getDriver().getId().equals(caller.getId())) {
            throw new UnauthorizedException("Only the trip driver can view bookings");
        }

        return bookingRepository.findByTripId(tripId)
                .stream()
                .map(bookingMapper::toResponse)
                .toList();
    }

    private User resolveUser(Authentication auth) {
        return userRepository.findByUsername(auth.getName())
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + auth.getName()));
    }
}
