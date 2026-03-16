package com.example.backend_service.controller.chat;

import com.example.backend_service.dto.request.ChatMessageRequest;
import com.example.backend_service.dto.response.ChatMessageResponse;
import com.example.backend_service.dto.response.ChatRoomResponse;
import com.example.backend_service.model.auth.User;
import com.example.backend_service.model.chat.ChatRoom;
import com.example.backend_service.service.chat.ChatService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
@Slf4j(topic = "CHAT-CONTROLLER")
public class ChatController {

    private final ChatService chatService;
    private final SimpMessagingTemplate messagingTemplate;

    // ==================== WebSocket STOMP ====================

    @MessageMapping("/chat.send")
    public void sendMessage(@Payload ChatMessageRequest request, Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        log.info("WebSocket message from user {} to room {}", user.getUsername(), request.getChatRoomId());

        ChatMessageResponse response = chatService.sendMessage(
                request.getChatRoomId(), user.getId(), request.getContent());

        // Broadcast to topic so both participants receive the message
        messagingTemplate.convertAndSend("/topic/chat/" + request.getChatRoomId(), response);

        // Global notifications for sidebar updates
        List<Long> participantIds = chatService.getParticipantIds(request.getChatRoomId());
        participantIds.forEach(id -> messagingTemplate.convertAndSend("/topic/user/" + id, response));
    }

    // ==================== REST API ====================

    /**
     * Create or get a chat room between current user and a shop
     */
    @PostMapping("/rooms")
    public ResponseEntity<ChatRoomResponse> createOrGetRoom(
            @AuthenticationPrincipal User user,
            @RequestBody Map<String, Long> body) {

        Long shopId = body.get("shopId");
        ChatRoom room = chatService.getOrCreateChatRoom(user.getId(), shopId);

        ChatRoomResponse response = ChatRoomResponse.builder()
                .id(room.getId())
                .shopId(room.getShop().getId())
                .shopName(room.getShop().getShopName())
                .shopLogo(room.getShop().getLogoUrl())
                .userId(user.getId())
                .userName(user.getFullName())
                .userAvatar(user.getAvatarUrl())
                .unreadCount(0L)
                .build();

        return ResponseEntity.ok(response);
    }

    /**
     * Get all chat rooms for the current user
     */
    @GetMapping("/rooms")
    public ResponseEntity<List<ChatRoomResponse>> getUserRooms(@AuthenticationPrincipal User user) {
        List<ChatRoomResponse> rooms = chatService.getUserChatRooms(user.getId());
        return ResponseEntity.ok(rooms);
    }

    /**
     * Get all chat rooms for the current user's shop
     */
    @GetMapping("/rooms/shop")
    public ResponseEntity<List<ChatRoomResponse>> getShopRooms(@AuthenticationPrincipal User user) {
        if (user.getShop() == null) {
            return ResponseEntity.badRequest().build();
        }
        List<ChatRoomResponse> rooms = chatService.getShopChatRooms(user.getShop().getId());
        return ResponseEntity.ok(rooms);
    }

    /**
     * Get message history for a chat room
     */
    @GetMapping("/rooms/{roomId}/messages")
    public ResponseEntity<List<ChatMessageResponse>> getChatHistory(@PathVariable Long roomId) {
        List<ChatMessageResponse> messages = chatService.getChatHistory(roomId);
        return ResponseEntity.ok(messages);
    }

    /**
     * Mark all messages in a chat room as read
     */
    @PatchMapping("/rooms/{roomId}/read")
    public ResponseEntity<Void> markAsRead(
            @PathVariable Long roomId,
            @AuthenticationPrincipal User user) {
        chatService.markAsRead(roomId, user.getId());
        return ResponseEntity.ok().build();
    }
}
