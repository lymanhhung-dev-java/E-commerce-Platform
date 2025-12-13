package com.example.backend_service.service.product.impl;

import java.util.List;

import org.springframework.stereotype.Service;

import com.example.backend_service.dto.request.product.CategoryRequest;
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
        category.setImageUrl(request.getImageUrl());
        category.setIsActive(true);

        if(request.getParntId() != null){
            Category parentCategory = categoryRepository.findById(request.getParntId())
            .orElseThrow(() -> new AppException("Danh mục cha không tồn tại"));
                category.setParent(parentCategory);
        }
        return categoryRepository.save(category);
    }

    @Override
    public Category updateCategory(Long id, CategoryRequest request) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'updateCategory'");
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
    public void deleteCategory(Long id) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'deleteCategory'");
    }

    
    
}
