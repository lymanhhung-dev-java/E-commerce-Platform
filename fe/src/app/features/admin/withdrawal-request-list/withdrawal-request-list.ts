import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AdminWithdrawalService, Withdrawal } from '../../../core/services/admin-withdrawal.service';
import { ToastrService } from 'ngx-toastr';

@Component({
  selector: 'app-withdrawal-request-list',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './withdrawal-request-list.html',
  styles: [`
    .badge-soft-warning { background-color: #fff8dd; color: #b78a00; }
    .badge-soft-success { background-color: #d1fae5; color: #065f46; }
    .badge-soft-danger { background-color: #fee2e2; color: #991b1b; }
    .table th { font-weight: 600; font-size: 0.85rem; text-transform: uppercase; color: #6b7280; }
    .table td { vertical-align: middle; font-size: 0.9rem; }
  `]
})
export class WithdrawalRequestListComponent implements OnInit {
  private withdrawalService = inject(AdminWithdrawalService);
  private toastr = inject(ToastrService);

  withdrawals: Withdrawal[] = [];
  
  // Pagination
  currentPage = 0;
  pageSize = 10;
  totalPages = 0;
  totalElements = 0;

  // Filter
  selectedStatus: string = 'ALL';

  // Modal Từ chối
  showRejectModal = false;
  rejectReason = '';
  selectedId: number | null = null;
  isLoading = false;

  ngOnInit() {
    this.loadData();
  }

  loadData() {
    this.isLoading = true;
    this.withdrawalService.getWithdrawals(this.currentPage, this.pageSize, this.selectedStatus)
      .subscribe({
        next: (res) => {
          this.withdrawals = res.content;
          this.totalPages = res.totalPages;
          this.totalElements = res.totalElements;
          this.isLoading = false;
        },
        error: (err) => {
          this.toastr.error('Lỗi tải dữ liệu');
          this.isLoading = false;
        }
      });
  }

  onFilterChange() {
    this.currentPage = 0;
    this.loadData();
  }

  onPageChange(page: number) {
    this.currentPage = page;
    this.loadData();
  }

  // --- ACTIONS ---

  // 1. Duyệt đơn
  approve(id: number) {
    if (!confirm('Bạn có chắc muốn DUYỆT yêu cầu rút tiền này?')) return;

    this.withdrawalService.updateStatus(id, 'APPROVED').subscribe({
      next: () => {
        this.toastr.success('Đã duyệt yêu cầu thành công');
        this.loadData();
      },
      error: (err) => this.toastr.error('Lỗi: ' + (err.error || err.message))
    });
  }

  // 2. Mở modal từ chối
  openRejectModal(id: number) {
    this.selectedId = id;
    this.rejectReason = '';
    this.showRejectModal = true;
  }

  // 3. Xác nhận từ chối
  confirmReject() {
    if (!this.selectedId) return;
    if (!this.rejectReason.trim()) {
      this.toastr.warning('Vui lòng nhập lý do từ chối');
      return;
    }

    this.withdrawalService.updateStatus(this.selectedId, 'REJECTED', this.rejectReason).subscribe({
      next: () => {
        this.toastr.success('Đã từ chối và hoàn tiền cho Shop');
        this.showRejectModal = false;
        this.loadData();
      },
      error: (err) => this.toastr.error('Lỗi: ' + (err.error || err.message))
    });
  }
}