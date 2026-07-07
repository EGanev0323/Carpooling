package com.carpooling.repository;

import com.carpooling.entity.Booking;
import com.carpooling.entity.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    List<Booking> findByTripId(Long tripId);

    List<Booking> findByPassengerIdOrderByCreatedAtDesc(Long passengerId);

    Optional<Booking> findByTripIdAndPassengerId(Long tripId, Long passengerId);

    boolean existsByTripIdAndPassengerIdAndStatusNot(Long tripId, Long passengerId, BookingStatus status);
}
