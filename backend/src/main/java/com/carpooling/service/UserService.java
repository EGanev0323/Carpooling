package com.carpooling.service;

import com.carpooling.dto.*;
import com.carpooling.entity.User;
import com.carpooling.exception.BusinessException;
import com.carpooling.exception.ResourceNotFoundException;
import com.carpooling.exception.UnauthorizedException;
import com.carpooling.mapper.BookingMapper;
import com.carpooling.mapper.RatingMapper;
import com.carpooling.mapper.TripMapper;
import com.carpooling.mapper.UserMapper;
import com.carpooling.repository.BookingRepository;
import com.carpooling.repository.RatingRepository;
import com.carpooling.repository.TripRepository;
import com.carpooling.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final TripRepository tripRepository;
    private final BookingRepository bookingRepository;
    private final RatingRepository ratingRepository;
    private final UserMapper userMapper;
    private final TripMapper tripMapper;
    private final BookingMapper bookingMapper;
    private final RatingMapper ratingMapper;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository,
                       TripRepository tripRepository,
                       BookingRepository bookingRepository,
                       RatingRepository ratingRepository,
                       UserMapper userMapper,
                       TripMapper tripMapper,
                       BookingMapper bookingMapper,
                       RatingMapper ratingMapper,
                       PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.tripRepository = tripRepository;
        this.bookingRepository = bookingRepository;
        this.ratingRepository = ratingRepository;
        this.userMapper = userMapper;
        this.tripMapper = tripMapper;
        this.bookingMapper = bookingMapper;
        this.ratingMapper = ratingMapper;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public UserResponse findById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", id));
        return userMapper.toResponse(user);
    }

    @Transactional
    public UserResponse updateProfile(Long id, UpdateProfileRequest request, Authentication auth) {
        User target = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", id));
        User caller = resolveUser(auth);

        if (!caller.getId().equals(target.getId())) {
            throw new UnauthorizedException("You can only update your own profile");
        }

        if (request.firstName() != null) target.setFirstName(request.firstName());
        if (request.lastName() != null) target.setLastName(request.lastName());
        if (request.phone() != null) target.setPhone(request.phone());
        if (request.avatarUrl() != null) target.setAvatarUrl(request.avatarUrl());
        if (request.isDriver() != null) target.setIsDriver(request.isDriver());
        if (request.isPassenger() != null) target.setIsPassenger(request.isPassenger());

        return userMapper.toResponse(userRepository.save(target));
    }

    @Transactional
    public void changePassword(Long id, ChangePasswordRequest request, Authentication auth) {
        User target = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", id));
        User caller = resolveUser(auth);

        if (!caller.getId().equals(target.getId())) {
            throw new UnauthorizedException("You can only change your own password");
        }
        if (!passwordEncoder.matches(request.currentPassword(), target.getPasswordHash())) {
            throw new BusinessException("Current password is incorrect");
        }

        target.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        userRepository.save(target);
    }

    @Transactional(readOnly = true)
    public List<RatingResponse> getUserRatings(Long userId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));
        return ratingRepository.findByRatedIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(ratingMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<TripSummaryResponse> getUserTrips(Long userId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));
        return tripRepository.findByDriverIdOrderByDepartureTimeDesc(userId)
                .stream()
                .map(tripMapper::toSummary)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<BookingResponse> getUserBookings(Long userId, Authentication auth) {
        User caller = resolveUser(auth);
        if (!caller.getId().equals(userId)) {
            throw new UnauthorizedException("You can only view your own bookings");
        }
        userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));
        return bookingRepository.findByPassengerIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(bookingMapper::toResponse)
                .toList();
    }

    private User resolveUser(Authentication auth) {
        return userRepository.findByUsername(auth.getName())
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + auth.getName()));
    }
}
