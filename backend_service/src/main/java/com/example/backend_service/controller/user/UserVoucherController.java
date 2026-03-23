package com.example.backend_service.controller.user;


import com.example.backend_service.dto.response.voucher.UserVoucherResponse;
import com.example.backend_service.service.voucher.UserVoucherService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user/vouchers")
@RequiredArgsConstructor
@CrossOrigin("*")
public class UserVoucherController {

    private final UserVoucherService userVoucherService;

    @PostMapping("/save/{code}")
    public ResponseEntity<UserVoucherResponse> saveVoucher(@PathVariable String code) {
        return ResponseEntity.ok(userVoucherService.saveVoucherByCode(code));
    }

    @GetMapping
    public ResponseEntity<Page<UserVoucherResponse>> getMyVouchers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(userVoucherService.getMySavedVouchers(pageable));
    }
}
