package com.carpooling.controller;

import com.carpooling.dto.BookingResponse;
import com.carpooling.service.BookingService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/bookings")
public class BookingController {

    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<BookingResponse> getBooking(@PathVariable Long id,
                                                       Authentication auth) {
        return ResponseEntity.ok(bookingService.getById(id, auth));
    }

    @PatchMapping("/{id}/approve")
    public ResponseEntity<BookingResponse> approve(@PathVariable Long id,
                                                    Authentication auth) {
        return ResponseEntity.ok(bookingService.approveBooking(id, auth));
    }

    @PatchMapping("/{id}/reject")
    public ResponseEntity<BookingResponse> reject(@PathVariable Long id,
                                                   Authentication auth) {
        return ResponseEntity.ok(bookingService.rejectBooking(id, auth));
    }

    @PatchMapping("/{id}/cancel")
    public ResponseEntity<BookingResponse> cancel(@PathVariable Long id,
                                                   Authentication auth) {
        return ResponseEntity.ok(bookingService.cancelBooking(id, auth));
    }
}
