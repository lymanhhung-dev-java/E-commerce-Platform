package com.example.backend_service.service.voucher;

import com.example.backend_service.dto.response.voucher.UserVoucherResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UserVoucherService {
    UserVoucherResponse saveVoucherByCode(String code);
    Page<UserVoucherResponse> getMySavedVouchers(Pageable pageable);
}
