package com.carpooling.mapper;

import com.carpooling.dto.CarRequest;
import com.carpooling.dto.CarResponse;
import com.carpooling.entity.Car;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CarMapper {

    CarResponse toResponse(Car car);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "owner", ignore = true)
    @Mapping(target = "isActive", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Car toEntity(CarRequest request);
}
