package bg.tu_sofia.carpooling.chat.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ChatMessagePayload(
        @NotNull Long channelId,
        @NotBlank @Size(max = 500) String content
) {}
