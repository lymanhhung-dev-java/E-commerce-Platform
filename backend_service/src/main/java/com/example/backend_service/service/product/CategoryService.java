package com.example.backend_service.service.product;

import java.util.List;

import com.example.backend_service.dto.request.product.CategoryRequest;
import com.example.backend_service.model.product.Category;

public interface CategoryService {
    Category createCategory(CategoryRequest request);
    Category updateCategory(Long id, CategoryRequest request);
    List <Category> getAllCategories();
    Category getCategoryById(Long id);
    void deleteCategory(Long id);
}
