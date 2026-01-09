package com.example.backend_service.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.backend_service.model.Address;
import com.example.backend_service.model.auth.User;

public interface AddressRepository extends JpaRepository<Address, Long>{
    List<Address> findByUser(User user);
    List<Address> findByUserAndIsDefaultTrue(User user);
}
