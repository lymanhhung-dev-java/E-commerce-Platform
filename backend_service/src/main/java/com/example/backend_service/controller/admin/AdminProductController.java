package com.example.backend_service.controller.admin;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.backend_service.dto.response.product.ProductListResponse;
import com.example.backend_service.service.Admin.AdminProductService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/admin/products")
@RequiredArgsConstructor
@Tag(name = "Admin Product Controller", description = "Admin quản lý sản phẩm")
public class AdminProductController {

    private final AdminProductService adminProductService;

    @Operation(summary = "Lấy danh sách sản phẩm", description = "Hỗ trợ tìm kiếm theo tên/shop và lọc theo trạng thái")
    @GetMapping
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<Page<ProductListResponse>> getProducts(
            @RequestParam(required = false) String keyword, 
            @RequestParam(required = false) Boolean status, 
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return ResponseEntity.ok(adminProductService.getAllProducts(keyword, status, pageable));
    }

    @Operation(summary = "Khóa / Mở khóa sản phẩm", description = "Thay đổi trạng thái Active/Locked của sản phẩm")
    @PutMapping("/{id}/toggle-status")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<String> toggleProductStatus(@PathVariable Long id) {
        adminProductService.toggleProductStatus(id);
        return ResponseEntity.ok("Cập nhật trạng thái sản phẩm thành công!");
    }
}