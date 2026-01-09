package com.example.backend_service.service.product.impl;

import com.example.backend_service.dto.request.product.MerchantProductCreateRequest;
import com.example.backend_service.dto.request.product.MerchantProductUpdateRequest;
import com.example.backend_service.dto.response.product.MerchantProductResponse;
import com.example.backend_service.dto.response.product.ProductDetailResponse;
import com.example.backend_service.model.product.Product;
import com.example.backend_service.model.product.ProductImage;
import com.example.backend_service.model.auth.User;
import com.example.backend_service.model.business.Shop;
import com.example.backend_service.model.product.Category;
import com.example.backend_service.repository.CategoryRepository;
import com.example.backend_service.repository.ProductRepository;
import com.example.backend_service.repository.UserRepository;
import com.example.backend_service.common.ShopStatus;
import com.example.backend_service.service.product.MerchantProductService;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j(topic = "MERCHANT-PRODUCT-SERVICE")
public class MerchantProductServiceImpl implements MerchantProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;

        private User getCurrentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByUsername(username);
    }


    @Override
    public ProductDetailResponse create(MerchantProductCreateRequest request) {
        @SuppressWarnings("null")
        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Category không tồn tại"));

        String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();
        User merchant = userRepository.findByUsername(currentUsername);
        Shop shop = merchant.getShop();
        if (shop == null) {
            throw new RuntimeException("Merchant does not have an associated shop");
        }
        if (shop.getStatus() != ShopStatus.ACTIVE) {
            throw new RuntimeException("Shop is not approved to add products");
        }

        Product product = new Product();
        product.setName(request.getName());
        product.setPrice(request.getPrice());
        product.setStockQuantity(request.getStock());
        product.setDescription(request.getDescription());
        product.setIsActive(true);
        product.setCategory(category);
        product.setImageUrl(request.getMainImageUrl());

        if (request.getDetailImageUrls() != null && !request.getDetailImageUrls().isEmpty()) {
            for (String url : request.getDetailImageUrls()) {
                ProductImage img = new ProductImage();
                img.setImageUrl(url);
                product.addImage(img); 
            }
        }

        product.setShop(shop);

        Product saveProduct = productRepository.save(product);
        return ProductDetailResponse.fromEntity(saveProduct);
    }

    @Override
    public ProductDetailResponse update(Long id, MerchantProductUpdateRequest request) {

        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm"));

        String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();
        User merchant = userRepository.findByUsername(currentUsername);
        if (!product.getShop().getOwner().getId().equals(merchant.getId())) {
            throw new RuntimeException("Bạn không có quyền chỉnh sửa sản phẩm này");
        }
        if (request.getName() != null) {
            product.setName(request.getName());
        }
        if (request.getPrice() != null) {
            product.setPrice(request.getPrice());
        }
        if (request.getStock() != null) {
            if (request.getStock() < 0) {
                throw new RuntimeException("Tồn kho phải >= 0");
            }
            product.setStockQuantity(request.getStock());
        }
        if (request.getDescription() != null) {
            product.setDescription(request.getDescription());
        }
        if (request.getImage() != null && !request.getImage().isEmpty()) {
            product.setImageUrl(request.getImage());
        }
        if (request.getIsActive() != null) {
            product.setIsActive(request.getIsActive());
        }
        if (request.getCategoryId() != null) {
            Category category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new RuntimeException("Category không tồn tại"));
            product.setCategory(category);
        }
        if (request.getDetailImages() != null) {
            product.getImages().clear(); 

            if (!request.getDetailImages().isEmpty()) {
                for (String url : request.getDetailImages()) {
                    ProductImage img = new ProductImage();
                    img.setImageUrl(url);
                    img.setProduct(product); 
                    product.addImage(img);   
                }
            }
        }
        Product updatedProduct = productRepository.save(product);
        return ProductDetailResponse.fromEntity(updatedProduct);
    }

    @Override
    public void softDelete(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm"));

        String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();
        User merchant = userRepository.findByUsername(currentUsername);
        if (!product.getShop().getOwner().getId().equals(merchant.getId())) {
            throw new RuntimeException("Bạn không có quyền chỉnh sửa sản phẩm này");
        }
        product.setIsDeleted(true);
        product.setIsActive(false);
        productRepository.save(product);
    }

    @Override
    public Page<MerchantProductResponse> getMerchantProducts(String keyword, Long categoryId, Boolean status,
            Pageable pageable) {
        User currentUsername = getCurrentUser();
        
        Page<Product> products = productRepository.findProductsForMerchant(
                currentUsername.getUsername(),
                keyword,
                categoryId,
                status,
                pageable
        );
        return products.map(MerchantProductResponse::fromEntity);

    }


    @Override
    public void toggleProductStatus(Long id) {
    Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm"));

     String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();
        User merchant = userRepository.findByUsername(currentUsername);
        if (!product.getShop().getOwner().getId().equals(merchant.getId())) {
            throw new RuntimeException("Bạn không có quyền chỉnh sửa sản phẩm này");
        }      
    product.setIsActive(!product.getIsActive());
    productRepository.save(product);
}
    }

