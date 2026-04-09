import { Component, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { ShopService } from '../../core/services/shop.Service';
import { ProductService } from '../../core/services/product.service';
import { ChatService } from '../../core/services/chat.service';
import { ProductResponse } from '../../core/models/product';
import { VoucherService } from '../../core/services/voucher.service';
import { ToastrService } from 'ngx-toastr';

@Component({
    selector: 'app-shop-detail',
    standalone: true,
    imports: [CommonModule, RouterModule],
    templateUrl: './shop-detail.html',
    styleUrls: ['./shop-detail.css']
})
export class ShopDetailComponent implements OnInit {
    private route = inject(ActivatedRoute);
    private router = inject(Router);
    private shopService = inject(ShopService);
    private productService = inject(ProductService);
    private chatService = inject(ChatService);
    private voucherService = inject(VoucherService);
    private toastr = inject(ToastrService);

    shopId: number = 0;
    shop: any = null;
    products: any[] = [];
    vouchers: any[] = [];

    isLoading = false;
    page = 0;
    size = 10;
    totalPages = 0;

    ngOnInit() {
        this.route.paramMap.subscribe(params => {
            const id = params.get('id');
            if (id) {
                this.shopId = +id;
                this.loadShopInfo();
                this.loadShopProducts();
                this.loadShopVouchers();
            }
        });
    }

    loadShopInfo() {
        this.shopService.getShopById(this.shopId).subscribe({
            next: (res) => this.shop = res,
            error: (err: any) => console.error('Error loading shop info', err)
        });
    }

    loadShopProducts() {
        this.isLoading = true;
        this.productService.getShopProducts(this.shopId, this.page, this.size).subscribe({
            next: (res: ProductResponse) => {
                this.products = res.content;
                this.totalPages = res.totalPages;
                this.isLoading = false;
            },
            error: (err: any) => {
                console.error('Error loading shop products', err);
                this.isLoading = false;
            }
        });
    }

    loadShopVouchers() {
        this.voucherService.getShopPublicVouchers(this.shopId).subscribe({
            next: (res: any[]) => this.vouchers = res,
            error: (err: any) => console.error('Error loading shop vouchers', err)
        });
    }

    saveVoucher(code: string) {
        this.voucherService.saveVoucher(code).subscribe({
            next: () => this.toastr.success('Lưu mã giảm giá thành công!'),
            error: (err: any) => this.toastr.error(err.error?.message || 'Lỗi khi lưu mã')
        });
    }

    onPageChange(newPage: number) {
        if (newPage >= 0 && newPage < this.totalPages) {
            this.page = newPage;
            this.loadShopProducts();
        }
    }

    chatWithShop() {
        if (!this.shopId) return;
        this.chatService.createOrGetRoom(this.shopId).subscribe({
            next: () => {
                this.router.navigate(['/chat']);
            },
            error: (err: any) => {
                console.error('Error creating chat room:', err);
            }
        });
    }
}
