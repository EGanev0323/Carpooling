package com.carpooling.service;

import com.carpooling.dto.BookingRequest;
import com.carpooling.dto.BookingResponse;
import com.carpooling.entity.*;
import com.carpooling.exception.BusinessException;
import com.carpooling.exception.ResourceNotFoundException;
import com.carpooling.exception.UnauthorizedException;
import com.carpooling.mapper.BookingMapper;
import com.carpooling.repository.BookingRepository;
import com.carpooling.repository.TripRepository;
import com.carpooling.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BookingService {

    private final BookingRepository bookingRepository;
    private final TripRepository tripRepository;
    private final UserRepository userRepository;
    private final BookingMapper bookingMapper;

    public BookingService(BookingRepository bookingRepository,
                          TripRepository tripRepository,
                          UserRepository userRepository,
                          BookingMapper bookingMapper) {
        this.bookingRepository = bookingRepository;
        this.tripRepository = tripRepository;
        this.userRepository = userRepository;
        this.bookingMapper = bookingMapper;
    }

    @Transactional(readOnly = true)
    public BookingResponse getById(Long id, Authentication auth) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Booking", id));
        User caller = resolveUser(auth);
        // visible to the passenger or the trip driver
        boolean isPassenger = booking.getPassenger().getId().equals(caller.getId());
        boolean isDriver = booking.getTrip().getDriver().getId().equals(caller.getId());
        if (!isPassenger && !isDriver) {
            throw new UnauthorizedException("Access denied");
        }
        return bookingMapper.toResponse(booking);
    }

    @Transactional
    public BookingResponse bookTrip(Long tripId, BookingRequest request, Authentication auth) {
        User passenger = resolveUser(auth);

        // Use pessimistic lock to avoid concurrent overbooking
        Trip trip = tripRepository.findByIdWithLock(tripId)
                .orElseThrow(() -> new ResourceNotFoundException("Trip", tripId));

        if (trip.getStatus() != TripStatus.SCHEDULED) {
            throw new BusinessException("Trip is not available for booking");
        }
        if (trip.getDriver().getId().equals(passenger.getId())) {
            throw new BusinessException("Drivers cannot book their own trip");
        }
        if (trip.getAvailableSeats() < request.seatsBooked()) {
            throw new BusinessException("Not enough available seats");
        }
        if (bookingRepository.existsByTripIdAndPassengerIdAndStatusNot(
                tripId, passenger.getId(), BookingStatus.CANCELLED)) {
            throw new BusinessException("You already have an active booking for this trip");
        }

        Booking booking = Booking.builder()
                .trip(trip)
                .passenger(passenger)
                .seatsBooked(request.seatsBooked())
                .status(BookingStatus.PENDING)
                .message(request.message())
                .build();

        return bookingMapper.toResponse(bookingRepository.save(booking));
    }

    @Transactional
    public BookingResponse approveBooking(Long bookingId, Authentication auth) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking", bookingId));
        User caller = resolveUser(auth);

        if (!booking.getTrip().getDriver().getId().equals(caller.getId())) {
            throw new UnauthorizedException("Only the trip driver can approve bookings");
        }
        if (booking.getStatus() != BookingStatus.PENDING) {
            throw new BusinessException("Only PENDING bookings can be approved");
        }

        // Pessimistic lock on trip before modifying availableSeats
        Trip trip = tripRepository.findByIdWithLock(booking.getTrip().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Trip", booking.getTrip().getId()));

        if (trip.getAvailableSeats() < booking.getSeatsBooked()) {
            throw new BusinessException("Not enough available seats to approve this booking");
        }

        trip.setAvailableSeats(trip.getAvailableSeats() - booking.getSeatsBooked());
        tripRepository.save(trip);

        booking.setStatus(BookingStatus.APPROVED);
        return bookingMapper.toResponse(bookingRepository.save(booking));
    }

    @Transactional
    public BookingResponse rejectBooking(Long bookingId, Authentication auth) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking", bookingId));
        User caller = resolveUser(auth);

        if (!booking.getTrip().getDriver().getId().equals(caller.getId())) {
            throw new UnauthorizedException("Only the trip driver can reject bookings");
        }
        if (booking.getStatus() != BookingStatus.PENDING) {
            throw new BusinessException("Only PENDING bookings can be rejected");
        }

        booking.setStatus(BookingStatus.REJECTED);
        return bookingMapper.toResponse(bookingRepository.save(booking));
    }

    @Transactional
    public BookingResponse cancelBooking(Long bookingId, Authentication auth) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking", bookingId));
        User caller = resolveUser(auth);

        if (!booking.getPassenger().getId().equals(caller.getId())) {
            throw new UnauthorizedException("Only the passenger can cancel their booking");
        }
        if (booking.getStatus() == BookingStatus.CANCELLED ||
            booking.getStatus() == BookingStatus.REJECTED) {
            throw new BusinessException("Booking is already " + booking.getStatus());
        }

        boolean wasApproved = booking.getStatus() == BookingStatus.APPROVED;
        booking.setStatus(BookingStatus.CANCELLED);
        bookingRepository.save(booking);

        if (wasApproved) {
            Trip trip = tripRepository.findByIdWithLock(booking.getTrip().getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Trip", booking.getTrip().getId()));
            trip.setAvailableSeats(trip.getAvailableSeats() + booking.getSeatsBooked());
            tripRepository.save(trip);
        }

        return bookingMapper.toResponse(booking);
    }

    private User resolveUser(Authentication auth) {
        return userRepository.findByUsername(auth.getName())
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + auth.getName()));
    }
}
