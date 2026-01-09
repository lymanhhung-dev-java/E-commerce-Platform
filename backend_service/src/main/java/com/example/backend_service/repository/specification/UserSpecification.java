package com.example.backend_service.repository.specification;

import org.springframework.data.jpa.domain.Specification;
import com.example.backend_service.common.UserStatus;
import com.example.backend_service.model.auth.User;
import jakarta.persistence.criteria.Predicate;

public class UserSpecification {

    // 1. Tìm kiếm chung (Username, FullName, Email, Phone)
    public static Specification<User> hasKeyword(String keyword) {
        return (root, query, cb) -> {
            if (keyword == null || keyword.trim().isEmpty()) return null;
            String likePattern = "%" + keyword.toLowerCase() + "%";
            
            Predicate hasUsername = cb.like(cb.lower(root.get("username")), likePattern);
            Predicate hasEmail = cb.like(cb.lower(root.get("email")), likePattern);
            Predicate hasFullName = cb.like(cb.lower(root.get("fullName")), likePattern);
            Predicate hasPhone = cb.like(root.get("phoneNumber"), likePattern);

            return cb.or(hasUsername, hasEmail, hasFullName, hasPhone);
        };
    }

    // 2. Lọc theo trạng thái (Active / Inactive / Blocked)
    public static Specification<User> hasStatus(UserStatus status) {
        return (root, query, cb) -> {
            if (status == null) return null;
            return cb.equal(root.get("status"), status);
        };
    }

    // 3. Lọc theo Shop Owner (Đã mở shop hay chưa)
    public static Specification<User> isShopOwner(Boolean isShopOwner) {
        return (root, query, cb) -> {
            if (isShopOwner == null) return null;
            
            if (isShopOwner) {
                return cb.isNotNull(root.get("shop"));
            } 
            // Chưa mở shop
            else {
                return cb.isNull(root.get("shop"));
            }
        };
    }
}