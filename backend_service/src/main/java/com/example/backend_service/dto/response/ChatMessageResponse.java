package com.example.backend_service.dto.response;

import com.example.backend_service.common.SenderType;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessageResponse {
    private Long id;
    private Long chatRoomId;
    private SenderType senderType;
    private Long senderId;
    private String senderName;
    private String senderAvatar;
    private String content;
    private LocalDateTime createdAt;
    private Boolean isRead;
    private com.example.backend_service.common.MessageType messageType;
}
