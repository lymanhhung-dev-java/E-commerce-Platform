import { Component, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ShopService } from '../../../core/services/shop.Service';
import { ToastrService } from 'ngx-toastr';

@Component({
  selector: 'app-shop-request-list',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './shop-request-list.html',
  styleUrl: './shop-request-list.css' 
})
export class ShopRequestListComponent implements OnInit {
  shopService = inject(ShopService);
  toastr = inject(ToastrService);

  shopRequests: any[] = [];
  
  currentPage = 0;
  pageSize = 10;
  totalPages = 0;
  totalElements = 0;

  ngOnInit() {
    this.loadRequests();
  }

  loadRequests() {
    this.shopService.getPendingShops(this.currentPage, this.pageSize).subscribe({
      next: (res) => {
        this.shopRequests = res.content;
        this.totalPages = res.totalPages;
        this.totalElements = res.totalElements;
      },
      error: () => this.toastr.error('Lỗi tải danh sách yêu cầu')
    });
  }

  // --- XỬ LÝ DUYỆT / TỪ CHỐI ---
  onApprove(shop: any, isApproved: boolean) {
    const actionName = isApproved ? 'DUYỆT' : 'TỪ CHỐI';
    const confirmMsg = `Bạn có chắc chắn muốn ${actionName} yêu cầu mở shop "${shop.shopName}"?`;

    if (confirm(confirmMsg)) {
      this.shopService.approveShop(shop.id, isApproved).subscribe({
        next: (msg) => {
          this.toastr.success(msg || `Đã ${actionName.toLowerCase()} thành công`);
          this.loadRequests();
        },
        error: (err) => {
          this.toastr.error('Có lỗi xảy ra khi xử lý yêu cầu');
          console.error(err);
        }
      });
    }
  }

  onPageChange(page: number) {
    this.currentPage = page;
    this.loadRequests();
  }
}