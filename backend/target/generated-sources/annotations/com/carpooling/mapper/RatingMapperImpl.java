package com.carpooling.mapper;

import com.carpooling.dto.RatingResponse;
import com.carpooling.dto.UserResponse;
import com.carpooling.entity.Rating;
import com.carpooling.entity.Trip;
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
public class RatingMapperImpl implements RatingMapper {

    @Autowired
    private UserMapper userMapper;

    @Override
    public RatingResponse toResponse(Rating rating) {
        if ( rating == null ) {
            return null;
        }

        Long tripId = null;
        Long id = null;
        UserResponse rater = null;
        UserResponse rated = null;
        Integer score = null;
        String comment = null;
        OffsetDateTime createdAt = null;

        tripId = ratingTripId( rating );
        id = rating.getId();
        rater = userMapper.toResponse( rating.getRater() );
        rated = userMapper.toResponse( rating.getRated() );
        score = rating.getScore();
        comment = rating.getComment();
        createdAt = rating.getCreatedAt();

        RatingResponse ratingResponse = new RatingResponse( id, tripId, rater, rated, score, comment, createdAt );

        return ratingResponse;
    }

    private Long ratingTripId(Rating rating) {
        Trip trip = rating.getTrip();
        if ( trip == null ) {
            return null;
        }
        return trip.getId();
    }
}
