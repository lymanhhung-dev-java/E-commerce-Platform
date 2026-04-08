package com.example.backend_service.service.Admin.impl;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.backend_service.common.OrderStatus;
import com.example.backend_service.common.RefundStatus;
import com.example.backend_service.exception.AppException;
import com.example.backend_service.model.business.Shop;
import com.example.backend_service.model.order.Order;
import com.example.backend_service.model.order.OrderItem;
import com.example.backend_service.model.order.Refund;
import com.example.backend_service.model.product.Product;
import com.example.backend_service.repository.OrderItemRepository;
import com.example.backend_service.repository.OrderRepository;
import com.example.backend_service.repository.ProductRepository;
import com.example.backend_service.repository.RefundRepository;
import com.example.backend_service.repository.ShopRepository;
import com.example.backend_service.service.Admin.AdminRefundService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AdminRefundServiceImpl implements AdminRefundService {

    private final RefundRepository refundRepository;
    private final OrderRepository orderRepository;
    private final ShopRepository shopRepository;
    private final OrderItemRepository orderItemRepository;
    private final ProductRepository productRepository;

    @Override
    public List<Refund> getPendingRefunds() {
        return refundRepository.findByStatus(RefundStatus.PENDING);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void confirmRefund(Long refundId) {
        Refund refund = refundRepository.findById(refundId)
                .orElseThrow(() -> new AppException("Yêu cầu hoàn tiền không tồn tại"));

        if (refund.getStatus() != RefundStatus.PENDING) {
            throw new AppException("Yêu cầu không ở trạng thái PENDING");
        }

        Order order = orderRepository.findById(refund.getOrderId())
                .orElseThrow(() -> new AppException("Đơn hàng không tồn tại"));

        if (order.getStatus() != OrderStatus.REFUND_PENDING) {
            throw new AppException("Đơn hàng không ở trạng thái REFUND_PENDING");
        }

        // 1. Cập nhật Refund
        refund.setStatus(RefundStatus.COMPLETED);
        refundRepository.save(refund);

        // 2. Cập nhật Order => CANCELED
        order.setStatus(OrderStatus.CANCELED);
        orderRepository.save(order);

        // 3. Trừ số tiền khỏi pendingBalance của Shop
        Shop shop = order.getShop();
        double amountToSubtract = order.getFinalAmountToShop() != null ? order.getFinalAmountToShop().doubleValue() : 0.0;
        shop.setPendingBalance(Math.max(0, shop.getPendingBalance() - amountToSubtract));
        shopRepository.save(shop);

        // 4. Hoàn số lượng hàng vào kho
        List<OrderItem> items = orderItemRepository.findByOrder(order);
        for (OrderItem item : items) {
            Product product = item.getProduct();
            product.setStockQuantity(product.getStockQuantity() + item.getQuantity());
            productRepository.save(product);
        }
    }
}
