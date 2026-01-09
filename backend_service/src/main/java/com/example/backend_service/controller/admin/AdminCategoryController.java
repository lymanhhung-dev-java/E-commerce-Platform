package com.example.backend_service.controller.admin;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.example.backend_service.dto.request.product.CategoryRequest;
import com.example.backend_service.service.product.CategoryService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/admin/categories")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ROLE_ADMIN')")
@Slf4j(topic = "ADMIN-CATEGORY-CONTROLLER")
@Tag(name = "Admin Category Controller", description = "APIs for admin category management")
public class AdminCategoryController {
    private final CategoryService categoryService;

    @Operation(summary = "Create Category", description = "Create a new category")
    @PostMapping
    public ResponseEntity<?> createCategory(@RequestBody CategoryRequest req) {
        return ResponseEntity.ok(categoryService.createCategory(req));
    }

    @Operation(summary = "Update Category", description = "Cập nhật tên, trạng thái hoặc di chuyển danh mục cha")
    @PutMapping("/{id}")
    public ResponseEntity<?> updateCategory(@PathVariable Long id, @RequestBody CategoryRequest req) {
        return ResponseEntity.ok(categoryService.updateCategory(id, req));
    }

    @Operation(summary = "Delete Category", description = "Xóa danh mục (Chỉ xóa được khi không có danh mục con)")
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteCategory(@PathVariable Long id) {
        categoryService.deleteCategory(id);
        return ResponseEntity.ok("Xóa danh mục thành công");
    }

}
