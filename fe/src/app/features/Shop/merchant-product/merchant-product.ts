import { Component, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ProductService } from '../../../core/services/product.service';
import { CategoryService } from '../../../core/services/category.service';
import { MerchantProductResponse } from '../../../core/models/merchant-product';
import { ToastrService } from 'ngx-toastr';
import { RouterModule } from '@angular/router';

@Component({
    selector: 'app-merchant-product-list',
    standalone: true,
    imports: [CommonModule, FormsModule, RouterModule],
    templateUrl: './merchant-product.html',
    styleUrls: ['./merchant-product.css']
})
export class MerchantProductListComponent implements OnInit {
    productService = inject(ProductService);
    categoryService = inject(CategoryService);
    toastr = inject(ToastrService);

    products: MerchantProductResponse[] = [];
    categories: any[] = [];

    filterKeyword: string = '';
    filterCategoryId: number | null = null;
    filterStatus: string = 'ALL';

    // Phân trang
    page: number = 0;
    size: number = 10;
    totalPages: number = 0;
    totalElements: number = 0;
    isLoading = false;

    ngOnInit() {
        this.loadCategories();
        this.loadProducts();
    }

    // 1. Lấy danh sách danh mục để đổ vào dropdown lọc
    loadCategories() {
        this.categoryService.getFlatCategories().subscribe({
            next: (res) => this.categories = res,
            error: () => console.error('Lỗi tải danh mục')
        });
    }

    // 2. Gọi API lấy danh sách sản phẩm
    loadProducts() {
        this.isLoading = true;
        let statusParam: boolean | null = null;
        if (this.filterStatus === 'ACTIVE') statusParam = true;
        if (this.filterStatus === 'INACTIVE') statusParam = false;

        this.productService.getMerchantProducts(
            this.filterKeyword,
            this.filterCategoryId,
            statusParam,
            this.page,
            this.size
        ).subscribe({
            next: (res) => {
                this.products = res.content;
                this.totalPages = res.totalPages;
                this.totalElements = res.totalElements;
                this.isLoading = false;
            },
            error: (err) => {
                this.toastr.error('Không thể tải danh sách sản phẩm');
                this.isLoading = false;
            }
        });
    }

    onFilterChange() {
        this.page = 0;
        this.loadProducts();
    }

    onPageChange(newPage: number) {
        if (newPage >= 0 && newPage < this.totalPages) {
            this.page = newPage;
            this.loadProducts();
        }
    }

    onToggleStatus(id: number) {
        if (confirm('Bạn có chắc muốn thay đổi trạng thái không?')) {
            this.productService.onToggleStatus(id).subscribe({
                next: () => {
                    this.toastr.success('Thay đổi trạng thái thành công');
                    this.loadProducts();
                },
                error: () => this.toastr.error('Lỗi khi thay đổi trạng thái')
            });
        }
    }


    // 5. Xóa sản phẩm
    // Helper để lấy stock an toàn từ nhiều trường có thể có
    getStock(p: any): number {
        return p.stockQuantity ?? p.stock ?? p.quantity ?? 0;
    }

    // Helper để lấy status an toàn
    isActive(p: any): boolean {
        // Kiểm tra kỹ các trường hợp true/false
        return (p.active === true) || (p.status === true) || (p.isActive === true);
    }

    onDelete(id: number) {
        if (confirm('Bạn có chắc muốn xóa sản phẩm này không?')) {
            this.productService.deleteProduct(id).subscribe({
                next: () => {
                    this.toastr.success('Xóa thành công');
                    this.loadProducts();
                },
                error: () => this.toastr.error('Lỗi khi xóa sản phẩm')
            });
        }
    }
}