package com.example.backend_service.service.wallet.impl;

import com.example.backend_service.common.WithdrawalStatus;
import com.example.backend_service.dto.request.wallet.AdminUpdateWithdrawalRequest;
import com.example.backend_service.model.business.Shop;
import com.example.backend_service.model.business.Withdrawal;
import com.example.backend_service.repository.ShopRepository;
import com.example.backend_service.repository.WithdrawalRepository;
import com.example.backend_service.service.wallet.AdminWithdrawalService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminWithdrawalServiceImpl implements AdminWithdrawalService {

    private final WithdrawalRepository withdrawalRepository;
    private final ShopRepository shopRepository;

    @Override
    public Page<Withdrawal> getAllWithdrawals(String status, Pageable pageable) {
        if (status != null && !status.isEmpty()) {
            try {
                WithdrawalStatus withdrawalStatus = WithdrawalStatus.valueOf(status.toUpperCase());
                return withdrawalRepository.findByStatus(withdrawalStatus, pageable);
            } catch (IllegalArgumentException e) {
                return withdrawalRepository.findAll(pageable);
            }
        }
        return withdrawalRepository.findAll(pageable);
    }

    @Override
    @Transactional 
    public void updateWithdrawalStatus(Long withdrawalId, AdminUpdateWithdrawalRequest request) {
        Withdrawal withdrawal = withdrawalRepository.findById(withdrawalId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy yêu cầu rút tiền!"));

        // Chỉ được xử lý đơn đang PENDING
        if (withdrawal.getStatus() != WithdrawalStatus.PENDING) {
            throw new RuntimeException("Yêu cầu này đã được xử lý trước đó rồi!");
        }

        WithdrawalStatus newStatus = request.getStatus();

        if (newStatus == WithdrawalStatus.APPROVED) {
            // --- TRƯỜNG HỢP 1: CHẤP NHẬN ---
            // Tiền đã trừ lúc Merchant tạo lệnh, nên ở đây chỉ cần update trạng thái
            // (Thực tế sẽ gọi API ngân hàng ở bước này để chuyển tiền thật)
            withdrawal.setStatus(WithdrawalStatus.APPROVED);
        
        } else if (newStatus == WithdrawalStatus.REJECTED) {
            // --- TRƯỜNG HỢP 2: TỪ CHỐI ---
            // Phải HOÀN TIỀN lại vào ví cho Shop
            Shop shop = withdrawal.getShop();
            shop.setBalance(shop.getBalance().add(withdrawal.getAmount())); // Cộng lại tiền
            shopRepository.save(shop);

            withdrawal.setStatus(WithdrawalStatus.REJECTED);
            // Lưu lý do từ chối (bạn cần thêm trường rejectReason vào Entity Withdrawal nếu muốn lưu)
            // withdrawal.setRejectReason(request.getRejectReason());
        } else {
            throw new RuntimeException("Trạng thái không hợp lệ!");
        }

        withdrawalRepository.save(withdrawal);
    }
}