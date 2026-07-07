package com.carpooling.mapper;

import com.carpooling.dto.CarResponse;
import com.carpooling.dto.DriverSummary;
import com.carpooling.dto.TripRequest;
import com.carpooling.dto.TripResponse;
import com.carpooling.dto.TripSummaryResponse;
import com.carpooling.dto.UserResponse;
import com.carpooling.entity.Trip;
import com.carpooling.entity.TripStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import javax.annotation.processing.Generated;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-07-07T15:18:41+0300",
    comments = "version: 1.6.3, compiler: javac, environment: Java 24.0.1 (Oracle Corporation)"
)
@Component
public class TripMapperImpl implements TripMapper {

    @Autowired
    private UserMapper userMapper;
    @Autowired
    private CarMapper carMapper;

    @Override
    public TripResponse toResponse(Trip trip) {
        if ( trip == null ) {
            return null;
        }

        Long id = null;
        String originCity = null;
        String originAddress = null;
        String destinationCity = null;
        String destinationAddress = null;
        LocalDateTime departureTime = null;
        LocalDateTime estimatedArrival = null;
        BigDecimal pricePerSeat = null;
        Integer totalSeats = null;
        Integer availableSeats = null;
        TripStatus status = null;
        String description = null;
        Boolean smokingAllowed = null;
        Boolean petsAllowed = null;
        OffsetDateTime createdAt = null;
        UserResponse driver = null;
        CarResponse car = null;

        id = trip.getId();
        originCity = trip.getOriginCity();
        originAddress = trip.getOriginAddress();
        destinationCity = trip.getDestinationCity();
        destinationAddress = trip.getDestinationAddress();
        departureTime = trip.getDepartureTime();
        estimatedArrival = trip.getEstimatedArrival();
        pricePerSeat = trip.getPricePerSeat();
        totalSeats = trip.getTotalSeats();
        availableSeats = trip.getAvailableSeats();
        status = trip.getStatus();
        description = trip.getDescription();
        smokingAllowed = trip.getSmokingAllowed();
        petsAllowed = trip.getPetsAllowed();
        createdAt = trip.getCreatedAt();
        driver = userMapper.toResponse( trip.getDriver() );
        car = carMapper.toResponse( trip.getCar() );

        TripResponse tripResponse = new TripResponse( id, originCity, originAddress, destinationCity, destinationAddress, departureTime, estimatedArrival, pricePerSeat, totalSeats, availableSeats, status, description, smokingAllowed, petsAllowed, createdAt, driver, car );

        return tripResponse;
    }

    @Override
    public TripSummaryResponse toSummary(Trip trip) {
        if ( trip == null ) {
            return null;
        }

        DriverSummary driver = null;
        Long id = null;
        String originCity = null;
        String destinationCity = null;
        LocalDateTime departureTime = null;
        BigDecimal pricePerSeat = null;
        Integer availableSeats = null;
        TripStatus status = null;

        driver = toDriverSummary( trip.getDriver() );
        id = trip.getId();
        originCity = trip.getOriginCity();
        destinationCity = trip.getDestinationCity();
        departureTime = trip.getDepartureTime();
        pricePerSeat = trip.getPricePerSeat();
        availableSeats = trip.getAvailableSeats();
        status = trip.getStatus();

        TripSummaryResponse tripSummaryResponse = new TripSummaryResponse( id, originCity, destinationCity, departureTime, pricePerSeat, availableSeats, status, driver );

        return tripSummaryResponse;
    }

    @Override
    public Trip toEntity(TripRequest request) {
        if ( request == null ) {
            return null;
        }

        Trip.TripBuilder trip = Trip.builder();

        trip.originCity( request.originCity() );
        trip.originAddress( request.originAddress() );
        trip.destinationCity( request.destinationCity() );
        trip.destinationAddress( request.destinationAddress() );
        trip.departureTime( request.departureTime() );
        trip.estimatedArrival( request.estimatedArrival() );
        trip.pricePerSeat( request.pricePerSeat() );
        trip.totalSeats( request.totalSeats() );
        trip.description( request.description() );
        trip.smokingAllowed( request.smokingAllowed() );
        trip.petsAllowed( request.petsAllowed() );

        return trip.build();
    }
}
