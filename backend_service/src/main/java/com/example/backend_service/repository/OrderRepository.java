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

public interface OrderRepository extends JpaRepository<Order, Long>,JpaSpecificationExecutor<Order>{
    Page<Order> findByShop(Shop shop, Pageable pageable);

    List<Order> findByUserOrderByCreatedAtDesc(User user);




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

    
    

}
