package com.example.backend_service.repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.example.backend_service.model.auth.User;
import com.example.backend_service.model.business.Shop;
import com.example.backend_service.model.order.Order;
import com.example.backend_service.common.OrderStatus;

public interface OrderRepository extends JpaRepository<Order, Long>,JpaSpecificationExecutor<Order>{
    Page<Order> findByShop(Shop shop, Pageable pageable);

    List<Order> findByShopAndStatus(Shop shop, OrderStatus status);

    @Query("SELECT o FROM Order o WHERE o.status = :status AND (o.isFundReleased = false OR o.isFundReleased IS NULL) AND o.updatedAt <= :date")
    List<Order> findByStatusAndIsFundReleasedFalseAndUpdatedAtBefore(@Param("status") OrderStatus status, @Param("date") LocalDateTime date);

    List<Order> findByUserOrderByCreatedAtDesc(User user);

    Long countByShopAndStatus(Shop shop, OrderStatus status);

    @Query("SELECT COUNT(o) FROM Order o WHERE o.shop = :shop AND o.createdAt >= :startDate")
    Long countNewOrdersSince(@Param("shop") Shop shop, @Param("startDate") java.time.LocalDateTime startDate);

    @Query("SELECT SUM(o.finalAmountToShop) FROM Order o WHERE o.shop = :shop AND o.createdAt >= :startDate AND o.status <> com.example.backend_service.common.OrderStatus.CANCELED AND o.status <> com.example.backend_service.common.OrderStatus.RETURNED")
    java.math.BigDecimal sumRevenueSince(@Param("shop") Shop shop, @Param("startDate") java.time.LocalDateTime startDate);




    @Query("SELECT FUNCTION('MONTH', o.createdAt), SUM(o.totalAmount) " +
           "FROM Order o " +
           "WHERE o.shop.id = :shopId " +
           "AND o.status = 'DELIVERED' " +
           "AND FUNCTION('YEAR', o.createdAt) = :year " +
           "GROUP BY FUNCTION('MONTH', o.createdAt) " +
           "ORDER BY FUNCTION('MONTH', o.createdAt)")
    List<Object[]> findRevenueByYear(@Param("shopId") Long shopId, @Param("year") int year);

    @Query("SELECT FUNCTION('DAY', o.createdAt), SUM(o.totalAmount) " +
           "FROM Order o " +
           "WHERE o.shop.id = :shopId " +
           "AND o.status = 'DELIVERED' " +
           "AND FUNCTION('MONTH', o.createdAt) = :month " +
           "AND FUNCTION('YEAR', o.createdAt) = :year " +
           "GROUP BY FUNCTION('DAY', o.createdAt) " +
           "ORDER BY FUNCTION('DAY', o.createdAt)")
    List<Object[]> findRevenueByMonth(@Param("shopId") Long shopId, @Param("month") int month, @Param("year") int year);

    @Query("SELECT FUNCTION('DATE', o.createdAt), SUM(o.totalAmount) " +
           "FROM Order o " +
           "WHERE o.shop.id = :shopId " +
           "AND o.status = 'DELIVERED' " +
           "AND o.createdAt BETWEEN :startDate AND :endDate " +
           "GROUP BY FUNCTION('DATE', o.createdAt) " +
           "ORDER BY FUNCTION('DATE', o.createdAt)")
    List<Object[]> findRevenueByDateRange(@Param("shopId") Long shopId, 
                                          @Param("startDate") LocalDateTime startDate, 
                                          @Param("endDate") LocalDateTime endDate);

    @Query("SELECT SUM(o.totalAmount) FROM Order o " +
           "WHERE o.shop.id = :shopId " +
           "AND o.status = 'DELIVERED'")
    BigDecimal sumTotalRevenueByShop(@Param("shopId") Long shopId);

    @Query("SELECT SUM(o.totalAmount) FROM Order o " +
           "WHERE o.shop.id = :shopId " +
           "AND o.status = 'DELIVERED' " +
           "AND FUNCTION('MONTH', o.createdAt) = :month " +
           "AND FUNCTION('YEAR', o.createdAt) = :year")
    BigDecimal sumRevenueByMonth(@Param("shopId") Long shopId, 
                                 @Param("month") int month, 
                                 @Param("year") int year);

    @Query("SELECT SUM(o.totalAmount) FROM Order o WHERE o.status = 'DELIVERED'")
    BigDecimal sumTotalPlatformRevenue();

    @Query("SELECT " +
           "COALESCE(SUM(o.finalAmountToShop + o.shopVoucherDiscount + o.commissionFee), 0), " +
           "COALESCE(SUM(o.shopVoucherDiscount), 0), " +
           "COALESCE(SUM(o.commissionFee), 0), " +
           "COALESCE(SUM(o.finalAmountToShop), 0) " +
           "FROM Order o " +
           "WHERE o.shop.id = :shopId " +
           "AND o.status = 'DELIVERED'")
    List<Object[]> getFinancialReportByShop(@Param("shopId") Long shopId);

    @Query("SELECT " +
           "COALESCE(SUM(o.totalProductPrice), 0), " +
           "COALESCE(SUM(o.shopVoucherDiscount), 0), " +
           "COALESCE(SUM(o.commissionFee), 0), " +
           "COALESCE(SUM(o.finalAmountToShop), 0) " +
           "FROM Order o " +
           "WHERE o.shop.id = :shopId " +
           "AND o.status = 'DELIVERED' " +
           "AND FUNCTION('MONTH', o.createdAt) = :month " +
           "AND FUNCTION('YEAR', o.createdAt) = :year")
    List<Object[]> getMonthlyFinancialReportByShop(@Param("shopId") Long shopId,
                                                   @Param("month") int month,
                                                   @Param("year") int year);
}
