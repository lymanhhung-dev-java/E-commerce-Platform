package com.example.backend_service.service.product.impl;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.example.backend_service.dto.request.product.CategoryRequest;
import com.example.backend_service.dto.response.product.CategoryResponse;
import com.example.backend_service.exception.AppException;
import com.example.backend_service.model.product.Category;
import com.example.backend_service.repository.CategoryRepository;
import com.example.backend_service.service.product.CategoryService;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;


@Service
@RequiredArgsConstructor
@Slf4j(topic = "CATEGORY-SERVICE")
public class CategoryServiceImpl  implements CategoryService{

    private final CategoryRepository categoryRepository;

    @Override
    @Transactional
    public Category createCategory(CategoryRequest request) {
        Category category = new Category();
        category.setName(request.getName());
        if (request.getIsActive() != null) {
            category.setIsActive(request.getIsActive());
        } else {
            category.setIsActive(true);
        }

        if(request.getParentId() != null){
            Category parentCategory = categoryRepository.findById(request.getParentId())
            .orElseThrow(() -> new AppException("Danh mục cha không tồn tại"));
                category.setParent(parentCategory);
        }
        return categoryRepository.save(category);
    }

    @Override
    @Transactional
    public Category updateCategory(Long id, CategoryRequest request) {
        
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new AppException("Danh mục không tồn tại"));

        category.setName(request.getName());
        
        if (request.getIsActive() != null) {
            category.setIsActive(request.getIsActive());
        }

        if (request.getParentId() != null) {
            if (request.getParentId().equals(id)) {
                throw new AppException("Danh mục không thể làm cha của chính nó");
            }

            Category parent = categoryRepository.findById(request.getParentId())
                    .orElseThrow(() -> new AppException("Danh mục cha không tồn tại"));
            category.setParent(parent);
        } else {
            category.setParent(null);
        }

        return categoryRepository.save(category);
    }

    @Override
    public List<Category> getAllCategories() {
        return categoryRepository.findByParentIsNull();
    }

    @Override
    public Category getCategoryById(Long id) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getCategoryById'");
    }

    @Override
    @Transactional
    public void deleteCategory(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new AppException("Danh mục không tồn tại"));

        if (category.getChildren() != null && !category.getChildren().isEmpty()) {
            throw new AppException("Không thể xóa vì danh mục này đang chứa các danh mục con. Hãy xóa hoặc di chuyển danh mục con trước.");
        }      
        categoryRepository.delete(category);
    }

    @Override
    public List<CategoryResponse> getAllCategoriesFlattened() {
       List<Category> rootCategories = categoryRepository.findByParentIsNull();
        
        List<CategoryResponse> result = new ArrayList<>();
        
        for (Category root : rootCategories) {
            flattenRecursive(root, 0, result);
        }
        
        return result;
    }
    

    private void flattenRecursive(Category category, int level, List<CategoryResponse> result) {

        String prefix = "";
        if (level > 0) {
            prefix = "-- ".repeat(level); 
        }

        CategoryResponse dto = CategoryResponse.builder()
                .id(category.getId())
                .name(category.getName())
                .imageUrl(category.getImageUrl())
                .active(category.getIsActive())
                .parentId(category.getParent() != null ? category.getParent().getId() : null)
                .level(level)
                .displayName(prefix + " " + category.getName()) // Format tên hiển thị
                .build();

        result.add(dto);

        if (category.getChildren() != null && !category.getChildren().isEmpty()) {
            for (Category child : category.getChildren()) {
                flattenRecursive(child, level + 1, result);
            }
        }
    }
    
    
}
