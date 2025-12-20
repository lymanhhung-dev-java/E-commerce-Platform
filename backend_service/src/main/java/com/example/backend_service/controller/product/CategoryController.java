package com.example.backend_service.controller.product;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.backend_service.model.product.Category;
import com.example.backend_service.service.product.CategoryService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/categories") 
@RequiredArgsConstructor
@Tag(name = "Public Category Controller", description = "APIs for viewing categories (Public)")
public class CategoryController {

    private final CategoryService categoryService;

    @Operation(summary = "Get All Categories", description = "Lấy danh sách danh mục (Dùng cho Sidebar/Menu)")
    @GetMapping
    @Transactional(readOnly = true)
    public ResponseEntity<List<Category>> getAllCategories() {
        return ResponseEntity.ok(categoryService.getAllCategories());
    }
}