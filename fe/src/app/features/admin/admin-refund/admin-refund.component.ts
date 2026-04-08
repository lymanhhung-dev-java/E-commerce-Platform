import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { AdminRefundService, Refund } from '../../../core/services/admin-refund.service';
import { FormsModule } from '@angular/forms';
import { NgIf, NgFor, CurrencyPipe, DatePipe } from '@angular/common';
import Swal from 'sweetalert2';

@Component({
  selector: 'app-admin-refund',
  standalone: true,
  imports: [NgIf, NgFor, CurrencyPipe, DatePipe, FormsModule],
  templateUrl: './admin-refund.component.html',
  styleUrls: ['./admin-refund.component.css']
})
export class AdminRefundComponent implements OnInit {
  refunds: Refund[] = [];
  isLoading = false;

  constructor(private adminRefundService: AdminRefundService) {}

  ngOnInit(): void {
    this.loadRefunds();
  }

  loadRefunds(): void {
    this.isLoading = true;
    this.adminRefundService.getPendingRefunds().subscribe({
      next: (data) => {
        this.refunds = data;
        this.isLoading = false;
      },
      error: (err) => {
        console.error('Failed to load refunds', err);
        this.isLoading = false;
        Swal.fire('Lỗi!', 'Không thể lấy danh sách yêu cầu hoàn tiền.', 'error');
      }
    });
  }

  confirmRefund(refundId: number): void {
    Swal.fire({
      title: 'Xác nhận đã chuyển khoản?',
      text: "Bạn chắc chắn đã chuyển khoản hoàn tất cho mã yêu cầu này?",
      icon: 'warning',
      showCancelButton: true,
      confirmButtonColor: '#3085d6',
      cancelButtonColor: '#d33',
      confirmButtonText: 'Đã hoàn tiền',
      cancelButtonText: 'Hủy'
    }).then((result) => {
      if (result.isConfirmed) {
        this.adminRefundService.confirmRefund(refundId).subscribe({
          next: () => {
            Swal.fire('Thành công!', 'Đã xác nhận hoàn tiền thành công.', 'success');
            this.loadRefunds();
          },
          error: (err) => {
            console.error('Lỗi khi xác nhận hoàn tiền', err);
            Swal.fire('Lỗi!', 'Có lỗi xảy ra khi xác nhận hoàn tiền.', 'error');
          }
        });
      }
    });
  }
}
