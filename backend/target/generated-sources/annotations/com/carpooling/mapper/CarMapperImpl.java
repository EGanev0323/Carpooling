package com.carpooling.mapper;

import com.carpooling.dto.CarRequest;
import com.carpooling.dto.CarResponse;
import com.carpooling.entity.Car;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-07-07T15:18:41+0300",
    comments = "version: 1.6.3, compiler: javac, environment: Java 24.0.1 (Oracle Corporation)"
)
@Component
public class CarMapperImpl implements CarMapper {

    @Override
    public CarResponse toResponse(Car car) {
        if ( car == null ) {
            return null;
        }

        Long id = null;
        String make = null;
        String model = null;
        Integer year = null;
        String color = null;
        String licensePlate = null;
        Integer totalSeats = null;
        Boolean isActive = null;

        id = car.getId();
        make = car.getMake();
        model = car.getModel();
        year = car.getYear();
        color = car.getColor();
        licensePlate = car.getLicensePlate();
        totalSeats = car.getTotalSeats();
        isActive = car.getIsActive();

        CarResponse carResponse = new CarResponse( id, make, model, year, color, licensePlate, totalSeats, isActive );

        return carResponse;
    }

    @Override
    public Car toEntity(CarRequest request) {
        if ( request == null ) {
            return null;
        }

        Car.CarBuilder car = Car.builder();

        car.make( request.make() );
        car.model( request.model() );
        car.year( request.year() );
        car.color( request.color() );
        car.licensePlate( request.licensePlate() );
        car.totalSeats( request.totalSeats() );

        return car.build();
    }
}
