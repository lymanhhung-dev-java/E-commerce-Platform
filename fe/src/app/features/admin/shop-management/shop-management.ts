import { Component, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { ShopService } from '../../../core/services/shop.Service';
import { ToastrService } from 'ngx-toastr';
import Swal from 'sweetalert2';

@Component({
  selector: 'app-shop-management',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './shop-management.html',
  styleUrl: './shop-management.css'
})
export class ShopManagementComponent implements OnInit {
  shopService = inject(ShopService);
  toastr = inject(ToastrService);
  private router = inject(Router);

  shops: any[] = [];
  
  // Bộ lọc
  keyword = '';
  selectedStatus = ''; // '' = Tất cả, PENDING, ACTIVE, REJECTED, BANNED
  
  // Phân trang
  page = 0;
  size = 10;
  totalPages = 0;
  totalElements = 0;

  ngOnInit() {
    this.loadShops();
  }

  loadShops() {
    this.shopService.getShopsForAdmin(this.keyword, this.selectedStatus, this.page, this.size)
      .subscribe({
        next: (res) => {
          this.shops = res.content;
          this.totalPages = res.totalPages;
          this.totalElements = res.totalElements;
        },
        error: () => this.toastr.error('Lỗi tải danh sách Shop')
      });
  }

  onFilterChange() {
    this.page = 0; // Reset về trang 1 khi lọc
    this.loadShops();
  }

  onPageChange(newPage: number) {
    this.page = newPage;
    this.loadShops();
  }

  // Xử lý Duyệt / Khóa Shop
  updateStatus(shop: any, isApproved: boolean) {
    const action = isApproved ? 'DUYỆT' : 'TỪ CHỐI/KHÓA';
    Swal.fire({
      title: 'Xác nhận thao tác',
      text: `Bạn có chắc muốn ${action} shop "${shop.shopName}"?`,
      icon: isApproved ? 'question' : 'warning',
      showCancelButton: true,
      showCloseButton: true,
      confirmButtonColor: isApproved ? '#198754' : '#d33',
      cancelButtonColor: '#6c757d',
      confirmButtonText: 'Xác nhận ' + action,
      cancelButtonText: 'Thoát'
    }).then((result) => {
      if(result.isConfirmed) {
         this.shopService.approveShop(shop.id, isApproved).subscribe({
           next: () => {
             this.toastr.success('Cập nhật thành công');
             this.loadShops();
           },
           error: () => this.toastr.error('Lỗi cập nhật')
         });
      }
    });
  }

  onBan(shop: any) {
    Swal.fire({
      title: 'CẢNH BÁO MỨC ĐỘ CAO',
      text: `Bạn có chắc muốn KHÓA VĨNH VIỄN shop "${shop.shopName}"? Hành động này sẽ chặn chủ shop đăng nhập vào trang quản lý.`,
      icon: 'error',
      showCancelButton: true,
      showCloseButton: true,
      confirmButtonColor: '#dc3545',
      cancelButtonColor: '#6c757d',
      confirmButtonText: 'Xác nhận Khóa',
      cancelButtonText: 'Thoát'
    }).then((result) => {
      if (result.isConfirmed) {
        this.shopService.banShop(shop.id).subscribe({
          next: () => {
            this.toastr.success('Đã khóa shop thành công');
            this.loadShops(); // Tải lại danh sách
          },
          error: () => this.toastr.error('Lỗi khi khóa shop')
        });
      }
    });
  }

  viewShopProducts(shopId: number) {
    this.router.navigate(['/admin/products'], { queryParams: { shopId } });
  }
}