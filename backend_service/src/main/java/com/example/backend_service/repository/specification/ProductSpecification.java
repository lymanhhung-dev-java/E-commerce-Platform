package com.example.backend_service.repository.specification;

import java.math.BigDecimal;

import org.springframework.data.jpa.domain.Specification;

import com.example.backend_service.model.product.Category;
import com.example.backend_service.model.product.Product;

import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;

public class ProductSpecification {

    public static Specification<Product> hasName(String name) {
        return (root, query, criteriaBuilder) -> {
            if (name == null || name.trim().isEmpty())
                return null;
            return criteriaBuilder.like(criteriaBuilder.lower(root.get("name")), "%" + name.toLowerCase() + "%");
        };
    }

    public static Specification<Product> hasCategory(Long categoryId) {
        return (root, query, criteriaBuilder) -> {
            if (categoryId == null)
                return null;

            Join<Product, Category> categoryJoin = root.join("category", JoinType.LEFT);

            Predicate isDirect = criteriaBuilder.equal(categoryJoin.get("id"), categoryId);

            Predicate isChild = criteriaBuilder.equal(categoryJoin.get("parent").get("id"), categoryId);
            return criteriaBuilder.or(isDirect, isChild);
        };
    }

    public static Specification<Product> hasPriceRange(BigDecimal minPrice, BigDecimal maxPrice) {
        return (root, query, criteriaBuilder) -> {
            if (minPrice == null && maxPrice == null)
                return null;

            if (minPrice != null && maxPrice != null) {
                return criteriaBuilder.between(root.get("price"), minPrice, maxPrice);
            } else if (minPrice != null) {
                return criteriaBuilder.greaterThanOrEqualTo(root.get("price"), minPrice);
            } else {
                return criteriaBuilder.lessThanOrEqualTo(root.get("price"), maxPrice);
            }
        };
    }

    public static Specification<Product> hasShop(Long shopId) {
        return (root, query, criteriaBuilder) -> {
            if (shopId == null)
                return null;
            return criteriaBuilder.equal(root.get("shop").get("id"), shopId);
        };
    }

    public static Specification<Product> isActive() {
        return (root, query, criteriaBuilder) -> criteriaBuilder.isTrue(root.get("isActive"));
    }

}
