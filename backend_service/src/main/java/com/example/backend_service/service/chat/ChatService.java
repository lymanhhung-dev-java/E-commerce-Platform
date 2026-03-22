package com.example.backend_service.service.chat;

import com.example.backend_service.dto.response.ChatMessageResponse;
import com.example.backend_service.dto.response.ChatRoomResponse;
import com.example.backend_service.model.chat.ChatRoom;

import java.util.List;

public interface ChatService {

    ChatRoom getOrCreateChatRoom(Long userId, Long shopId);

    ChatMessageResponse sendMessage(Long chatRoomId, Long senderId, String content, com.example.backend_service.common.MessageType messageType);

    List<ChatRoomResponse> getUserChatRooms(Long userId);

    List<ChatRoomResponse> getShopChatRooms(Long shopId);

    List<ChatMessageResponse> getChatHistory(Long chatRoomId);

    void markAsRead(Long chatRoomId, Long userId);

    List<Long> getParticipantIds(Long chatRoomId);
}
