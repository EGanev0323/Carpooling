package bg.tu_sofia.carpooling.cars.api;

import bg.tu_sofia.carpooling.cars.api.dto.CarRequest;
import bg.tu_sofia.carpooling.cars.api.dto.CarResponse;
import bg.tu_sofia.carpooling.cars.service.CarService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/cars")
public class CarController {

    private final CarService carService;

    public CarController(CarService carService) {
        this.carService = carService;
    }

    @PostMapping
    public ResponseEntity<CarResponse> addCar(
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody CarRequest request) {
        CarResponse response = carService.addCar(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/my")
    public ResponseEntity<List<CarResponse>> getMyCars(@AuthenticationPrincipal Long userId) {
        return ResponseEntity.ok(carService.getMyCars(userId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CarResponse> getCar(
            @PathVariable Long id,
            @AuthenticationPrincipal Long userId) {
        return ResponseEntity.ok(carService.getCar(id, userId));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CarResponse> updateCar(
            @PathVariable Long id,
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody CarRequest request) {
        return ResponseEntity.ok(carService.updateCar(id, userId, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCar(
            @PathVariable Long id,
            @AuthenticationPrincipal Long userId) {
        carService.deleteCar(id, userId);
        return ResponseEntity.noContent().build();
    }
}
