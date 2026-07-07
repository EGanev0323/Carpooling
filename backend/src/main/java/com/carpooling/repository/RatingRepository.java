package com.carpooling.repository;

import com.carpooling.entity.Rating;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RatingRepository extends JpaRepository<Rating, Long> {

    List<Rating> findByRatedIdOrderByCreatedAtDesc(Long ratedId);

    List<Rating> findByTripId(Long tripId);

    boolean existsByTripIdAndRaterIdAndRatedId(Long tripId, Long raterId, Long ratedId);

    @Query("SELECT AVG(r.score) FROM Rating r WHERE r.rated.id = :userId")
    Optional<Double> findAvgScoreByRatedId(@Param("userId") Long userId);

    long countByRatedId(Long ratedId);
}
