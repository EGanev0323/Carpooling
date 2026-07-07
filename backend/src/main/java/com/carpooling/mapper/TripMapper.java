package com.carpooling.mapper;

import com.carpooling.dto.DriverSummary;
import com.carpooling.dto.TripRequest;
import com.carpooling.dto.TripResponse;
import com.carpooling.dto.TripSummaryResponse;
import com.carpooling.entity.Trip;
import com.carpooling.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {UserMapper.class, CarMapper.class})
public interface TripMapper {

    TripResponse toResponse(Trip trip);

    @Mapping(target = "driver", source = "driver")
    TripSummaryResponse toSummary(Trip trip);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "driver", ignore = true)
    @Mapping(target = "car", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "availableSeats", ignore = true)
    @Mapping(target = "bookings", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Trip toEntity(TripRequest request);

    default DriverSummary toDriverSummary(User user) {
        if (user == null) return null;
        return new DriverSummary(
                user.getId(),
                user.getFirstName(),
                user.getLastName(),
                user.getAvgRating()
        );
    }
}
