package bg.tu_sofia.carpooling.chat.api;

import bg.tu_sofia.carpooling.chat.api.dto.ChatChannelResponse;
import bg.tu_sofia.carpooling.chat.api.dto.ChatMessageResponse;
import bg.tu_sofia.carpooling.chat.service.ChatService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/chat")
public class ChatController {

    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @GetMapping("/channels")
    public ResponseEntity<List<ChatChannelResponse>> getMyChannels(
            @AuthenticationPrincipal Long userId
    ) {
        return ResponseEntity.ok(chatService.getMyChannels(userId));
    }

    @GetMapping("/channels/{channelId}/messages")
    public ResponseEntity<Page<ChatMessageResponse>> getMessages(
            @PathVariable Long channelId,
            @AuthenticationPrincipal Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size
    ) {
        // Cap page size to prevent abuse
        int safeSize = Math.min(size, 100);
        Pageable pageable = PageRequest.of(page, safeSize);
        return ResponseEntity.ok(chatService.getMessageHistory(channelId, userId, pageable));
    }
}
