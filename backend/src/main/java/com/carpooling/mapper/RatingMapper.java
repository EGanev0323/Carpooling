package com.carpooling.mapper;

import com.carpooling.dto.RatingResponse;
import com.carpooling.entity.Rating;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {UserMapper.class})
public interface RatingMapper {

    @Mapping(target = "tripId", source = "trip.id")
    RatingResponse toResponse(Rating rating);
}
