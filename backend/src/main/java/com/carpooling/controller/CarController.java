package com.carpooling.controller;

import com.carpooling.dto.CarRequest;
import com.carpooling.dto.CarResponse;
import com.carpooling.service.CarService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cars")
public class CarController {

    private final CarService carService;

    public CarController(CarService carService) {
        this.carService = carService;
    }

    @GetMapping
    public ResponseEntity<List<CarResponse>> getMyCars(Authentication auth) {
        return ResponseEntity.ok(carService.getMyCars(auth));
    }

    @PostMapping
    public ResponseEntity<CarResponse> addCar(@Valid @RequestBody CarRequest request,
                                               Authentication auth) {
        return ResponseEntity.status(HttpStatus.CREATED).body(carService.addCar(request, auth));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CarResponse> getCar(@PathVariable Long id, Authentication auth) {
        return ResponseEntity.ok(carService.getCarById(id, auth));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CarResponse> updateCar(@PathVariable Long id,
                                                  @Valid @RequestBody CarRequest request,
                                                  Authentication auth) {
        return ResponseEntity.ok(carService.updateCar(id, request, auth));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCar(@PathVariable Long id, Authentication auth) {
        carService.deleteCar(id, auth);
        return ResponseEntity.noContent().build();
    }
}
