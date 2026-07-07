package bg.tu_sofia.carpooling.auth.api.dto;

import jakarta.validation.constraints.*;

public record RegisterRequest(
        @Email(message = "Must be a valid email address")
        @NotBlank(message = "Email is required")
        String email,

        @NotBlank(message = "Password is required")
        @Size(min = 12, message = "Password must be at least 12 characters long")
        String password,

        @NotBlank(message = "First name is required")
        @Size(max = 64, message = "First name must not exceed 64 characters")
        String firstName,

        @NotBlank(message = "Last name is required")
        @Size(max = 64, message = "Last name must not exceed 64 characters")
        String lastName,

        @NotBlank(message = "Phone number is required")
        @Pattern(
                regexp = "^(\\+359|0)[0-9]{8,9}$",
                message = "Phone must be a valid Bulgarian number (e.g. +359888123456 or 0888123456)"
        )
        String phone,

        @NotBlank(message = "Turnstile token is required")
        String turnstileToken
) {}
