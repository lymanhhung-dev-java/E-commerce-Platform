package com.example.backend_service.dto.response;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatRoomResponse {
    private Long id;
    private Long shopId;
    private String shopName;
    private String shopLogo;
    private Long userId;
    private String userName;
    private String userAvatar;
    private String lastMessage;
    private LocalDateTime lastMessageTime;
    private Long unreadCount;
}
