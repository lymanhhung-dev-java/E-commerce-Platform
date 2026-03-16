package com.example.backend_service.repository;

import com.example.backend_service.model.chat.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {

    Optional<ChatRoom> findByUserIdAndShopId(Long userId, Long shopId);

    List<ChatRoom> findByUserIdOrderByUpdatedAtDesc(Long userId);

    List<ChatRoom> findByShopIdOrderByUpdatedAtDesc(Long shopId);
}
