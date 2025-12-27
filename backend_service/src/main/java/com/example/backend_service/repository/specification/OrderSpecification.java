package com.example.backend_service.repository.specification;

import org.springframework.data.jpa.domain.Specification;
import com.example.backend_service.common.OrderStatus;
import com.example.backend_service.model.auth.User;
import com.example.backend_service.model.order.Order;
import com.example.backend_service.model.order.OrderItem;
import com.example.backend_service.model.product.Product;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;

public class OrderSpecification {

    // 1. Bắt buộc phải là đơn của User đang đăng nhập
    public static Specification<Order> hasUser(User user) {
        return (root, query, cb) -> cb.equal(root.get("user"), user);
    }

    // 2. Lọc theo trạng thái (nếu có chọn)
    public static Specification<Order> hasStatus(OrderStatus status) {
        return (root, query, cb) -> {
            if (status == null) return null;
            return cb.equal(root.get("status"), status);
        };
    }

    // 3. Tìm kiếm: Theo ID đơn hàng HOẶC Tên sản phẩm bên trong
    public static Specification<Order> containsKeyword(String keyword) {
        return (root, query, cb) -> {
            if (keyword == null || keyword.trim().isEmpty()) return null;
            
            String likePattern = "%" + keyword.toLowerCase() + "%";

            // Tìm theo ID đơn hàng (ép kiểu ID sang String để so sánh like)
            var idPredicate = cb.like(root.get("id").as(String.class), likePattern);

            // Join bảng Order -> OrderItem -> Product để tìm theo tên sản phẩm
            Join<Order, OrderItem> items = root.join("orderItems", JoinType.LEFT);
            Join<OrderItem, Product> product = items.join("product", JoinType.LEFT);
            var productNamePredicate = cb.like(cb.lower(product.get("name")), likePattern);

            // Logic: ID khớp HOẶC Tên sản phẩm khớp
            return cb.or(idPredicate, productNamePredicate);
        };
    }
}