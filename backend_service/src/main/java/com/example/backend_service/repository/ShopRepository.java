package com.example.backend_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.backend_service.model.auth.User;
import com.example.backend_service.model.business.Shop;

public interface ShopRepository extends JpaRepository<Shop, Long> {

    boolean existsByOwner(User user);

}
