package bg.tu_sofia.carpooling.cars.service;

import bg.tu_sofia.carpooling.auth.domain.Role;
import bg.tu_sofia.carpooling.auth.domain.RoleName;
import bg.tu_sofia.carpooling.auth.domain.User;
import bg.tu_sofia.carpooling.auth.repository.RoleRepository;
import bg.tu_sofia.carpooling.auth.repository.UserRepository;
import bg.tu_sofia.carpooling.cars.api.dto.CarRequest;
import bg.tu_sofia.carpooling.cars.api.dto.CarResponse;
import bg.tu_sofia.carpooling.cars.domain.Car;
import bg.tu_sofia.carpooling.cars.repository.CarRepository;
import bg.tu_sofia.carpooling.common.exception.BusinessException;
import bg.tu_sofia.carpooling.common.exception.ResourceNotFoundException;
import bg.tu_sofia.carpooling.common.exception.UnauthorizedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CarService {

    private final CarRepository carRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    public CarService(CarRepository carRepository,
                      UserRepository userRepository,
                      RoleRepository roleRepository) {
        this.carRepository = carRepository;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
    }

    @Transactional
    public CarResponse addCar(Long userId, CarRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        boolean isDriver = user.getRoles().stream()
                .anyMatch(r -> r.getName() == RoleName.DRIVER);

        if (!isDriver) {
            // Auto-assign DRIVER role when adding first car
            Role driverRole = roleRepository.findByName(RoleName.DRIVER)
                    .orElseThrow(() -> new ResourceNotFoundException("DRIVER role not found"));
            user.getRoles().add(driverRole);
            userRepository.save(user);
        }

        Car car = Car.builder()
                .owner(user)
                .make(request.make())
                .model(request.model())
                .year((short) request.year())
                .color(request.color())
                .licensePlate(request.licensePlate())
                .seats((short) request.seats())
                .amenities(request.amenities() != null ? request.amenities() : "{}")
                .build();

        Car saved = carRepository.save(car);
        return toResponse(saved, true);
    }

    @Transactional(readOnly = true)
    public List<CarResponse> getMyCars(Long userId) {
        return carRepository.findByOwnerIdOrderByCreatedAtDesc(userId).stream()
                .map(car -> toResponse(car, true))
                .toList();
    }

    @Transactional(readOnly = true)
    public CarResponse getCar(Long carId, Long userId) {
        Car car = carRepository.findById(carId)
                .orElseThrow(() -> new ResourceNotFoundException("Car not found: " + carId));

        boolean isOwner = car.getOwner().getId().equals(userId);
        if (!isOwner) {
            throw new UnauthorizedException("You do not own this car");
        }
        return toResponse(car, true);
    }

    @Transactional
    public CarResponse updateCar(Long carId, Long userId, CarRequest request) {
        Car car = carRepository.findById(carId)
                .orElseThrow(() -> new ResourceNotFoundException("Car not found: " + carId));

        if (!car.getOwner().getId().equals(userId)) {
            throw new UnauthorizedException("You do not own this car");
        }

        car.setMake(request.make());
        car.setModel(request.model());
        car.setYear((short) request.year());
        car.setColor(request.color());
        car.setLicensePlate(request.licensePlate());
        car.setSeats((short) request.seats());
        car.setAmenities(request.amenities() != null ? request.amenities() : "{}");

        Car saved = carRepository.save(car);
        return toResponse(saved, true);
    }

    @Transactional
    public void deleteCar(Long carId, Long userId) {
        Car car = carRepository.findById(carId)
                .orElseThrow(() -> new ResourceNotFoundException("Car not found: " + carId));

        if (!car.getOwner().getId().equals(userId)) {
            throw new UnauthorizedException("You do not own this car");
        }

        long activeRides = carRepository.countActiveRidesByCar(carId);
        if (activeRides > 0) {
            throw new BusinessException("Car has active rides", 409);
        }

        carRepository.delete(car);
    }

    private CarResponse toResponse(Car car, boolean isOwner) {
        String plate = isOwner ? car.getLicensePlate() : maskPlate(car.getLicensePlate());
        return new CarResponse(
                car.getId(),
                car.getMake(),
                car.getModel(),
                car.getYear(),
                car.getColor(),
                plate,
                car.getSeats(),
                car.getAmenities(),
                car.getCreatedAt(),
                car.getUpdatedAt(),
                isOwner
        );
    }

    private String maskPlate(String plate) {
        if (plate == null || plate.length() < 4) {
            return plate;
        }
        String first = plate.substring(0, 2);
        String last = plate.substring(plate.length() - 2);
        return first + "****" + last;
    }
}
