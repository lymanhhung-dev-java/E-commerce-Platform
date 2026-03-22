package com.example.backend_service.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SendOrderMessageRequest {
    private Long chatRoomId;
    private Long orderId;
}
