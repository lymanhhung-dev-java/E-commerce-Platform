package com.example.backend_service.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.backend_service.common.OrderStatus;
import com.example.backend_service.model.order.Order;
import com.example.backend_service.model.order.OrderItem;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

        List<OrderItem> findByOrder(Order order);

        @Query("SELECT oi FROM OrderItem oi " +
                        "WHERE oi.product.id = :productId " +
                        "AND oi.order.user.id = :userId " +
                        "AND oi.order.status = :status")
        List<OrderItem> findDeliveredItemsByProductAndUser(
                        @Param("productId") Long productId,
                        @Param("userId") Long userId,
                        @Param("status") OrderStatus status);
}
