package com.example.backend_service.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChatMessageRequest {
    private Long chatRoomId;
    private Long shopId;
    private String content;
}
