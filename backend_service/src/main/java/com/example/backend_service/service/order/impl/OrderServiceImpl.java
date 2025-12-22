package com.example.backend_service.service.order.impl;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.example.backend_service.common.OrderStatus;
import com.example.backend_service.dto.response.order.OrderResponse;
import com.example.backend_service.exception.AppException;
import com.example.backend_service.model.auth.User;
import com.example.backend_service.model.business.Shop;
import com.example.backend_service.model.order.Order;
import com.example.backend_service.repository.OrderRepository;
import com.example.backend_service.repository.UserRepository;
import com.example.backend_service.service.order.OrderService;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j(topic = "ORDER-SERVICE")
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final UserRepository userRepository;    
    private final OrderRepository orderRepository;
    
     private User getCurrentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByUsername(username);
    }

    @Override

    public Page<OrderResponse> getOrdersByShop(Pageable pageable) {
        User user = getCurrentUser();
        Shop shop = user.getShop();

        if (shop == null) {
            throw new AppException("Bạn chưa đăng ký Shop");
        }
        return orderRepository.findByShop(shop, pageable)
                .map(OrderResponse::fromEntity);
    }

    @Override
    @Transactional
    public void updateOrderStatus(Long orderId, OrderStatus status) {
        User user = getCurrentUser();
        Shop shop = user.getShop();

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new AppException("Đơn hàng không tồn tại"));

        if (!order.getShop().getId().equals(shop.getId())) {
            throw new AppException("Bạn không có quyền xử lý đơn hàng này");
        }

        order.setStatus(status);
        orderRepository.save(order);
    }
    }
    
