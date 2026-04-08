package com.example.backend_service.service.Admin;

import java.util.List;
import com.example.backend_service.model.order.Refund;

public interface AdminRefundService {
    List<Refund> getPendingRefunds();
    void confirmRefund(Long refundId);
}
