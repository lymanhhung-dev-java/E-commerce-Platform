package com.example.backend_service.service.business.impl;

import java.util.Optional;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.example.backend_service.common.ShopStatus;
import com.example.backend_service.dto.request.business.RegisterShopRequest;
import com.example.backend_service.exception.AppException;
import com.example.backend_service.model.auth.User;
import com.example.backend_service.model.business.Shop;
import com.example.backend_service.repository.RoleRepository;
import com.example.backend_service.repository.ShopRepository;
import com.example.backend_service.repository.UserRepository;
import com.example.backend_service.service.business.ShopService;
import com.example.backend_service.model.auth.Role;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j(topic = "SHOP-SERVICE")
@RequiredArgsConstructor
public class ShopServiceImpl  implements ShopService{

    private final ShopRepository shopRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    @Override
    @Transactional
    public Shop registerShop(RegisterShopRequest req) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userRepository.findByUsername(username);
        if (shopRepository.existsByOwner(currentUser)){
            throw new AppException("Bạn đã đăng ký Shop rồi!");
        }

        Shop shop = new Shop();
        shop.setShopName(req.getShopName());
        shop.setDescription(req.getDescription());
        shop.setLogoUrl(req.getLogoUrl());
        shop.setOwner(currentUser);
        shop.setStatus(ShopStatus.PENDING);
        shop.setRating(5.0);
        return shopRepository.save(shop);

    }

    @Override
    public void approveShope(Long shopId, Boolean isApproved) {
        
        Shop shop = shopRepository.findById(shopId)
        .orElseThrow(() -> new AppException("Shop không tồn tại!"));
        if (isApproved){
            shop.setStatus(ShopStatus.ACTIVE);
            User owner = shop.getOwner();
            Role sellerRole = roleRepository.findByName("SELLER")
                .orElseThrow(() -> new AppException("Role SELLER not found"));
            owner.getRoles().add(sellerRole);
            userRepository.save(owner);
            
        }else{
            shop.setStatus(ShopStatus.REJECTED);
        }
        shopRepository.save(shop);
    }
    
}
