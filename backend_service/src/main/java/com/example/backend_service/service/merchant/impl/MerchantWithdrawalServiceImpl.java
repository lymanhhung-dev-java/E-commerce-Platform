package com.example.backend_service.service.merchant.impl;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.backend_service.dto.response.wallet.WithdrawalResponse;
import com.example.backend_service.exception.AppException;
import com.example.backend_service.model.auth.User;
import com.example.backend_service.model.business.Shop;
import com.example.backend_service.repository.UserRepository;
import com.example.backend_service.repository.WithdrawalRepository;
import com.example.backend_service.service.merchant.MerchantWithdrawalService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j(topic = "MERCHANT-WITHDRAWAL-SERVICE")
public class MerchantWithdrawalServiceImpl implements MerchantWithdrawalService {

    private final WithdrawalRepository withdrawalRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public Page<WithdrawalResponse> getMyWithdrawalHistory(Pageable pageable) {
        String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userRepository.findByUsername(currentUsername);

        if (currentUser == null) {
            throw new AppException("User not found");
        }

        Shop shop = currentUser.getShop();
        if (shop == null) {
            throw new AppException("User does not have a shop");
        }

        log.info("Fetching withdrawal history for shop: {}", shop.getShopName());

        return withdrawalRepository.findByShop(shop, pageable)
                .map(WithdrawalResponse::fromEntity);
    }
}
