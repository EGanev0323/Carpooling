package com.carpooling.mapper;

import com.carpooling.dto.BookingResponse;
import com.carpooling.dto.TripSummaryResponse;
import com.carpooling.dto.UserResponse;
import com.carpooling.entity.Booking;
import com.carpooling.entity.BookingStatus;
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
public class BookingMapperImpl implements BookingMapper {

    @Autowired
    private TripMapper tripMapper;
    @Autowired
    private UserMapper userMapper;

    @Override
    public BookingResponse toResponse(Booking booking) {
        if ( booking == null ) {
            return null;
        }

        Long id = null;
        TripSummaryResponse trip = null;
        UserResponse passenger = null;
        Integer seatsBooked = null;
        BookingStatus status = null;
        String message = null;
        OffsetDateTime createdAt = null;
        OffsetDateTime updatedAt = null;

        id = booking.getId();
        trip = tripMapper.toSummary( booking.getTrip() );
        passenger = userMapper.toResponse( booking.getPassenger() );
        seatsBooked = booking.getSeatsBooked();
        status = booking.getStatus();
        message = booking.getMessage();
        createdAt = booking.getCreatedAt();
        updatedAt = booking.getUpdatedAt();

        BookingResponse bookingResponse = new BookingResponse( id, trip, passenger, seatsBooked, status, message, createdAt, updatedAt );

        return bookingResponse;
    }
}
