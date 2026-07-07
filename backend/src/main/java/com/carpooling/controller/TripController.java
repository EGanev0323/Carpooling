package com.carpooling.controller;

import com.carpooling.dto.*;
import com.carpooling.service.BookingService;
import com.carpooling.service.RatingService;
import com.carpooling.service.TripService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/trips")
public class TripController {

    private final TripService tripService;
    private final BookingService bookingService;
    private final RatingService ratingService;

    public TripController(TripService tripService,
                          BookingService bookingService,
                          RatingService ratingService) {
        this.tripService = tripService;
        this.bookingService = bookingService;
        this.ratingService = ratingService;
    }

    @GetMapping
    public ResponseEntity<Page<TripSummaryResponse>> searchTrips(
            @RequestParam(required = false) String origin,
            @RequestParam(required = false) String destination,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) Integer seats,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(tripService.searchTrips(origin, destination, date, seats, pageable));
    }

    @PostMapping
    public ResponseEntity<TripResponse> createTrip(@Valid @RequestBody TripRequest request,
                                                    Authentication auth) {
        return ResponseEntity.status(HttpStatus.CREATED).body(tripService.createTrip(request, auth));
    }

    @GetMapping("/{id}")
    public ResponseEntity<TripResponse> getTrip(@PathVariable Long id) {
        return ResponseEntity.ok(tripService.getTripById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<TripResponse> updateTrip(@PathVariable Long id,
                                                    @Valid @RequestBody TripRequest request,
                                                    Authentication auth) {
        return ResponseEntity.ok(tripService.updateTrip(id, request, auth));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> cancelTrip(@PathVariable Long id, Authentication auth) {
        tripService.cancelTrip(id, auth);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/bookings")
    public ResponseEntity<List<BookingResponse>> getTripBookings(@PathVariable Long id,
                                                                  Authentication auth) {
        return ResponseEntity.ok(tripService.getTripBookings(id, auth));
    }

    @PostMapping("/{tripId}/bookings")
    public ResponseEntity<BookingResponse> bookTrip(@PathVariable Long tripId,
                                                     @Valid @RequestBody BookingRequest request,
                                                     Authentication auth) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(bookingService.bookTrip(tripId, request, auth));
    }

    @GetMapping("/{tripId}/ratings")
    public ResponseEntity<List<RatingResponse>> getTripRatings(@PathVariable Long tripId) {
        return ResponseEntity.ok(ratingService.getTripRatings(tripId));
    }

    @PostMapping("/{tripId}/ratings")
    public ResponseEntity<RatingResponse> submitRating(@PathVariable Long tripId,
                                                        @Valid @RequestBody RatingRequest request,
                                                        Authentication auth) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ratingService.submitRating(tripId, request, auth));
    }
}
