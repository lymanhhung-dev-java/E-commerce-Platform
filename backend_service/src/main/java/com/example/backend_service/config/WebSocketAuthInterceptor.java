package com.example.backend_service.config;

import com.example.backend_service.common.TokenType;
import com.example.backend_service.service.auth.JwtService;
import com.example.backend_service.service.auth.UserServiceDetail;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j(topic = "WEBSOCKET-AUTH")
public class WebSocketAuthInterceptor implements ChannelInterceptor {

    private final JwtService jwtService;
    private final UserServiceDetail userServiceDetail;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {
            List<String> authHeaders = accessor.getNativeHeader("Authorization");

            if (authHeaders != null && !authHeaders.isEmpty()) {
                String authHeader = authHeaders.get(0);

                if (authHeader.startsWith("Bearer ")) {
                    String token = authHeader.substring(7);

                    try {
                        String username = jwtService.extractUsername(token, TokenType.ACCESS_TOKEN);
                        log.info("WebSocket CONNECT - authenticated user: {}", username);

                        UserDetails userDetails = userServiceDetail.userDetailsService().loadUserByUsername(username);

                        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                                userDetails, null, userDetails.getAuthorities());

                        accessor.setUser(authToken);
                        SecurityContextHolder.getContext().setAuthentication(authToken);
                    } catch (Exception e) {
                        log.error("WebSocket authentication failed: {}", e.getMessage());
                    }
                }
            }
        }

        return message;
    }
}
