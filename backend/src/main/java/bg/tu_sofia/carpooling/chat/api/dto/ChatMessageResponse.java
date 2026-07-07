package bg.tu_sofia.carpooling.chat.api.dto;

import java.time.OffsetDateTime;

public record ChatMessageResponse(
        Long id,
        Long channelId,
        SenderSummary sender,
        String content,
        OffsetDateTime createdAt,
        OffsetDateTime readAt
) {
    public record SenderSummary(Long id, String firstName, String lastName, String avatarUrl) {}
}
