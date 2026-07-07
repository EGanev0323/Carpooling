package com.carpooling.service;

import com.carpooling.dto.CarRequest;
import com.carpooling.dto.CarResponse;
import com.carpooling.entity.Car;
import com.carpooling.entity.User;
import com.carpooling.exception.ResourceNotFoundException;
import com.carpooling.exception.UnauthorizedException;
import com.carpooling.mapper.CarMapper;
import com.carpooling.repository.CarRepository;
import com.carpooling.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CarService {

    private final CarRepository carRepository;
    private final UserRepository userRepository;
    private final CarMapper carMapper;

    public CarService(CarRepository carRepository,
                      UserRepository userRepository,
                      CarMapper carMapper) {
        this.carRepository = carRepository;
        this.userRepository = userRepository;
        this.carMapper = carMapper;
    }

    @Transactional(readOnly = true)
    public List<CarResponse> getMyCars(Authentication auth) {
        User user = resolveUser(auth);
        return carRepository.findByOwnerIdAndIsActiveTrue(user.getId())
                .stream()
                .map(carMapper::toResponse)
                .toList();
    }

    @Transactional
    public CarResponse addCar(CarRequest request, Authentication auth) {
        User owner = resolveUser(auth);
        Car car = carMapper.toEntity(request);
        car.setOwner(owner);
        car.setIsActive(true);
        return carMapper.toResponse(carRepository.save(car));
    }

    @Transactional(readOnly = true)
    public CarResponse getCarById(Long id, Authentication auth) {
        Car car = findOwnedCar(id, auth);
        return carMapper.toResponse(car);
    }

    @Transactional
    public CarResponse updateCar(Long id, CarRequest request, Authentication auth) {
        Car car = findOwnedCar(id, auth);
        car.setMake(request.make());
        car.setModel(request.model());
        car.setYear(request.year());
        car.setColor(request.color());
        car.setLicensePlate(request.licensePlate());
        car.setTotalSeats(request.totalSeats());
        return carMapper.toResponse(carRepository.save(car));
    }

    @Transactional
    public void deleteCar(Long id, Authentication auth) {
        Car car = findOwnedCar(id, auth);
        car.setIsActive(false);
        carRepository.save(car);
    }

    private Car findOwnedCar(Long id, Authentication auth) {
        Car car = carRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Car", id));
        User caller = resolveUser(auth);
        if (!car.getOwner().getId().equals(caller.getId())) {
            throw new UnauthorizedException("You do not own this car");
        }
        return car;
    }

    private User resolveUser(Authentication auth) {
        return userRepository.findByUsername(auth.getName())
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + auth.getName()));
    }
}
