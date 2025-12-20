package com.example.backend_service.service.order.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestMapping;

import com.example.backend_service.dto.request.checkout.CheckoutRequest;
import com.example.backend_service.exception.AppException;
import com.example.backend_service.exception.OutOfStockException;
import com.example.backend_service.model.auth.User;
import com.example.backend_service.model.order.CartItem;
import com.example.backend_service.model.order.Order;
import com.example.backend_service.model.order.OrderItem;
import com.example.backend_service.model.product.Product;
import com.example.backend_service.repository.CartItemRepository;
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
    private final UserRepository userRepository;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long checkout(CheckoutRequest request, String username) {
        User user = userRepository.findByUsername(username);
        if (user == null) throw new AppException("User not found");

        // load products with lock
        List<Product> products = new ArrayList<>();
        for (CheckoutRequest.Item it : request.getItems()) {
            Product p = productRepository.findByIdForUpdate(it.getProductId())
                    .orElseThrow(() -> new AppException("Product not found: " + it.getProductId()));
            if (p.getStockQuantity() == null || p.getStockQuantity() < it.getQuantity()) {
                throw new OutOfStockException("Sản phẩm hết hàng: " + p.getName());
            }
            products.add(p);
        }

        // decrement stock and prepare order items
        BigDecimal total = BigDecimal.ZERO;
        List<OrderItem> orderItems = new ArrayList<>();
        for (CheckoutRequest.Item it : request.getItems()) {
            Product p = products.stream().filter(x -> x.getId().equals(it.getProductId())).findFirst().get();
            int newStock = p.getStockQuantity() - it.getQuantity();
            p.setStockQuantity(newStock);
            productRepository.save(p);

            OrderItem oi = new OrderItem();
            oi.setProduct(p);
            oi.setQuantity(it.getQuantity());
            oi.setPrice(p.getPrice());
            orderItems.add(oi);

            total = total.add(p.getPrice().multiply(BigDecimal.valueOf(it.getQuantity())));
        }

        // create order
        Order order = new Order();
        order.setUser(user);
        // assume same shop for all items; pick from first product
        order.setShop(products.get(0).getShop());
        order.setShippingAddress(request.getShippingAddress());
        order.setShippingPhone(request.getShippingPhone());
        order.setTotalAmount(total);
        order.setOrderItems(orderItems);
        // set back-reference
        for (OrderItem oi : orderItems) oi.setOrder(order);

        Order saved = orderRepository.save(order);

        // remove from cart
        List<Product> boughtProducts = products.stream().collect(Collectors.toList());
        List<CartItem> toDelete = cartItemRepository.findByUserAndProductIn(user, boughtProducts);
        if (!toDelete.isEmpty()) cartItemRepository.deleteAll(toDelete);

        return saved.getId();
    }
}
