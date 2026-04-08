package com.example.backend_service.controller.chat;

import com.example.backend_service.dto.request.ChatMessageRequest;
import com.example.backend_service.dto.response.ChatMessageResponse;
import com.example.backend_service.dto.response.ChatRoomResponse;
import com.example.backend_service.model.auth.User;
import com.example.backend_service.model.chat.ChatRoom;
import com.example.backend_service.model.order.Order;
import com.example.backend_service.model.product.Product;
import com.example.backend_service.repository.OrderRepository;
import com.example.backend_service.repository.ProductRepository;
import com.example.backend_service.service.chat.ChatService;
import com.example.backend_service.common.MessageType;
import com.example.backend_service.dto.request.SendOrderMessageRequest;
import com.example.backend_service.dto.request.SendProductMessageRequest;
import com.example.backend_service.service.common.StorageService;
import org.springframework.web.multipart.MultipartFile;

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
    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final StorageService storageService;

    // ==================== WebSocket STOMP ====================

    @MessageMapping("/chat.send")
    public void sendMessage(@Payload ChatMessageRequest request, Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        log.info("WebSocket message from user {} to room {}", user.getUsername(), request.getChatRoomId());

        ChatMessageResponse response = chatService.sendMessage(
                request.getChatRoomId(), user.getId(), request.getContent(), request.getMessageType());

        // Broadcast to topic so both participants receive the message
        messagingTemplate.convertAndSend("/topic/chat/" + request.getChatRoomId(), response);

        // Global notifications for sidebar updates
        List<Long> participantIds = chatService.getParticipantIds(request.getChatRoomId());
        participantIds.forEach(id -> messagingTemplate.convertAndSend("/topic/user/" + id, response));
    }

    @MessageMapping("/chat.sendOrder")
    public void sendOrderMessage(@Payload SendOrderMessageRequest request, Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        log.info("WebSocket sendOrder message from user {} to room {}", user.getUsername(), request.getChatRoomId());

        Order order = orderRepository.findById(request.getOrderId())
                .orElseThrow(() -> new RuntimeException("Order not found"));

        if (!order.getUser().getId().equals(user.getId()) && !order.getShop().getOwner().getId().equals(user.getId())) {
            throw new RuntimeException("Unauthorized to send this order");
        }

        String jsonContent = String.format("{\"orderId\": %d, \"status\": \"%s\", \"totalAmount\": %s}", 
                order.getId(), order.getStatus().name(), order.getTotalAmount().toPlainString());

        ChatMessageResponse response = chatService.sendMessage(
                request.getChatRoomId(), user.getId(), jsonContent, MessageType.ORDER_INFO);

        messagingTemplate.convertAndSend("/topic/chat/" + request.getChatRoomId(), response);

        List<Long> participantIds = chatService.getParticipantIds(request.getChatRoomId());
        participantIds.forEach(id -> messagingTemplate.convertAndSend("/topic/user/" + id, response));
    }

    @MessageMapping("/chat.sendProduct")
    public void sendProductMessage(@Payload SendProductMessageRequest request, Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        log.info("WebSocket sendProduct message from user {} to room {}", user.getUsername(), request.getChatRoomId());

        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new RuntimeException("Product not found"));

        String jsonContent = String.format("{\"productId\": %d, \"name\": \"%s\", \"price\": %s, \"imageUrl\": \"%s\"}", 
                product.getId(), 
                product.getName().replace("\"", "\\\""), 
                product.getPrice().toPlainString(), 
                product.getImageUrl() != null ? product.getImageUrl().replace("\"", "\\\"") : "");

        ChatMessageResponse response = chatService.sendMessage(
                request.getChatRoomId(), user.getId(), jsonContent, MessageType.PRODUCT_INFO);

        messagingTemplate.convertAndSend("/topic/chat/" + request.getChatRoomId(), response);

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

    /**
     * Upload an image to send in chat
     */
    @PostMapping("/rooms/{roomId}/image")
    public ResponseEntity<ChatMessageResponse> uploadImageMessage(
            @PathVariable Long roomId,
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal User user) {
        
        String imageUrl = storageService.uploadFile(file, "chat_images");

        ChatMessageResponse response = chatService.sendMessage(
                roomId, user.getId(), imageUrl, MessageType.IMAGE);

        messagingTemplate.convertAndSend("/topic/chat/" + roomId, response);
        List<Long> participantIds = chatService.getParticipantIds(roomId);
        participantIds.forEach(id -> messagingTemplate.convertAndSend("/topic/user/" + id, response));

        return ResponseEntity.ok(response);
    }
}
