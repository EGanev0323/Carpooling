package com.carpooling.controller;

import com.carpooling.dto.*;
import com.carpooling.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUser(@PathVariable Long id) {
        return ResponseEntity.ok(userService.findById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserResponse> updateUser(@PathVariable Long id,
                                                    @Valid @RequestBody UpdateProfileRequest request,
                                                    Authentication auth) {
        return ResponseEntity.ok(userService.updateProfile(id, request, auth));
    }

    @PatchMapping("/{id}/password")
    public ResponseEntity<Void> changePassword(@PathVariable Long id,
                                                @Valid @RequestBody ChangePasswordRequest request,
                                                Authentication auth) {
        userService.changePassword(id, request, auth);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/ratings")
    public ResponseEntity<List<RatingResponse>> getUserRatings(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getUserRatings(id));
    }

    @GetMapping("/{id}/trips")
    public ResponseEntity<List<TripSummaryResponse>> getUserTrips(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getUserTrips(id));
    }

    @GetMapping("/{id}/bookings")
    public ResponseEntity<List<BookingResponse>> getUserBookings(@PathVariable Long id,
                                                                  Authentication auth) {
        return ResponseEntity.ok(userService.getUserBookings(id, auth));
    }
}
