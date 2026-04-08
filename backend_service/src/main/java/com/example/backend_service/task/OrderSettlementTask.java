package com.example.backend_service.task;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.example.backend_service.common.OrderStatus;
import com.example.backend_service.model.business.Shop;
import com.example.backend_service.model.order.Order;
import com.example.backend_service.repository.OrderRepository;
import com.example.backend_service.repository.ShopRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j(topic = "SETTLEMENT-TASK")
public class OrderSettlementTask {

    private final OrderRepository orderRepository;
    private final ShopRepository shopRepository;

    /**
     * Chạy định kỳ vào 1 giờ sáng mỗi ngày.
     * Cron: "0 0 1 * * ?"
     */
    @Scheduled(cron = "0 0 1 * * ?")
    @Transactional(rollbackFor = Exception.class)
    public void processPendingBalances() {
        log.info("Bắt đầu tiến trình quét và giải ngân tiền đơn hàng sau 7 ngày...");

        LocalDateTime sevenDaysAgo = LocalDateTime.now().minusDays(7);

        // Tìm các đơn hàng đã DELIVERED, có updateAt qua 7 ngày, và chưa rút tiền
        List<Order> ordersToProcess = orderRepository.findByStatusAndIsFundReleasedFalseAndUpdatedAtBefore(
                OrderStatus.DELIVERED, sevenDaysAgo
        );

        if (ordersToProcess.isEmpty()) {
            log.info("Không có đơn hàng nào đủ điều kiện giải ngân trong hôm nay.");
            return;
        }

        int count = 0;
        for (Order order : ordersToProcess) {
            try {
                Shop shop = order.getShop();

                // Lấy số tiền thực nhận
                double finalAmountToShop = order.getFinalAmountToShop() != null ? order.getFinalAmountToShop().doubleValue() : 0.0;

                // Trừ khỏi pending và cộng vào balance
                shop.setPendingBalance(Math.max(0, shop.getPendingBalance() - finalAmountToShop));
                shop.setBalance(shop.getBalance().add(BigDecimal.valueOf(finalAmountToShop)));

                // Lưu cờ báo hiệu đã giải ngân
                order.setIsFundReleased(true);
                
                // (Tùy chọn) Chuyển luôn sang trạng thái COMPLETED thể hiện kết thúc chu kỳ
                order.setStatus(OrderStatus.COMPLETED);

                shopRepository.save(shop);
                orderRepository.save(order);
                count++;

                log.info("Giải ngân thành công cho OrderId: {}, ShopId: {}, Số tiền: {}", order.getId(), shop.getId(), finalAmountToShop);
            } catch (Exception e) {
                log.error("Lỗi khi giải ngân cho OrderId: {}", order.getId(), e);
            }
        }

        log.info("Đã hoàn tất giải ngân cho {} đơn hàng.", count);
    }
}
