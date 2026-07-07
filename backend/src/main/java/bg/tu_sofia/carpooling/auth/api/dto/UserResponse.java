package bg.tu_sofia.carpooling.auth.api.dto;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public record UserResponse(
        UUID publicId,
        String email,
        String firstName,
        String lastName,
        String phone,
        String avatarUrl,
        String bio,
        String status,
        boolean emailVerified,
        OffsetDateTime createdAt,
        List<String> roles
) {}
