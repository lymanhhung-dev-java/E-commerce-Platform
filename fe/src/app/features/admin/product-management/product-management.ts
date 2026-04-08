import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AdminProductService, AdminProductResponse } from '../../../core/services/admin-product.service';
import { ProductService } from '../../../core/services/product.service';
import { ShopService } from '../../../core/services/shop.Service';
import { ToastrService } from 'ngx-toastr';
import { ActivatedRoute } from '@angular/router';
import Swal from 'sweetalert2';

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
  private publicProductService = inject(ProductService);
  private shopService = inject(ShopService);
  private toastr = inject(ToastrService);
  private route = inject(ActivatedRoute);

  products: AdminProductResponse[] = [];
  shops: any[] = [];
  isLoading = false;
  selectedProductDetails: any = null;

  // Pagination
  currentPage = 0;
  pageSize = 10;
  totalPages = 0;
  totalElements = 0;

  // Filter
  keyword: string = '';
  selectedStatus: string = 'ALL'; // ALL | ACTIVE | LOCKED
  selectedShopId: string | null = null; 
  minPrice: number | null = null;
  maxPrice: number | null = null;

  ngOnInit() {
    this.loadShops();
    this.route.queryParams.subscribe(params => {
      if (params['shopId']) {
        this.selectedShopId = params['shopId'];
      }
      this.loadProducts();
    });
  }

  loadShops() {
    this.shopService.getShopsForAdmin('', 'ACTIVE', 0, 1000).subscribe({
      next: (res) => this.shops = res.content || [],
      error: (err) => console.error('Lỗi khi tải danh sách Shop', err)
    });
  }

  loadProducts() {
    this.isLoading = true;
    
    // Convert filter string sang boolean hoặc undefined
    let statusParam: boolean | undefined = undefined;
    if (this.selectedStatus === 'ACTIVE') statusParam = true;
    if (this.selectedStatus === 'LOCKED') statusParam = false;
    
    // Parse shopId filter
    const shopIdParam = this.selectedShopId && this.selectedShopId !== 'null' ? Number(this.selectedShopId) : undefined;
    
    // Parse price limits
    const minParam = this.minPrice !== null && this.minPrice >= 0 ? this.minPrice : undefined;
    const maxParam = this.maxPrice !== null && this.maxPrice >= 0 ? this.maxPrice : undefined;

    this.adminProductService.getProducts(this.currentPage, this.pageSize, this.keyword, statusParam, shopIdParam, minParam, maxParam)
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

  toggleStatus(product: AdminProductResponse) {
    const action = product.isActive ? 'Khóa' : 'Mở khóa';
    Swal.fire({
      title: 'Xác nhận thao tác',
      text: `Bạn có chắc muốn ${action.toLowerCase()} sản phẩm "${product.name}"?`,
      icon: 'warning',
      showCancelButton: true,
      showCloseButton: true,
      confirmButtonColor: product.isActive ? '#dc3545' : '#198754',
      cancelButtonColor: '#6c757d',
      confirmButtonText: 'Xác nhận ' + action,
      cancelButtonText: 'Thoát'
    }).then((result) => {
      if (result.isConfirmed) {
        this.adminProductService.toggleProductStatus(product.id).subscribe({
          next: (msg) => {
            this.toastr.success(msg || `Đã ${action.toLowerCase()} thành công!`);
            product.isActive = !product.isActive;
          },
          error: (err) => {
            this.toastr.error('Lỗi: ' + (err.error || err.message));
          }
        });
      }
    });
  }

  viewProductDetails(product: AdminProductResponse) {
    this.publicProductService.getProductById(product.id).subscribe({
      next: (res) => {
        this.selectedProductDetails = res;
      },
      error: (err) => {
        console.error(err);
        this.toastr.error('Không thể tải chi tiết sản phẩm');
      }
    });
  }
}