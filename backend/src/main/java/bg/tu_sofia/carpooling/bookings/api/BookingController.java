package bg.tu_sofia.carpooling.bookings.api;

import bg.tu_sofia.carpooling.bookings.api.dto.BookingRequest;
import bg.tu_sofia.carpooling.bookings.api.dto.BookingResponse;
import bg.tu_sofia.carpooling.bookings.service.BookingService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/bookings")
public class BookingController {

    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @PostMapping
    public ResponseEntity<BookingResponse> createBooking(
            @AuthenticationPrincipal Long passengerId,
            @Valid @RequestBody BookingRequest request) {
        BookingResponse response = bookingService.createBooking(passengerId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/my")
    public ResponseEntity<List<BookingResponse>> getMyBookings(@AuthenticationPrincipal Long passengerId) {
        return ResponseEntity.ok(bookingService.getMyBookings(passengerId));
    }

    @GetMapping("/ride/{ridePublicId}")
    public ResponseEntity<List<BookingResponse>> getRideBookings(
            @PathVariable UUID ridePublicId,
            @AuthenticationPrincipal Long driverId) {
        return ResponseEntity.ok(bookingService.getRideBookings(ridePublicId, driverId));
    }

    @PostMapping("/{publicId}/confirm")
    public ResponseEntity<BookingResponse> confirmBooking(
            @PathVariable UUID publicId,
            @AuthenticationPrincipal Long driverId) {
        return ResponseEntity.ok(bookingService.confirmBooking(publicId, driverId));
    }

    @PostMapping("/{publicId}/reject")
    public ResponseEntity<BookingResponse> rejectBooking(
            @PathVariable UUID publicId,
            @AuthenticationPrincipal Long driverId) {
        return ResponseEntity.ok(bookingService.rejectBooking(publicId, driverId));
    }

    @PostMapping("/{publicId}/cancel")
    public ResponseEntity<BookingResponse> cancelBooking(
            @PathVariable UUID publicId,
            @AuthenticationPrincipal Long passengerId) {
        return ResponseEntity.ok(bookingService.cancelBooking(publicId, passengerId));
    }
}
