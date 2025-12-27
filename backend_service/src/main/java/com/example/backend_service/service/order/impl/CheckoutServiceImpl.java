package com.example.backend_service.service.order.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.example.backend_service.common.OrderStatus;
import com.example.backend_service.dto.request.order.CheckoutRequest;
import com.example.backend_service.exception.AppException;
import com.example.backend_service.exception.OutOfStockException;
import com.example.backend_service.model.auth.User;
import com.example.backend_service.model.business.Shop;
import com.example.backend_service.model.order.CartItem;
import com.example.backend_service.model.order.Order;
import com.example.backend_service.model.order.OrderItem;
import com.example.backend_service.model.product.Product;
import com.example.backend_service.repository.CartItemRepository;
import com.example.backend_service.repository.OrderItemRepository;
import com.example.backend_service.repository.OrderRepository;
import com.example.backend_service.repository.ProductRepository;
import com.example.backend_service.repository.UserRepository;
import com.example.backend_service.service.order.CheckoutService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j(topic = "CHECKOUT-SERVICE")
public class CheckoutServiceImpl implements CheckoutService {

    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;
    private final CartItemRepository cartItemRepository;
    private final OrderItemRepository orderItemRepository;
    private final UserRepository userRepository;
    
    private User getCurrentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByUsername(username);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<Long> checkout(CheckoutRequest request) {

        User user = getCurrentUser();

        // Validate và lấy danh sách sản phẩm (Lock row để tránh race condition)
        List<Product> products = new ArrayList<>();
        for (CheckoutRequest.Item it : request.getItems()) {
            Product p = productRepository.findByIdForUpdate(it.getProductId())
                    .orElseThrow(() -> new AppException("Sản phẩm không tồn tại: " + it.getProductId()));
            
            if (p.getStockQuantity() == null || p.getStockQuantity() < it.getQuantity()) {
                throw new OutOfStockException("Sản phẩm hết hàng: " + p.getName());
            }
            products.add(p);
        }

        //  Gom nhóm theo Shop ID (An toàn hơn dùng Object Shop làm key)
        Map<Long, List<CheckoutRequest.Item>> itemsByShopId = new HashMap<>();
        
        for (CheckoutRequest.Item itemReq : request.getItems()) {
            Product p = products.stream()
                .filter(prod -> prod.getId().equals(itemReq.getProductId()))
                .findFirst().orElseThrow();
            
            // Dùng ID của Shop làm Key
            itemsByShopId.computeIfAbsent(p.getShop().getId(), k -> new ArrayList<>()).add(itemReq);
        }

        List<Long> createdOrderIds = new ArrayList<>();

        // Tạo đơn hàng cho từng Shop
        for (Map.Entry<Long, List<CheckoutRequest.Item>> entry : itemsByShopId.entrySet()) {
            Long shopId = entry.getKey();
            List<CheckoutRequest.Item> itemsInShop = entry.getValue();
            
            Product representativeProduct = products.stream()
                .filter(p -> p.getShop().getId().equals(shopId))
                .findFirst().orElseThrow();
            Shop shop = representativeProduct.getShop();

            Order order = new Order();
            order.setUser(user);
            order.setShop(shop); 
            order.setShippingAddress(request.getShippingAddress());
            order.setShippingPhone(request.getShippingPhone());
            order.setPaymentMethod(request.getPaymentMethod());
            order.setNote(request.getNote()); 
            order.setStatus(OrderStatus.PENDING);

            order = orderRepository.save(order);

            BigDecimal totalAmount = BigDecimal.ZERO;
            List<OrderItem> orderItems = new ArrayList<>();

            for (CheckoutRequest.Item itemReq : itemsInShop) {
                Product p = products.stream()
                        .filter(prod -> prod.getId().equals(itemReq.getProductId()))
                        .findFirst().orElseThrow();

                // Trừ kho
                p.setStockQuantity(p.getStockQuantity() - itemReq.getQuantity());
                productRepository.save(p);

                OrderItem oi = new OrderItem();
                oi.setOrder(order);
                oi.setProduct(p);
                oi.setQuantity(itemReq.getQuantity());
                
                BigDecimal price = p.getPrice();
                oi.setPrice(p.getPrice()); 
                
                orderItems.add(oi);
                
                // Cộng dồn tổng tiền: Giá * Số lượng
                BigDecimal lineTotal = price.multiply(BigDecimal.valueOf(itemReq.getQuantity()));
                totalAmount = totalAmount.add(lineTotal);
            }

            orderItemRepository.saveAll(orderItems);
            
            // Cập nhật lại tổng tiền sau khi đã cộng hết
            order.setTotalAmount(totalAmount);
            orderRepository.save(order);
            
            createdOrderIds.add(order.getId());
        }

        //  Xóa giỏ hàng
        // Chỉ xóa những item có Product nằm trong list đã mua
        List<CartItem> toDelete = cartItemRepository.findByUserAndProductIn(user, products);
        if (!toDelete.isEmpty()) {
            cartItemRepository.deleteAll(toDelete);
        }

        return createdOrderIds; 
    }
}