package com.carpooling.mapper;

import com.carpooling.dto.RegisterRequest;
import com.carpooling.dto.UserResponse;
import com.carpooling.entity.User;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-07-07T15:18:41+0300",
    comments = "version: 1.6.3, compiler: javac, environment: Java 24.0.1 (Oracle Corporation)"
)
@Component
public class UserMapperImpl implements UserMapper {

    @Override
    public UserResponse toResponse(User user) {
        if ( user == null ) {
            return null;
        }

        Long id = null;
        String username = null;
        String email = null;
        String firstName = null;
        String lastName = null;
        String phone = null;
        String avatarUrl = null;
        Boolean isDriver = null;
        Boolean isPassenger = null;
        Boolean isAdmin = null;
        BigDecimal avgRating = null;
        Integer ratingCount = null;
        Integer tripsAsDriver = null;
        Integer tripsAsPassenger = null;
        OffsetDateTime createdAt = null;

        id = user.getId();
        username = user.getUsername();
        email = user.getEmail();
        firstName = user.getFirstName();
        lastName = user.getLastName();
        phone = user.getPhone();
        avatarUrl = user.getAvatarUrl();
        isDriver = user.getIsDriver();
        isPassenger = user.getIsPassenger();
        isAdmin = user.getIsAdmin();
        avgRating = user.getAvgRating();
        ratingCount = user.getRatingCount();
        tripsAsDriver = user.getTripsAsDriver();
        tripsAsPassenger = user.getTripsAsPassenger();
        createdAt = user.getCreatedAt();

        UserResponse userResponse = new UserResponse( id, username, email, firstName, lastName, phone, avatarUrl, isDriver, isPassenger, isAdmin, avgRating, ratingCount, tripsAsDriver, tripsAsPassenger, createdAt );

        return userResponse;
    }

    @Override
    public User toEntity(RegisterRequest request) {
        if ( request == null ) {
            return null;
        }

        User.UserBuilder user = User.builder();

        user.username( request.username() );
        user.email( request.email() );
        user.firstName( request.firstName() );
        user.lastName( request.lastName() );
        user.phone( request.phone() );
        user.isDriver( request.isDriver() );

        return user.build();
    }
}
