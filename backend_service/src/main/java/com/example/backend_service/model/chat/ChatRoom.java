package com.example.backend_service.model.chat;

import com.example.backend_service.model.AbstractEntity;
import com.example.backend_service.model.auth.User;
import com.example.backend_service.model.business.Shop;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "chat_rooms", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"user_id", "shop_id"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ChatRoom extends AbstractEntity<Long> {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shop_id", nullable = false)
    private Shop shop;

    @OneToMany(mappedBy = "chatRoom", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("createdAt ASC")
    private List<ChatMessage> messages = new ArrayList<>();
}
