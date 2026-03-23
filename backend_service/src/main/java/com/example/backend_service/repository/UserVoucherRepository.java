package com.example.backend_service.repository;

import com.example.backend_service.model.UserVoucher;
import com.example.backend_service.model.Voucher;
import com.example.backend_service.model.auth.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserVoucherRepository extends JpaRepository<UserVoucher, Long> {

    Page<UserVoucher> findByUserAndIsUsedFalse(User user, Pageable pageable);

    boolean existsByUserAndVoucher(User user, Voucher voucher);

    Optional<UserVoucher> findByUserAndVoucher(User user, Voucher voucher);
}
