package com.example.backend_service.service.chat.impl;

import com.example.backend_service.common.SenderType;
import com.example.backend_service.dto.response.ChatMessageResponse;
import com.example.backend_service.dto.response.ChatRoomResponse;
import com.example.backend_service.model.auth.User;
import com.example.backend_service.model.business.Shop;
import com.example.backend_service.model.chat.ChatMessage;
import com.example.backend_service.model.chat.ChatRoom;
import com.example.backend_service.repository.ChatMessageRepository;
import com.example.backend_service.repository.ChatRoomRepository;
import com.example.backend_service.repository.ShopRepository;
import com.example.backend_service.repository.UserRepository;
import com.example.backend_service.service.chat.ChatService;

import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j(topic = "CHAT-SERVICE")
public class ChatServiceImpl implements ChatService {

    private final ChatRoomRepository chatRoomRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final UserRepository userRepository;
    private final ShopRepository shopRepository;

    @Override
    @Transactional
    public ChatRoom getOrCreateChatRoom(Long userId, Long shopId) {
        return chatRoomRepository.findByUserIdAndShopId(userId, shopId)
                .orElseGet(() -> {
                    User user = userRepository.findById(userId)
                            .orElseThrow(() -> new RuntimeException("User not found"));
                    Shop shop = shopRepository.findById(shopId)
                            .orElseThrow(() -> new RuntimeException("Shop not found"));

                    ChatRoom chatRoom = new ChatRoom();
                    chatRoom.setUser(user);
                    chatRoom.setShop(shop);
                    return chatRoomRepository.save(chatRoom);
                });
    }

    @Override
    @Transactional
    public ChatMessageResponse sendMessage(Long chatRoomId, Long senderId, String content, com.example.backend_service.common.MessageType messageType) {
        ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
                .orElseThrow(() -> new RuntimeException("Chat room not found"));

        // Determine sender type
        SenderType senderType;
        String senderName;
        String senderAvatar;

        if (chatRoom.getUser().getId().equals(senderId)) {
            senderType = SenderType.USER;
            senderName = chatRoom.getUser().getFullName();
            senderAvatar = chatRoom.getUser().getAvatarUrl();
        } else if (chatRoom.getShop().getOwner().getId().equals(senderId)) {
            senderType = SenderType.SHOP;
            senderName = chatRoom.getShop().getShopName();
            senderAvatar = chatRoom.getShop().getLogoUrl();
        } else {
            throw new RuntimeException("Sender is not a participant of this chat room");
        }

        ChatMessage message = new ChatMessage();
        message.setChatRoom(chatRoom);
        message.setSenderType(senderType);
        message.setSenderId(senderId);
        message.setContent(content);
        message.setIsRead(false);
        message.setMessageType(messageType != null ? messageType : com.example.backend_service.common.MessageType.TEXT);

        ChatMessage saved = chatMessageRepository.save(message);

        return ChatMessageResponse.builder()
                .id(saved.getId())
                .chatRoomId(chatRoomId)
                .senderType(senderType)
                .senderId(senderId)
                .senderName(senderName)
                .senderAvatar(senderAvatar)
                .content(content)
                .createdAt(saved.getCreatedAt())
                .isRead(false)
                .messageType(saved.getMessageType())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ChatRoomResponse> getUserChatRooms(Long userId) {
        List<ChatRoom> rooms = chatRoomRepository.findByUserIdOrderByUpdatedAtDesc(userId);
        return rooms.stream().map(room -> buildChatRoomResponse(room, userId)).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ChatRoomResponse> getShopChatRooms(Long shopId) {
        Shop shop = shopRepository.findById(shopId)
                .orElseThrow(() -> new RuntimeException("Shop not found"));
        Long ownerId = shop.getOwner().getId();
        
        List<ChatRoom> rooms = chatRoomRepository.findByShopIdOrderByUpdatedAtDesc(shopId);
        return rooms.stream().map(room -> buildChatRoomResponse(room, ownerId)).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ChatMessageResponse> getChatHistory(Long chatRoomId) {
        ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
                .orElseThrow(() -> new RuntimeException("Chat room not found"));

        List<ChatMessage> messages = chatMessageRepository.findByChatRoomIdOrderByCreatedAtAsc(chatRoomId);

        return messages.stream().map(msg -> {
            String senderName;
            String senderAvatar;
            if (msg.getSenderType() == SenderType.USER) {
                senderName = chatRoom.getUser().getFullName();
                senderAvatar = chatRoom.getUser().getAvatarUrl();
            } else {
                senderName = chatRoom.getShop().getShopName();
                senderAvatar = chatRoom.getShop().getLogoUrl();
            }

            return ChatMessageResponse.builder()
                    .id(msg.getId())
                    .chatRoomId(chatRoomId)
                    .senderType(msg.getSenderType())
                    .senderId(msg.getSenderId())
                    .senderName(senderName)
                    .senderAvatar(senderAvatar)
                    .content(msg.getContent())
                    .createdAt(msg.getCreatedAt())
                    .isRead(msg.getIsRead())
                    .messageType(msg.getMessageType())
                    .build();
        }).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void markAsRead(Long chatRoomId, Long userId) {
        chatMessageRepository.markAllAsRead(chatRoomId, userId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Long> getParticipantIds(Long chatRoomId) {
        ChatRoom room = chatRoomRepository.findById(chatRoomId)
                .orElseThrow(() -> new RuntimeException("Chat room not found"));
        return List.of(room.getUser().getId(), room.getShop().getOwner().getId());
    }

    private ChatRoomResponse buildChatRoomResponse(ChatRoom room, Long currentUserId) {
        ChatMessage lastMsg = chatMessageRepository.findLastMessageByChatRoomId(room.getId());
        Long unreadCount = chatMessageRepository.countUnreadMessages(room.getId(), currentUserId);

        return ChatRoomResponse.builder()
                .id(room.getId())
                .shopId(room.getShop().getId())
                .shopName(room.getShop().getShopName())
                .shopLogo(room.getShop().getLogoUrl())
                .userId(room.getUser().getId())
                .userName(room.getUser().getFullName())
                .userAvatar(room.getUser().getAvatarUrl())
                .lastMessage(lastMsg != null ? lastMsg.getContent() : null)
                .lastMessageTime(lastMsg != null ? lastMsg.getCreatedAt() : room.getCreatedAt())
                .unreadCount(unreadCount)
                .build();
    }
}
