package com.example.backend_service.service.order.impl;

import com.example.backend_service.common.OrderStatus;
import com.example.backend_service.dto.request.order.CheckoutRequest;
import com.example.backend_service.dto.response.order.PaymentQrResponse;
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
import com.example.backend_service.service.voucher.VoucherService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import com.example.backend_service.model.Voucher;
import com.example.backend_service.common.DiscountType;
import com.example.backend_service.common.OwnerType;
import java.time.LocalDateTime;

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
    private final VoucherRepository voucherRepository;
    private final UserVoucherRepository userVoucherRepository;
    private final VoucherService voucherService;
    private final ShopRepository shopRepository;
    private final RefundRepository refundRepository;

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

            BigDecimal shopVoucherDiscount = BigDecimal.ZERO;
            if (request.getShopVoucherId() != null) {
                Voucher shopVoucher = voucherRepository.findById(request.getShopVoucherId())
                        .orElseThrow(() -> new AppException("Shop Voucher không tồn tại"));
                
                // Only process Shop Voucher if it matches the current shop
                if (shopVoucher.getOwnerType() == OwnerType.SHOP && shopId.equals(shopVoucher.getShopId())) {
                    LocalDateTime now = LocalDateTime.now();
                    if (now.isBefore(shopVoucher.getStartDate()) || now.isAfter(shopVoucher.getEndDate())) {
                        throw new AppException("Shop Voucher đã hết hạn hoặc chưa bắt đầu");
                    }
                    if (shopVoucher.getMinOrderValue() != null && totalAmount.compareTo(shopVoucher.getMinOrderValue()) < 0) {
                        throw new AppException("Chưa đạt giá trị đơn hàng tối thiểu để dùng Shop Voucher");
                    }
                    if (shopVoucher.getLimitUsage() != null && shopVoucher.getUsedCount() >= shopVoucher.getLimitUsage()) {
                        throw new AppException("Shop Voucher đã hết lượt sử dụng");
                    }

                    if (shopVoucher.getDiscountType() == DiscountType.FIXED) {
                        shopVoucherDiscount = shopVoucher.getDiscountValue();
                    } else if (shopVoucher.getDiscountType() == DiscountType.PERCENT) {
                        shopVoucherDiscount = totalAmount.multiply(shopVoucher.getDiscountValue()).divide(BigDecimal.valueOf(100));
                        if (shopVoucher.getMaxDiscount() != null && shopVoucherDiscount.compareTo(shopVoucher.getMaxDiscount()) > 0) {
                            shopVoucherDiscount = shopVoucher.getMaxDiscount();
                        }
                    }
                    if (shopVoucherDiscount.compareTo(totalAmount) > 0) {
                        shopVoucherDiscount = totalAmount;
                    }

                    // Update voucher usage
                    shopVoucher.setUsedCount(shopVoucher.getUsedCount() + 1);
                    voucherRepository.save(shopVoucher);

                    userVoucherRepository.findByUserAndVoucher(user, shopVoucher).ifPresent(uv -> {
                        uv.setIsUsed(true);
                        userVoucherRepository.save(uv);
                    });
                }
            }

            BigDecimal totalProductPrice = totalAmount;

            BigDecimal amountAfterShopDiscount = totalProductPrice.subtract(shopVoucherDiscount);
            if (amountAfterShopDiscount.compareTo(BigDecimal.ZERO) < 0) {
                amountAfterShopDiscount = BigDecimal.ZERO;
            }

            BigDecimal commissionRate = BigDecimal.valueOf(0.1);
            BigDecimal commissionFee = amountAfterShopDiscount.multiply(commissionRate);

            BigDecimal finalAmountToShop = amountAfterShopDiscount.subtract(commissionFee);

            BigDecimal systemVoucherDiscount = BigDecimal.ZERO;
            if (request.getSystemVoucherId() != null) {
                Voucher systemVoucher = voucherRepository.findById(request.getSystemVoucherId())
                        .orElseThrow(() -> new AppException("System Voucher không tồn tại"));
                if (systemVoucher.getOwnerType() != OwnerType.SYSTEM) {
                    throw new AppException("System Voucher không hợp lệ");
                }
                LocalDateTime now = LocalDateTime.now();
                if (now.isBefore(systemVoucher.getStartDate()) || now.isAfter(systemVoucher.getEndDate())) {
                    throw new AppException("System Voucher đã hết hạn hoặc chưa bắt đầu");
                }
                if (systemVoucher.getMinOrderValue() != null && totalProductPrice.compareTo(systemVoucher.getMinOrderValue()) < 0) {
                    throw new AppException("Chưa đạt giá trị đơn hàng tối thiểu để dùng System Voucher");
                }
                if (systemVoucher.getLimitUsage() != null && systemVoucher.getUsedCount() >= systemVoucher.getLimitUsage()) {
                    throw new AppException("System Voucher đã hết lượt sử dụng");
                }

                if (systemVoucher.getDiscountType() == DiscountType.FIXED) {
                    systemVoucherDiscount = systemVoucher.getDiscountValue();
                } else if (systemVoucher.getDiscountType() == DiscountType.PERCENT) {
                    systemVoucherDiscount = totalProductPrice.multiply(systemVoucher.getDiscountValue()).divide(BigDecimal.valueOf(100));
                    if (systemVoucher.getMaxDiscount() != null && systemVoucherDiscount.compareTo(systemVoucher.getMaxDiscount()) > 0) {
                        systemVoucherDiscount = systemVoucher.getMaxDiscount();
                    }
                }
                if (systemVoucherDiscount.compareTo(amountAfterShopDiscount) > 0) {
                    systemVoucherDiscount = amountAfterShopDiscount;
                }

                // Update voucher usage
                systemVoucher.setUsedCount(systemVoucher.getUsedCount() + 1);
                voucherRepository.save(systemVoucher);

                userVoucherRepository.findByUserAndVoucher(user, systemVoucher).ifPresent(uv -> {
                    uv.setIsUsed(true);
                    userVoucherRepository.save(uv);
                });

                // Clear the ID so it doesn't get applied to the next shop's order
                request.setSystemVoucherId(null);
            }

            BigDecimal shippingFee = BigDecimal.ZERO;
            BigDecimal finalUserPay = totalProductPrice.subtract(shopVoucherDiscount).subtract(systemVoucherDiscount).add(shippingFee);
            if (finalUserPay.compareTo(BigDecimal.ZERO) < 0) {
                finalUserPay = BigDecimal.ZERO;
            }

            order.setTotalProductPrice(totalProductPrice);
            order.setShopVoucherDiscount(shopVoucherDiscount);
            order.setSystemVoucherDiscount(systemVoucherDiscount);
            order.setCommissionRate(commissionRate);
            order.setCommissionFee(commissionFee);
            order.setFinalAmountToShop(finalAmountToShop);
            order.setTotalAmount(finalUserPay);
            
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
    public PaymentQrResponse getPaymentQrUrl(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new AppException("Đơn hàng không tồn tại"));

        String content = extractPaymentCode(order.getNote());

        String qrUrl = String.format("https://qr.sepay.vn/img?acc=%s&bank=%s&amount=%s&des=%s",
                MY_BANK_ACC,
                MY_BANK_NAME,
                order.getTotalAmount().toPlainString(),
                content
        );

        return PaymentQrResponse.builder()
                .qrUrl(qrUrl)
                .bankName("MBBank")
                .accountNo(MY_BANK_ACC)
                .accountName("CTY TNHH SEPAY")
                .amount(order.getTotalAmount())
                .content(content)
                .build();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean checkPaymentStatus(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new AppException("Đơn hàng không tồn tại"));

        SepayTransactionDto trans = checkPaymentStatusInternal(order);

        if (trans != null) {
            if (order.getStatus() == OrderStatus.PENDING) {
                order.setStatus(OrderStatus.PAID);
                
                String bankInfo = "Ngân hàng: " + trans.getBankBrandName() + ", STK: " + trans.getAccountNumber();
                order.setCustomerBankInfo(bankInfo);
                
                orderRepository.save(order);
                
                Shop shop = order.getShop();
                double finalAmountToShop = order.getFinalAmountToShop() != null ? order.getFinalAmountToShop().doubleValue() : 0.0;
                shop.setPendingBalance(shop.getPendingBalance() + finalAmountToShop);
                shopRepository.save(shop);
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

        if (order.getStatus() == OrderStatus.PAID) {
            order.setStatus(OrderStatus.REFUND_PENDING);
            orderRepository.save(order);

            com.example.backend_service.model.order.Refund refund = new com.example.backend_service.model.order.Refund();
            refund.setOrderId(order.getId());
            refund.setAmount(order.getTotalAmount().doubleValue());
            refund.setCustomerBankInfo(order.getCustomerBankInfo());
            refund.setReason("Người dùng yêu cầu hủy đơn hàng đã thanh toán");
            refund.setStatus(com.example.backend_service.common.RefundStatus.PENDING);
            refundRepository.save(refund);

            log.info("YÊU CẦU HOÀN TIỀN MỚI: Đơn hàng {} đã chuyển sang REFUND_PENDING, chờ Admin xử lý.", order.getId());
            return;
        } else if (order.getStatus() != OrderStatus.PENDING) {
            throw new AppException("Đơn hàng đã được xử lý. Không thể hủy!");
        }

        SepayTransactionDto trans = checkPaymentStatusInternal(order);

        if (trans != null) {
            order.setStatus(OrderStatus.REFUND_PENDING);
            String bankInfo = "Ngân hàng: " + trans.getBankBrandName() + ", STK: " + trans.getAccountNumber();
            order.setCustomerBankInfo(bankInfo);
            orderRepository.save(order);

            com.example.backend_service.model.order.Refund refund = new com.example.backend_service.model.order.Refund();
            refund.setOrderId(order.getId());
            refund.setAmount(order.getTotalAmount().doubleValue());
            refund.setCustomerBankInfo(bankInfo);
            refund.setReason("Người dùng yêu cầu hủy, giao dịch vừa khớp");
            refund.setStatus(com.example.backend_service.common.RefundStatus.PENDING);
            refundRepository.save(refund);

            log.info("YÊU CẦU HOÀN TIỀN MỚI: Đơn hàng {} đã chuyển sang REFUND_PENDING, chờ Admin xử lý.", order.getId());
            return;
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

    private SepayTransactionDto checkPaymentStatusInternal(Order order) {
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
                        return trans;
                    }
                }
            }
        } catch (Exception e) {
            log.error("Lỗi check SePay order {}: {}", order.getId(), e.getMessage());
            return null;
        }
        return null;
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