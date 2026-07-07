package com.carpooling.service;

import com.carpooling.dto.RatingRequest;
import com.carpooling.dto.RatingResponse;
import com.carpooling.entity.*;
import com.carpooling.exception.BusinessException;
import com.carpooling.exception.ResourceNotFoundException;
import com.carpooling.mapper.RatingMapper;
import com.carpooling.repository.BookingRepository;
import com.carpooling.repository.RatingRepository;
import com.carpooling.repository.TripRepository;
import com.carpooling.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
public class RatingService {

    private final RatingRepository ratingRepository;
    private final TripRepository tripRepository;
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;
    private final RatingMapper ratingMapper;

    public RatingService(RatingRepository ratingRepository,
                         TripRepository tripRepository,
                         UserRepository userRepository,
                         BookingRepository bookingRepository,
                         RatingMapper ratingMapper) {
        this.ratingRepository = ratingRepository;
        this.tripRepository = tripRepository;
        this.userRepository = userRepository;
        this.bookingRepository = bookingRepository;
        this.ratingMapper = ratingMapper;
    }

    @Transactional
    public RatingResponse submitRating(Long tripId, RatingRequest request, Authentication auth) {
        User rater = resolveUser(auth);
        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new ResourceNotFoundException("Trip", tripId));

        if (trip.getStatus() != TripStatus.COMPLETED) {
            throw new BusinessException("Ratings can only be submitted for COMPLETED trips");
        }

        // Verify rater participated (was the driver or an approved passenger)
        boolean isDriver = trip.getDriver().getId().equals(rater.getId());
        boolean isApprovedPassenger = bookingRepository
                .findByTripIdAndPassengerId(tripId, rater.getId())
                .map(b -> b.getStatus() == BookingStatus.APPROVED)
                .orElse(false);

        if (!isDriver && !isApprovedPassenger) {
            throw new BusinessException("You did not participate in this trip");
        }

        User rated = userRepository.findById(request.ratedId())
                .orElseThrow(() -> new ResourceNotFoundException("User", request.ratedId()));

        if (rated.getId().equals(rater.getId())) {
            throw new BusinessException("You cannot rate yourself");
        }

        if (ratingRepository.existsByTripIdAndRaterIdAndRatedId(tripId, rater.getId(), rated.getId())) {
            throw new BusinessException("You have already rated this user for this trip");
        }

        Rating rating = Rating.builder()
                .trip(trip)
                .rater(rater)
                .rated(rated)
                .score(request.score())
                .comment(request.comment())
                .build();

        Rating saved = ratingRepository.save(rating);

        // Update rated user's average rating and count
        double avg = ratingRepository.findAvgScoreByRatedId(rated.getId()).orElse(0.0);
        long count = ratingRepository.countByRatedId(rated.getId());
        rated.setAvgRating(BigDecimal.valueOf(avg).setScale(2, RoundingMode.HALF_UP));
        rated.setRatingCount((int) count);
        userRepository.save(rated);

        return ratingMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<RatingResponse> getTripRatings(Long tripId) {
        tripRepository.findById(tripId)
                .orElseThrow(() -> new ResourceNotFoundException("Trip", tripId));
        return ratingRepository.findByTripId(tripId)
                .stream()
                .map(ratingMapper::toResponse)
                .toList();
    }

    private User resolveUser(Authentication auth) {
        return userRepository.findByUsername(auth.getName())
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + auth.getName()));
    }
}
