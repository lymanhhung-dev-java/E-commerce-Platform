package com.example.backend_service.service.voucher.impl;

import com.example.backend_service.dto.response.voucher.UserVoucherResponse;
import com.example.backend_service.exception.AppException;
import com.example.backend_service.model.UserVoucher;
import com.example.backend_service.model.Voucher;
import com.example.backend_service.model.auth.User;
import com.example.backend_service.repository.UserRepository;
import com.example.backend_service.repository.UserVoucherRepository;
import com.example.backend_service.repository.VoucherRepository;
import com.example.backend_service.service.voucher.UserVoucherService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class UserVoucherServiceImpl implements UserVoucherService {

    private final UserVoucherRepository userVoucherRepository;
    private final VoucherRepository voucherRepository;
    private final UserRepository userRepository;

    private User getCurrentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByUsername(username);
    }

    @Override
    @Transactional
    public UserVoucherResponse saveVoucherByCode(String code) {
        User user = getCurrentUser();

        Voucher voucher = voucherRepository.findByCode(code)
                .orElseThrow(() -> new AppException("Mã giảm giá không tồn tại: " + code));

        if (!voucher.getIsActive()) {
            throw new AppException("Mã giảm giá đã bị khóa");
        }

        LocalDateTime now = LocalDateTime.now();
        if (now.isAfter(voucher.getEndDate())) {
            throw new AppException("Mã giảm giá đã hết hạn");
        }

        if (voucher.getLimitUsage() != null && voucher.getUsedCount() >= voucher.getLimitUsage()) {
            throw new AppException("Mã giảm giá đã hết lượt sử dụng");
        }

        if (userVoucherRepository.existsByUserAndVoucher(user, voucher)) {
            throw new AppException("Bạn đã lưu mã giảm giá này rồi");
        }

        UserVoucher userVoucher = new UserVoucher();
        userVoucher.setUser(user);
        userVoucher.setVoucher(voucher);
        // isUsed is default false
        
        userVoucher = userVoucherRepository.save(userVoucher);
        return mapToResponse(userVoucher);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UserVoucherResponse> getMySavedVouchers(Pageable pageable) {
        User user = getCurrentUser();
        return userVoucherRepository.findByUserAndIsUsedFalse(user, pageable)
                .map(this::mapToResponse);
    }

    private UserVoucherResponse mapToResponse(UserVoucher uv) {
        Voucher v = uv.getVoucher();
        return UserVoucherResponse.builder()
                .id(uv.getId())
                .voucherId(v.getId())
                .code(v.getCode())
                .discountValue(v.getDiscountValue())
                .discountType(v.getDiscountType())
                .ownerType(v.getOwnerType())
                .shopId(v.getShopId())
                .minOrderValue(v.getMinOrderValue())
                .maxDiscount(v.getMaxDiscount())
                .startDate(v.getStartDate())
                .endDate(v.getEndDate())
                .isUsed(uv.getIsUsed())
                .savedAt(uv.getCreatedAt())
                .build();
    }
}
