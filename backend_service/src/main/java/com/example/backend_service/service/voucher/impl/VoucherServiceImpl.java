package com.example.backend_service.service.voucher.impl;

import com.example.backend_service.common.OwnerType;
import com.example.backend_service.dto.request.voucher.VoucherRequest;
import com.example.backend_service.dto.response.voucher.VoucherResponse;
import com.example.backend_service.exception.AppException;
import com.example.backend_service.exception.ErrorCode;
import com.example.backend_service.model.Voucher;
import com.example.backend_service.model.auth.User;
import com.example.backend_service.model.business.Shop;
import com.example.backend_service.repository.UserRepository;
import com.example.backend_service.repository.VoucherRepository;
import com.example.backend_service.service.voucher.VoucherService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Objects;

@Service
@RequiredArgsConstructor
@Slf4j
public class VoucherServiceImpl implements VoucherService {

    private final VoucherRepository voucherRepository;
    private final UserRepository userRepository;

    private Shop getCurrentShop() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User merchant = userRepository.findByUsername(username);
        if (merchant == null || merchant.getShop() == null) {
            throw new RuntimeException("Người dùng không có quyền hoặc chưa tạo Shop");
        }
        return merchant.getShop();
    }

    @Override
    public Page<VoucherResponse> getAllVouchers(String keyword, Pageable pageable) {
        Page<Voucher> vouchers;
        if (keyword != null && !keyword.trim().isEmpty()) {
            vouchers = voucherRepository.findByCodeContainingIgnoreCase(keyword.trim(), pageable);
        } else {
            vouchers = voucherRepository.findAll(pageable);
        }
        return vouchers.map(this::mapToResponse);
    }

    @Override
    public VoucherResponse createSystemVoucher(VoucherRequest request) {
        validateDates(request);
        if (voucherRepository.findByCode(request.getCode()).isPresent()) {
            throw new RuntimeException("Mã voucher đã tồn tại");
        }

        Voucher voucher = Voucher.builder()
                .code(request.getCode())
                .discountValue(request.getDiscountValue())
                .discountType(request.getDiscountType())
                .ownerType(OwnerType.SYSTEM)
                .minOrderValue(request.getMinOrderValue())
                .maxDiscount(request.getMaxDiscount())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .limitUsage(request.getLimitUsage() != null ? request.getLimitUsage() : 0)
                .isActive(true)
                .usedCount(0)
                .build();

        return mapToResponse(voucherRepository.save(voucher));
    }

    @Override
    public VoucherResponse updateSystemVoucher(Long id, VoucherRequest request) {
        validateDates(request);
        Voucher voucher = voucherRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy voucher"));

        if (!voucher.getOwnerType().equals(OwnerType.SYSTEM)) {
            throw new RuntimeException("Chỉ được phép cập nhật voucher của hệ thống");
        }

        updateVoucherFields(voucher, request);
        return mapToResponse(voucherRepository.save(voucher));
    }

    @Override
    public void deleteSystemVoucher(Long id) {
        Voucher voucher = voucherRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy voucher"));
        if (!voucher.getOwnerType().equals(OwnerType.SYSTEM)) {
            throw new RuntimeException("Chỉ được phép xóa voucher của hệ thống");
        }
        voucherRepository.delete(voucher);
    }

    @Override
    public void toggleSystemVoucherStatus(Long id) {
        Voucher voucher = voucherRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy voucher"));
        if (!voucher.getOwnerType().equals(OwnerType.SYSTEM)) {
            throw new RuntimeException("Chỉ được phép thay đổi trạng thái voucher của hệ thống");
        }
        voucher.setIsActive(!voucher.getIsActive());
        voucherRepository.save(voucher);
    }

    @Override
    public Page<VoucherResponse> getShopVouchers(String keyword, Pageable pageable) {
        Long shopId = getCurrentShop().getId();
        Page<Voucher> vouchers;
        if (keyword != null && !keyword.trim().isEmpty()) {
            vouchers = voucherRepository.findByOwnerTypeAndShopIdAndCodeContainingIgnoreCase(OwnerType.SHOP, shopId, keyword.trim(), pageable);
        } else {
            vouchers = voucherRepository.findByOwnerTypeAndShopId(OwnerType.SHOP, shopId, pageable);
        }
        return vouchers.map(this::mapToResponse);
    }

    @Override
    public VoucherResponse createShopVoucher(VoucherRequest request) {
        Long shopId = getCurrentShop().getId();
        validateDates(request);
        if (voucherRepository.findByCode(request.getCode()).isPresent()) {
            throw new RuntimeException("Mã voucher đã tồn tại");
        }

        Voucher voucher = Voucher.builder()
                .code(request.getCode())
                .discountValue(request.getDiscountValue())
                .discountType(request.getDiscountType())
                .ownerType(OwnerType.SHOP)
                .shopId(shopId)
                .minOrderValue(request.getMinOrderValue())
                .maxDiscount(request.getMaxDiscount())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .limitUsage(request.getLimitUsage() != null ? request.getLimitUsage() : 0)
                .isActive(true)
                .usedCount(0)
                .build();

        return mapToResponse(voucherRepository.save(voucher));
    }

    @Override
    public VoucherResponse updateShopVoucher(Long id, VoucherRequest request) {
        Long shopId = getCurrentShop().getId();
        validateDates(request);
        Voucher voucher = voucherRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy voucher"));

        if (!voucher.getOwnerType().equals(OwnerType.SHOP) || !Objects.equals(voucher.getShopId(), shopId)) {
            throw new RuntimeException("Chỉ được phép cập nhật voucher của Shop bạn");
        }

        updateVoucherFields(voucher, request);
        return mapToResponse(voucherRepository.save(voucher));
    }

    @Override
    public void deleteShopVoucher(Long id) {
        Long shopId = getCurrentShop().getId();
        Voucher voucher = voucherRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy voucher"));
        
        if (!voucher.getOwnerType().equals(OwnerType.SHOP) || !Objects.equals(voucher.getShopId(), shopId)) {
            throw new RuntimeException("Chỉ được phép xóa voucher của Shop bạn");
        }
        voucherRepository.delete(voucher);
    }

    @Override
    public void toggleShopVoucherStatus(Long id) {
        Long shopId = getCurrentShop().getId();
        Voucher voucher = voucherRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy voucher"));
        
        if (!voucher.getOwnerType().equals(OwnerType.SHOP) || !Objects.equals(voucher.getShopId(), shopId)) {
            throw new RuntimeException("Chỉ được phép thay đổi trạng thái voucher của Shop bạn");
        }
        voucher.setIsActive(!voucher.getIsActive());
        voucherRepository.save(voucher);
    }

    @Override
    public VoucherResponse getVoucherById(Long id, boolean isMerchant) {
        Voucher voucher = voucherRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy voucher"));
        
        if (isMerchant) {
            // If merchant request, can only view SYSTEM vouchers or their own SHOP vouchers
            if (voucher.getOwnerType().equals(OwnerType.SHOP)) {
                Long shopId = getCurrentShop().getId();
                if (!Objects.equals(voucher.getShopId(), shopId)) {
                    throw new RuntimeException("Không được phép xem voucher của Shop khác");
                }
            }
        }
        return mapToResponse(voucher);
    }

    @Override
    public java.util.List<VoucherResponse> getShopPublicVouchers(Long shopId) {
        java.time.LocalDateTime now = java.time.LocalDateTime.now();
        java.util.List<Voucher> vouchers = voucherRepository.findByOwnerTypeAndShopIdAndIsActiveTrueAndEndDateAfter(OwnerType.SHOP, shopId, now);
        
        return vouchers.stream()
                .filter(v -> v.getStartDate().isBefore(now) || v.getStartDate().isEqual(now))
                .filter(v -> v.getLimitUsage() == null || v.getLimitUsage() == 0 || v.getUsedCount() < v.getLimitUsage())
                .map(this::mapToResponse)
                .collect(java.util.stream.Collectors.toList());
    }

    private void updateVoucherFields(Voucher voucher, VoucherRequest request) {
        // If code changed, check if exists
        if (!voucher.getCode().equals(request.getCode()) && voucherRepository.findByCode(request.getCode()).isPresent()) {
            throw new RuntimeException("Mã voucher mới đã tồn tại");
        }
        voucher.setCode(request.getCode());
        voucher.setDiscountValue(request.getDiscountValue());
        voucher.setDiscountType(request.getDiscountType());
        voucher.setMinOrderValue(request.getMinOrderValue());
        voucher.setMaxDiscount(request.getMaxDiscount());
        voucher.setStartDate(request.getStartDate());
        voucher.setEndDate(request.getEndDate());
        voucher.setLimitUsage(request.getLimitUsage() != null ? request.getLimitUsage() : 0);
    }

    private void validateDates(VoucherRequest request) {
        if (request.getEndDate().isBefore(request.getStartDate())) {
            throw new RuntimeException("Ngày kết thúc phải sau ngày bắt đầu");
        }
    }

    private VoucherResponse mapToResponse(Voucher voucher) {
        return VoucherResponse.builder()
                .id(voucher.getId())
                .code(voucher.getCode())
                .discountValue(voucher.getDiscountValue())
                .discountType(voucher.getDiscountType())
                .ownerType(voucher.getOwnerType())
                .shopId(voucher.getShopId())
                .minOrderValue(voucher.getMinOrderValue())
                .maxDiscount(voucher.getMaxDiscount())
                .startDate(voucher.getStartDate())
                .endDate(voucher.getEndDate())
                .isActive(voucher.getIsActive())
                .limitUsage(voucher.getLimitUsage())
                .usedCount(voucher.getUsedCount())
                .createdAt(voucher.getCreatedAt())
                .updatedAt(voucher.getUpdatedAt())
                .build();
    }
}
