import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AdminProductService, AdminProductResponse } from '../../../core/services/admin-product.service';
import { ToastrService } from 'ngx-toastr';

@Component({
  selector: 'app-product-management', 
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './product-management.html',
  styles: [`
    .img-thumb { width: 48px; height: 48px; object-fit: cover; border-radius: 6px; }
    .badge-soft-success { background-color: #d1fae5; color: #065f46; }
    .badge-soft-danger { background-color: #fee2e2; color: #991b1b; }
  `]
})
export class ProductManagementComponent implements OnInit {
  private adminProductService = inject(AdminProductService);
  private toastr = inject(ToastrService);

  products: AdminProductResponse[] = [];
  isLoading = false;

  // Pagination
  currentPage = 0;
  pageSize = 10;
  totalPages = 0;
  totalElements = 0;

  // Filter
  keyword: string = '';
  selectedStatus: string = 'ALL'; // ALL | ACTIVE | LOCKED

  ngOnInit() {
    this.loadProducts();
  }

  loadProducts() {
    this.isLoading = true;
    
    // Convert filter string sang boolean hoặc undefined
    let statusParam: boolean | undefined = undefined;
    if (this.selectedStatus === 'ACTIVE') statusParam = true;
    if (this.selectedStatus === 'LOCKED') statusParam = false;

    this.adminProductService.getProducts(this.currentPage, this.pageSize, this.keyword, statusParam)
      .subscribe({
        next: (res) => {
          this.products = res.content;
          this.totalPages = res.totalPages;
          this.totalElements = res.totalElements;
          this.isLoading = false;
        },
        error: (err) => {
          console.error(err);
          this.toastr.error('Không thể tải danh sách sản phẩm');
          this.isLoading = false;
        }
      });
  }

  onSearch() {
    this.currentPage = 0; // Reset về trang 1 khi tìm kiếm
    this.loadProducts();
  }

  onPageChange(page: number) {
    this.currentPage = page;
    this.loadProducts();
  }

  // Xử lý Khóa/Mở khóa
  toggleStatus(product: AdminProductResponse) {
    const action = product.isActive ? 'KHÓA' : 'MỞ KHÓA';
    if (!confirm(`Bạn có chắc muốn ${action} sản phẩm "${product.name}"?`)) return;

    this.adminProductService.toggleProductStatus(product.id).subscribe({
      next: (msg) => {
        this.toastr.success(msg || `Đã ${action} thành công!`);
        // Cập nhật lại trạng thái ngay trên giao diện (không cần load lại API)
        product.isActive = !product.isActive;
      },
      error: (err) => {
        this.toastr.error('Lỗi: ' + (err.error || err.message));
      }
    });
  }
}