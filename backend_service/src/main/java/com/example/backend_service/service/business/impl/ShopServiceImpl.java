package com.example.backend_service.service.business.impl;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import com.example.backend_service.common.ShopStatus;
import com.example.backend_service.dto.request.business.RegisterShopRequest;
import com.example.backend_service.dto.request.business.UpdateShopRequest;
import com.example.backend_service.dto.response.business.ShopResponse;
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
public class ShopServiceImpl implements ShopService {

    private final ShopRepository shopRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    private User getCurrentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByUsername(username);
    }

    @Override
    @Transactional
    public Shop registerShop(RegisterShopRequest req) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userRepository.findByUsername(username);
        if (shopRepository.existsByOwner(currentUser)) {
            throw new AppException("Bạn đã đăng ký Shop rồi!");
        }

        Shop shop = new Shop();
        shop.setShopName(req.getShopName());
        shop.setDescription(req.getDescription());
        shop.setLogoUrl(req.getLogoUrl());
        shop.setOwner(currentUser);
        shop.setAddress(req.getAddress());
        shop.setStatus(ShopStatus.PENDING);
        shop.setRating(5.0);
        return shopRepository.save(shop);

    }

    @Override
    public void approveShope(Long shopId, Boolean isApproved) {

        Shop shop = shopRepository.findById(shopId)
                .orElseThrow(() -> new AppException("Shop không tồn tại!"));
        if (isApproved) {
            shop.setStatus(ShopStatus.ACTIVE);
            User owner = shop.getOwner();
            Role sellerRole = roleRepository.findByName("SELLER")
                    .orElseThrow(() -> new AppException("Role SELLER not found"));
            owner.getRoles().add(sellerRole);
            userRepository.save(owner);

        } else {
            shop.setStatus(ShopStatus.REJECTED);
        }
        shopRepository.save(shop);
    }

    @Override
    public Page<ShopResponse> getPendingShopRequests(Pageable pageable) {
        return shopRepository.findByStatus(ShopStatus.PENDING, pageable)
                .map(ShopResponse::fromEntity);
    }

    @Override
    public ShopResponse getCurrentShop() {
        User user = getCurrentUser();
        Shop shop = shopRepository.findByOwner(user)
                .orElseThrow(() -> new AppException("Bạn chưa đăng ký Shop"));
        return ShopResponse.fromEntity(shop);

    }

    @Override
    public Page<ShopResponse> getShopsForAdmin(String keyword, ShopStatus status, Pageable pageable) {
        return shopRepository.findAllByKeywordAndStatus(keyword, status, pageable)
                .map(ShopResponse::fromEntity);
    }

    @Override
    public void banShop(Long shopId) {
        Shop shop = shopRepository.findById(shopId)
                .orElseThrow(() -> new AppException("Shop không tồn tại"));
        shop.setStatus(ShopStatus.BANNED);
        shopRepository.save(shop);
    }

    @Override
    @Transactional
    public Shop updateShopRegistration(RegisterShopRequest req) {
        User user = getCurrentUser();

        Shop shop = shopRepository.findByOwner(user)
                .orElseThrow(() -> new AppException("Bạn chưa có shop nào"));

        // 2. Chỉ cho phép sửa khi chưa Active (hoặc đã bị từ chối)
        if (shop.getStatus() == ShopStatus.ACTIVE) {
            throw new AppException("Shop đang hoạt động, vui lòng dùng chức năng Cập nhật hồ sơ trong trang quản lý.");
        }

        shop.setShopName(req.getShopName());
        shop.setAddress(req.getAddress());
        shop.setDescription(req.getDescription());
        if (req.getLogoUrl() != null && !req.getLogoUrl().isEmpty()) {
            shop.setLogoUrl(req.getLogoUrl());
        }
        shop.setStatus(ShopStatus.PENDING);

        return shopRepository.save(shop);
    }

    @Override
    @Transactional
    public Shop updateShopInfo(UpdateShopRequest req) {
        User user = getCurrentUser();
        Shop shop = shopRepository.findByOwner(user)
                .orElseThrow(() -> new AppException("Bạn chưa có shop nào"));

        if (shop.getStatus() == ShopStatus.BANNED || shop.getStatus() == ShopStatus.REJECTED) {
            throw new AppException("Shop đang bị khóa hoặc từ chối, không thể chỉnh sửa thông tin hoạt động.");
        }

        shop.setShopName(req.getShopName());
        shop.setAddress(req.getAddress());
        shop.setDescription(req.getDescription());
        if (req.getLogoUrl() != null && !req.getLogoUrl().isEmpty()) {
            shop.setLogoUrl(req.getLogoUrl());
        }

        return shopRepository.save(shop);
    }

    @Override
    public ShopResponse getShopById(Long id) {
        Shop shop = shopRepository.findById(id)
                .orElseThrow(() -> new AppException("Shop not found"));
        return ShopResponse.fromEntity(shop);
    }
}
