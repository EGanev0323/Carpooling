package com.carpooling.dto;

public record CarResponse(
        Long id,
        String make,
        String model,
        Integer year,
        String color,
        String licensePlate,
        Integer totalSeats,
        Boolean isActive
) {}
