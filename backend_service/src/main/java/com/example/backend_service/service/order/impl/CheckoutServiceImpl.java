package com.example.backend_service.service.order.impl;

import com.example.backend_service.common.OrderStatus;
import com.example.backend_service.dto.request.order.CheckoutRequest;
import com.example.backend_service.dto.response.order.SepayResponse;
import com.example.backend_service.dto.response.order.SepayTransactionDto;
import com.example.backend_service.exception.AppException;
import com.example.backend_service.exception.OutOfStockException;
import com.example.backend_service.model.auth.User;
import com.example.backend_service.model.business.Shop;
import com.example.backend_service.model.order.CartItem;
import com.example.backend_service.model.order.Order;
import com.example.backend_service.model.order.OrderItem;
import com.example.backend_service.model.product.Product;
import com.example.backend_service.repository.*;
import com.example.backend_service.service.order.CheckoutService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j(topic = "CHECKOUT-SERVICE")
public class CheckoutServiceImpl implements CheckoutService {

    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;
    private final CartItemRepository cartItemRepository;
    private final OrderItemRepository orderItemRepository;
    private final UserRepository userRepository;
    private final RestTemplate restTemplate;

    @Value("${sepay.api.token}")
    private String sepayApiToken;

    private final String MY_BANK_ACC = "0867696204";
    private final String MY_BANK_NAME = "MBBank";

    private User getCurrentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByUsername(username);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<Long> checkout(CheckoutRequest request) {
        User user = getCurrentUser();

        List<Product> products = new ArrayList<>();
        for (CheckoutRequest.Item it : request.getItems()) {
            Product p = productRepository.findByIdForUpdate(it.getProductId())
                    .orElseThrow(() -> new AppException("Sản phẩm không tồn tại: " + it.getProductId()));

            if (p.getStockQuantity() == null || p.getStockQuantity() < it.getQuantity()) {
                throw new OutOfStockException("Sản phẩm hết hàng: " + p.getName());
            }
            products.add(p);
        }

        Map<Long, List<CheckoutRequest.Item>> itemsByShopId = new HashMap<>();
        for (CheckoutRequest.Item itemReq : request.getItems()) {
            Product p = products.stream()
                    .filter(prod -> prod.getId().equals(itemReq.getProductId()))
                    .findFirst().orElseThrow();
            itemsByShopId.computeIfAbsent(p.getShop().getId(), k -> new ArrayList<>()).add(itemReq);
        }

        List<Long> createdOrderIds = new ArrayList<>();

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
            order.setTotalAmount(BigDecimal.ZERO);

            String uniquePaymentCode = "PAY" + System.currentTimeMillis() + "U" + user.getId();
            String currentNote = order.getNote() == null ? "" : order.getNote();
            order.setNote(currentNote + " [Mã CK: " + uniquePaymentCode + "]");

            order = orderRepository.save(order);

            BigDecimal totalAmount = BigDecimal.ZERO;
            List<OrderItem> orderItems = new ArrayList<>();

            for (CheckoutRequest.Item itemReq : itemsInShop) {
                Product p = products.stream()
                        .filter(prod -> prod.getId().equals(itemReq.getProductId()))
                        .findFirst().orElseThrow();

                p.setStockQuantity(p.getStockQuantity() - itemReq.getQuantity());
                productRepository.save(p);

                OrderItem oi = new OrderItem();
                oi.setOrder(order);
                oi.setProduct(p);
                oi.setQuantity(itemReq.getQuantity());
                oi.setPrice(p.getPrice());

                BigDecimal lineTotal = p.getPrice().multiply(BigDecimal.valueOf(itemReq.getQuantity()));
                totalAmount = totalAmount.add(lineTotal);
                orderItems.add(oi);
            }

            orderItemRepository.saveAll(orderItems);
            order.setTotalAmount(totalAmount);
            orderRepository.save(order);
            createdOrderIds.add(order.getId());
        }

        List<CartItem> toDelete = cartItemRepository.findByUserAndProductIn(user, products);
        if (!toDelete.isEmpty()) {
            cartItemRepository.deleteAll(toDelete);
        }

        return createdOrderIds;
    }

    @Override
    public String getPaymentQrUrl(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new AppException("Đơn hàng không tồn tại"));

        String content = extractPaymentCode(order.getNote());

        return String.format("https://qr.sepay.vn/img?acc=%s&bank=%s&amount=%s&des=%s",
                MY_BANK_ACC,
                MY_BANK_NAME,
                order.getTotalAmount().toPlainString(),
                content
        );
    }

    @Override
    public boolean checkPaymentStatus(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new AppException("Đơn hàng không tồn tại"));

        boolean isPaid = checkPaymentStatusInternal(order);

        if (isPaid) {
            if (order.getStatus() == OrderStatus.PENDING) {
                order.setStatus(OrderStatus.PAID);
                orderRepository.save(order);
            }
            return true;
        }
        return false;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelOrderAndRestoreCart(Long orderId) {
        User user = getCurrentUser();

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new AppException("Đơn hàng không tồn tại"));

        if (order.getStatus() != OrderStatus.PENDING) {
            throw new AppException("Đơn hàng đã được xử lý hoặc thanh toán. Không thể hủy!");
        }

        boolean isJustPaid = checkPaymentStatusInternal(order);

        if (isJustPaid) {
            order.setStatus(OrderStatus.PAID);
            orderRepository.save(order);
            throw new AppException("Giao dịch thành công! Tiền đã vào tài khoản nên không thể hủy đơn.");
        }

        List<OrderItem> orderItems = orderItemRepository.findByOrder(order);
        List<CartItem> restoredCartItems = new ArrayList<>();

        for (OrderItem item : orderItems) {
            Product product = item.getProduct();

            product.setStockQuantity(product.getStockQuantity() + item.getQuantity());
            productRepository.save(product);

            CartItem cartItem = cartItemRepository.findByUserAndProduct(user, product)
                    .orElse(new CartItem());

            if (cartItem.getId() == null) {
                cartItem.setUser(user);
                cartItem.setProduct(product);
                cartItem.setQuantity(item.getQuantity());
            } else {
                cartItem.setQuantity(cartItem.getQuantity() + item.getQuantity());
            }
            restoredCartItems.add(cartItem);
        }

        cartItemRepository.saveAll(restoredCartItems);
        orderItemRepository.deleteAll(orderItems);
        orderRepository.delete(order);
    }

    private boolean checkPaymentStatusInternal(Order order) {
        try {
            String paymentCodeToCheck = extractPaymentCode(order.getNote());
            BigDecimal amountToCheck = order.getTotalAmount();

            String url = "https://my.sepay.vn/userapi/transactions/list?account_number=" + MY_BANK_ACC + "&limit=20";

            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + sepayApiToken);
            headers.set("Content-Type", "application/json");
            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<SepayResponse> response = restTemplate.exchange(
                    url, HttpMethod.GET, entity, SepayResponse.class
            );

            if (response.getBody() != null && response.getBody().getTransactions() != null) {
                for (SepayTransactionDto trans : response.getBody().getTransactions()) {
                    boolean amountMatch = trans.getAmountIn().compareTo(amountToCheck) >= 0;
                    boolean contentMatch = trans.getTransactionContent() != null &&
                            trans.getTransactionContent().toUpperCase().contains(paymentCodeToCheck.toUpperCase());

                    if (amountMatch && contentMatch) {
                        return true;
                    }
                }
            }
        } catch (Exception e) {
            log.error("Lỗi check SePay order {}: {}", order.getId(), e.getMessage());
            return false;
        }
        return false;
    }

    private String extractPaymentCode(String note) {
        if (note == null || !note.contains("Mã CK: ")) return "UNKNOWN";
        try {
            int start = note.indexOf("Mã CK: ") + 7;
            int end = note.indexOf("]", start);
            if (end > start) {
                return note.substring(start, end);
            }
            return "UNKNOWN";
        } catch (Exception e) {
            return "UNKNOWN";
        }
    }
}