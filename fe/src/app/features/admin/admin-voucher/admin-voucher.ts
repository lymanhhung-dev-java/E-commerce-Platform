import { Component, inject, OnInit } from '@angular/core';
import { CommonModule, DatePipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { VoucherService, PageableResponse } from '../../../core/services/voucher.service';
import { Voucher, VoucherRequest } from '../../../core/models/voucher';
import { ToastrService } from 'ngx-toastr';

@Component({
  selector: 'app-admin-voucher',
  standalone: true,
  imports: [CommonModule, FormsModule],
  providers: [DatePipe],
  templateUrl: './admin-voucher.html',
  styleUrls: ['./admin-voucher.css']
})
export class AdminVoucherComponent implements OnInit {
  voucherService = inject(VoucherService);
  toastr = inject(ToastrService);
  datePipe = inject(DatePipe);

  vouchers: Voucher[] = [];
  filterKeyword: string = '';
  today: Date = new Date();

  // Pagination
  page: number = 0;
  size: number = 10;
  totalPages: number = 0;
  totalElements: number = 0;
  isLoading = false;

  // Form state
  showForm: boolean = false;
  isEditMode: boolean = false;
  editingId: number | null = null;
  
  formData: VoucherRequest = {
    code: '',
    discountValue: 0,
    discountType: 'FIXED',
    minOrderValue: 0,
    maxDiscount: 0,
    startDate: '',
    endDate: '',
    limitUsage: 0
  };

  ngOnInit() {
    this.loadVouchers();
  }

  loadVouchers() {
    this.isLoading = true;
    this.voucherService.getAdminVouchers(this.page, this.size, this.filterKeyword)
      .subscribe({
        next: (res: PageableResponse<Voucher>) => {
          this.vouchers = res.content;
          this.totalPages = res.totalPages;
          this.totalElements = res.totalElements;
          this.isLoading = false;
        },
        error: () => {
          this.toastr.error('Lỗi khi tải danh sách Voucher hệ thống');
          this.isLoading = false;
        }
      });
  }

  onFilterChange() {
    this.page = 0;
    this.loadVouchers();
  }

  onPageChange(newPage: number) {
    if (newPage >= 0 && newPage < this.totalPages) {
      this.page = newPage;
      this.loadVouchers();
    }
  }

  openCreateForm() {
    this.isEditMode = false;
    this.editingId = null;
    this.resetForm();
    this.showForm = true;
  }

  openEditForm(voucher: Voucher) {
    this.isEditMode = true;
    this.editingId = voucher.id;
    this.formData = {
      code: voucher.code,
      discountValue: voucher.discountValue,
      discountType: voucher.discountType,
      minOrderValue: voucher.minOrderValue,
      maxDiscount: voucher.maxDiscount,
      limitUsage: voucher.limitUsage || 0,
      // Format 'yyyy-MM-ddTHH:mm' for datetime-local input
      startDate: this.formatDateForInput(voucher.startDate),
      endDate: this.formatDateForInput(voucher.endDate)
    };
    this.showForm = true;
  }

  closeForm() {
    this.showForm = false;
  }

  resetForm() {
    this.formData = {
      code: '',
      discountValue: 0,
      discountType: 'FIXED',
      minOrderValue: 0,
      maxDiscount: 0,
      startDate: '',
      endDate: '',
      limitUsage: 0
    };
  }

  formatDateForInput(dateString: string): string {
    const date = new Date(dateString);
    return this.datePipe.transform(date, 'yyyy-MM-ddTHH:mm') || '';
  }

  onSaveVoucher() {
    if (!this.formData.code || this.formData.discountValue < 0 || !this.formData.startDate || !this.formData.endDate) {
      this.toastr.warning('Vui lòng điền đầy đủ và đúng thông tin!');
      return;
    }

    if (new Date(this.formData.startDate) >= new Date(this.formData.endDate)) {
      this.toastr.warning('Ngày kết thúc phải sau ngày bắt đầu!');
      return;
    }

    if (this.isEditMode && this.editingId) {
      this.voucherService.updateAdminVoucher(this.editingId, this.formData).subscribe({
        next: () => {
          this.toastr.success('Cập nhật Voucher thành công!');
          this.showForm = false;
          this.loadVouchers();
        },
        error: (err) => {
          this.toastr.error(err.error?.message || 'Lỗi khi cập nhật Voucher');
        }
      });
    } else {
      this.voucherService.createAdminVoucher(this.formData).subscribe({
        next: () => {
          this.toastr.success('Tạo Voucher hệ thống mới thành công!');
          this.showForm = false;
          this.loadVouchers();
        },
        error: (err) => {
          this.toastr.error(err.error?.message || 'Lỗi khi tạo Voucher');
        }
      });
    }
  }

  onDelete(id: number) {
    if (confirm('Bạn có chắc chắn muốn xóa Voucher hệ thống này?')) {
      this.voucherService.deleteAdminVoucher(id).subscribe({
        next: () => {
          this.toastr.success('Xóa Voucher định thành công!');
          this.loadVouchers();
        },
        error: () => this.toastr.error('Lỗi khi xóa Voucher')
      });
    }
  }

  toggleStatus(voucher: Voucher) {
    if (voucher.ownerType !== 'SYSTEM') return;
    if (confirm(`Bạn có chắc muốn ${voucher.isActive ? 'tắt' : 'bật'} voucher này không?`)) {
      this.voucherService.toggleAdminVoucherStatus(voucher.id).subscribe({
        next: () => {
          this.toastr.success('Thay đổi trạng thái thành công!');
          this.loadVouchers();
        },
        error: () => this.toastr.error('Lỗi khi thay đổi trạng thái')
      });
    } else {
      this.loadVouchers();
    }
  }
}
