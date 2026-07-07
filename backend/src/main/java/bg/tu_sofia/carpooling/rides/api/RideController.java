package bg.tu_sofia.carpooling.rides.api;

import bg.tu_sofia.carpooling.rides.api.dto.RideRequest;
import bg.tu_sofia.carpooling.rides.api.dto.RideResponse;
import bg.tu_sofia.carpooling.rides.api.dto.RideSearchParams;
import bg.tu_sofia.carpooling.rides.service.RideService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/rides")
public class RideController {

    private final RideService rideService;

    public RideController(RideService rideService) {
        this.rideService = rideService;
    }

    @PostMapping
    @PreAuthorize("hasRole('DRIVER')")
    public ResponseEntity<RideResponse> createRide(
            @AuthenticationPrincipal Long driverId,
            @Valid @RequestBody RideRequest request) {
        RideResponse response = rideService.createRide(driverId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<Page<RideResponse>> searchRides(
            @RequestParam(required = false) Integer originCityId,
            @RequestParam(required = false) Integer destinationCityId,
            @RequestParam(required = false) LocalDate date,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "departureAt,asc") String sort) {

        RideSearchParams params = new RideSearchParams(originCityId, destinationCityId, date);
        String[] sortParts = sort.split(",");
        String sortField = sortParts[0];
        Sort.Direction direction = sortParts.length > 1 && "desc".equalsIgnoreCase(sortParts[1])
                ? Sort.Direction.DESC
                : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortField));

        return ResponseEntity.ok(rideService.searchRides(params, pageable));
    }

    @GetMapping("/{publicId}")
    public ResponseEntity<RideResponse> getRide(
            @PathVariable UUID publicId,
            @AuthenticationPrincipal Long userId) {
        return ResponseEntity.ok(rideService.getRide(publicId, userId));
    }

    @PostMapping("/{publicId}/cancel")
    public ResponseEntity<Void> cancelRide(
            @PathVariable UUID publicId,
            @AuthenticationPrincipal Long driverId) {
        rideService.cancelRide(publicId, driverId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/my")
    public ResponseEntity<List<RideResponse>> getMyRides(@AuthenticationPrincipal Long driverId) {
        return ResponseEntity.ok(rideService.getMyRides(driverId));
    }
}
