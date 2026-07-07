package com.carpooling.mapper;

import com.carpooling.dto.RegisterRequest;
import com.carpooling.dto.UserResponse;
import com.carpooling.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserMapper {

    UserResponse toResponse(User user);

    /**
     * Maps RegisterRequest to User entity.
     * passwordHash must be set separately in the service layer after BCrypt encoding.
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "passwordHash", ignore = true)
    @Mapping(target = "avatarUrl", ignore = true)
    @Mapping(target = "isPassenger", ignore = true)
    @Mapping(target = "avgRating", ignore = true)
    @Mapping(target = "ratingCount", ignore = true)
    @Mapping(target = "tripsAsDriver", ignore = true)
    @Mapping(target = "tripsAsPassenger", ignore = true)
    @Mapping(target = "isActive", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    User toEntity(RegisterRequest request);
}
