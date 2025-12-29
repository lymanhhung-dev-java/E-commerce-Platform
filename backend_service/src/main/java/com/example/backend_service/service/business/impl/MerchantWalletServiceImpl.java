package com.example.backend_service.service.business.impl;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.example.backend_service.common.WithdrawalStatus;
import com.example.backend_service.dto.request.business.WithdrawRequest;
import com.example.backend_service.dto.response.business.WalletOverviewResponse;
import com.example.backend_service.model.auth.User;
import com.example.backend_service.model.business.Shop;
import com.example.backend_service.model.business.Withdrawal;
import com.example.backend_service.repository.OrderRepository;
import com.example.backend_service.repository.ShopRepository;
import com.example.backend_service.repository.UserRepository;
import com.example.backend_service.repository.WithdrawalRepository;
import com.example.backend_service.service.business.MerchantWalletService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MerchantWalletServiceImpl implements MerchantWalletService  {
    private final ShopRepository shopRepository;
    private final WithdrawalRepository withdrawalRepository;
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;

    private Shop getCurrentShop() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username);
        if (user == null || user.getShop() == null) {
            throw new RuntimeException("Tài khoản này chưa đăng ký Shop hoặc không tồn tại!");
        }
        return user.getShop();
    }

    @Override
    public WalletOverviewResponse getWalletOverview() {
        Shop shop = getCurrentShop();
        Long shopId = shop.getId();

        BigDecimal totalRevenue = orderRepository.sumTotalRevenueByShop(shopId);
        if (totalRevenue == null) totalRevenue = BigDecimal.ZERO;

        LocalDate now = LocalDate.now();
        BigDecimal thisMonthRevenue = orderRepository.sumRevenueByMonth(shopId, now.getMonthValue(), now.getYear());
        if (thisMonthRevenue == null) thisMonthRevenue = BigDecimal.ZERO;

        BigDecimal currentBalance = shop.getBalance();
        if (currentBalance == null) currentBalance = BigDecimal.ZERO;

        return WalletOverviewResponse.builder()
                .totalRevenue(totalRevenue)
                .periodRevenue(thisMonthRevenue)
                .currentBalance(currentBalance)
                .build();
    }

    @Override
    @Transactional 
    public void requestWithdraw(WithdrawRequest request) {
        Shop shop = getCurrentShop();

        // Kiểm tra số dư
        BigDecimal currentBalance = shop.getBalance() != null ? shop.getBalance() : BigDecimal.ZERO;
        if (currentBalance.compareTo(request.getAmount()) < 0) {
            throw new RuntimeException("Số dư hiện tại (" + currentBalance + ") không đủ để rút " + request.getAmount());
        }

        shop.setBalance(currentBalance.subtract(request.getAmount()));
        shopRepository.save(shop);

        // 2. Tạo bản ghi lịch sử rút tiền
        Withdrawal withdrawal = new Withdrawal();
        withdrawal.setShop(shop);
        withdrawal.setAmount(request.getAmount());
        withdrawal.setBankName("MB Bank");
        withdrawal.setAccountNumber(request.getAccountNumber());
        withdrawal.setAccountName(request.getAccountName()); 
        withdrawal.setStatus(WithdrawalStatus.PENDING);

        withdrawalRepository.save(withdrawal);
    }

    @Override
    public Page<Withdrawal> getWithdrawalHistory(Pageable pageable) {
        Shop shop = getCurrentShop();
        return withdrawalRepository.findByShop(shop, pageable);
    }
}
