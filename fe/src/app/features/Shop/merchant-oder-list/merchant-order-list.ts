import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { OrderService } from '../../../core/services/order.service';
import { Order } from '../../../core/models/order';
import { FormsModule,} from '@angular/forms';
declare var bootstrap: any;

@Component({
  selector: 'app-merchant-order-list',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './merchant-order-list.html',
  styleUrls: ['./merchant-order-list.css']
})


export class MerchantOrderListComponent implements OnInit {
  orders: Order[] = [];
  selectedOrder: Order | null = null;
  currentPage = 0;
  totalPages = 0;
  totalElements = 0;
  isLoading = false;

  // Danh sách trạng thái khớp với Enum trong Java (OrderStatus.java)
  orderStatuses = [
    { value: 'PENDING', label: 'Chờ xác nhận', color: 'bg-warning' },
    { value: 'PROCESSING', label: 'Đang xử lý', color: 'bg-info' },
    { value: 'SHIPPED', label: 'Đang giao', color: 'bg-primary' },
    { value: 'DELIVERED', label: 'Đã giao', color: 'bg-success' },
    { value: 'CANCELED', label: 'Huỷ đơn', color: 'bg-danger' },
    { value: 'RETURNED', label: 'Trả hàng', color: 'bg-secondary' }
  ];

  constructor(private orderService: OrderService) {}

  ngOnInit(): void {
    this.loadOrders();
  }

  loadOrders(page: number = 0) {
    this.isLoading = true;
    this.orderService.getShopOrders(page).subscribe({
      next: (res) => {
        this.orders = res.content;
        this.totalPages = res.totalPages;
        this.totalElements = res.totalElements;
        this.currentPage = res.number;
        this.isLoading = false;
      },
      error: (err) => {
        console.error('Lỗi tải đơn hàng', err);
        this.isLoading = false;
      }
    });
  }

  // Hàm xử lý khi người dùng đổi trạng thái trên Dropdown
  onStatusChange(order: Order, event: any) {
    const newStatus = event.target.value;
    const oldStatus = order.status;

    if (!confirm(`Bạn có chắc muốn đổi trạng thái đơn hàng #${order.id} sang ${newStatus}?`)) {
      // Nếu user hủy, reset lại dropdown về giá trị cũ
      event.target.value = oldStatus;
      return;
    }

    this.orderService.updateOderStatus(order.id, newStatus).subscribe({
      next: (res) => {
        alert('Cập nhật trạng thái thành công!');
        order.status = newStatus; // Cập nhật UI
      },
      error: (err) => {
        console.error(err);
        alert('Lỗi cập nhật trạng thái');
        event.target.value = oldStatus; // Revert nếu lỗi
      }
    });
  }

  // Helper để lấy màu badge
  getStatusColor(status: string): string {
    const found = this.orderStatuses.find(s => s.value === status);
    return found ? found.color : 'bg-secondary';
  }
  
  openOrderDetails(order: Order) {
    this.selectedOrder = order;
    const modalElement = document.getElementById('orderDetailModal');
    if (modalElement) {
      const modal = new bootstrap.Modal(modalElement);
      modal.show();
    }
  
  }
  // Helper lấy label tiếng Việt
  getStatusLabel(status: string): string {
    const found = this.orderStatuses.find(s => s.value === status);
    return found ? found.label : status;
  }

  onPageChange(page: number) {
    this.loadOrders(page);
  }
}