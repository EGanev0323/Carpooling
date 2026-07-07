package bg.tu_sofia.carpooling.auth.api.dto;

import jakarta.validation.constraints.NotBlank;

public record VerifyEmailRequest(
        @NotBlank(message = "Verification token is required")
        String token
) {}
