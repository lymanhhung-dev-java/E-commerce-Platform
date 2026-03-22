package com.example.backend_service.service.voucher;

import com.example.backend_service.dto.request.voucher.VoucherRequest;
import com.example.backend_service.dto.response.voucher.VoucherResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface VoucherService {

    // Admin methods
    Page<VoucherResponse> getAllVouchers(String keyword, Pageable pageable);
    VoucherResponse createSystemVoucher(VoucherRequest request);
    VoucherResponse updateSystemVoucher(Long id, VoucherRequest request);
    void deleteSystemVoucher(Long id);
    void toggleSystemVoucherStatus(Long id);

    // Merchant methods
    Page<VoucherResponse> getShopVouchers(String keyword, Pageable pageable);
    VoucherResponse createShopVoucher(VoucherRequest request);
    VoucherResponse updateShopVoucher(Long id, VoucherRequest request);
    void deleteShopVoucher(Long id);
    void toggleShopVoucherStatus(Long id);

    // Shared
    VoucherResponse getVoucherById(Long id, boolean isMerchant);
}
