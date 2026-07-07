package com.carpooling.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

public record UpdateProfileRequest(
        @Size(max = 50) String firstName,
        @Size(max = 50) String lastName,
        @Email @Size(max = 100) String email,
        @Size(max = 20) String phone,
        @Size(max = 500) String avatarUrl,
        Boolean isDriver,
        Boolean isPassenger
) {}
