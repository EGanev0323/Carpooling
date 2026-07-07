package bg.tu_sofia.carpooling.chat.api;

import bg.tu_sofia.carpooling.chat.api.dto.ChatMessagePayload;
import bg.tu_sofia.carpooling.chat.api.dto.ChatMessageResponse;
import bg.tu_sofia.carpooling.chat.service.ChatService;
import jakarta.validation.Valid;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Controller
public class ChatWebSocketController {

    private final ChatService chatService;
    private final SimpMessagingTemplate messagingTemplate;

    public ChatWebSocketController(ChatService chatService, SimpMessagingTemplate messagingTemplate) {
        this.chatService = chatService;
        this.messagingTemplate = messagingTemplate;
    }

    /**
     * Client sends to /app/chat.send
     * Broadcasts the saved message to /topic/chat.{channelId}
     * The Principal is set during STOMP CONNECT from the JWT cookie (see JwtHandshakeInterceptor).
     */
    @MessageMapping("/chat.send")
    public void sendMessage(
            @Payload @Valid ChatMessagePayload payload,
            Principal principal
    ) {
        Long senderId = Long.parseLong(principal.getName());
        ChatMessageResponse response = chatService.sendMessage(senderId, payload);
        messagingTemplate.convertAndSend("/topic/chat." + payload.channelId(), response);
    }

    /**
     * Typing indicator — lightweight, not persisted.
     * Client sends to /app/chat.typing
     * Broadcasts userId to /topic/chat.{channelId}.typing
     */
    @MessageMapping("/chat.typing")
    public void sendTyping(@Payload Long channelId, Principal principal) {
        Long userId = Long.parseLong(principal.getName());
        messagingTemplate.convertAndSend("/topic/chat." + channelId + ".typing", userId);
    }
}
