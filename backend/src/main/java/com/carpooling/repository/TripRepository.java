package com.carpooling.repository;

import com.carpooling.entity.Trip;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface TripRepository extends JpaRepository<Trip, Long> {

    @Query("SELECT t FROM Trip t WHERE " +
           "(:origin IS NULL OR LOWER(t.originCity) = LOWER(:origin)) AND " +
           "(:destination IS NULL OR LOWER(t.destinationCity) = LOWER(:destination)) AND " +
           "(:date IS NULL OR CAST(t.departureTime AS date) = :date) AND " +
           "(:seats IS NULL OR t.availableSeats >= :seats) AND " +
           "t.status = 'SCHEDULED' " +
           "ORDER BY t.departureTime ASC")
    Page<Trip> searchTrips(
            @Param("origin") String origin,
            @Param("destination") String destination,
            @Param("date") LocalDate date,
            @Param("seats") Integer seats,
            Pageable pageable);

    List<Trip> findByDriverIdOrderByDepartureTimeDesc(Long driverId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT t FROM Trip t WHERE t.id = :id")
    Optional<Trip> findByIdWithLock(@Param("id") Long id);
}
