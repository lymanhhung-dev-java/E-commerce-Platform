package com.example.backend_service.repository;

import com.example.backend_service.model.chat.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    List<ChatMessage> findByChatRoomIdOrderByCreatedAtAsc(Long chatRoomId);

    @Query("SELECT m FROM ChatMessage m WHERE m.chatRoom.id = :chatRoomId ORDER BY m.createdAt DESC LIMIT 1")
    ChatMessage findLastMessageByChatRoomId(@Param("chatRoomId") Long chatRoomId);

    @Query("SELECT COUNT(m) FROM ChatMessage m WHERE m.chatRoom.id = :chatRoomId AND m.isRead = false AND m.senderId <> :currentUserId")
    Long countUnreadMessages(@Param("chatRoomId") Long chatRoomId, @Param("currentUserId") Long currentUserId);

    @Modifying
    @Query("UPDATE ChatMessage m SET m.isRead = true WHERE m.chatRoom.id = :chatRoomId AND m.senderId <> :currentUserId AND m.isRead = false")
    void markAllAsRead(@Param("chatRoomId") Long chatRoomId, @Param("currentUserId") Long currentUserId);
}
