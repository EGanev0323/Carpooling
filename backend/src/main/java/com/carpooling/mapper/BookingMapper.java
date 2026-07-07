package com.carpooling.mapper;

import com.carpooling.dto.BookingResponse;
import com.carpooling.entity.Booking;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", uses = {TripMapper.class, UserMapper.class})
public interface BookingMapper {

    BookingResponse toResponse(Booking booking);
}
