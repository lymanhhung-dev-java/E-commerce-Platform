import { Component, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, RouterModule } from '@angular/router';
import { ShopService } from '../../core/services/shop.Service';
import { ProductService } from '../../core/services/product.service';
import { ProductResponse } from '../../core/models/product';

@Component({
    selector: 'app-shop-detail',
    standalone: true,
    imports: [CommonModule, RouterModule],
    templateUrl: './shop-detail.html',
    styleUrls: ['./shop-detail.css']
})
export class ShopDetailComponent implements OnInit {
    private route = inject(ActivatedRoute);
    private shopService = inject(ShopService);
    private productService = inject(ProductService);

    shopId: number = 0;
    shop: any = null;
    products: any[] = [];

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
            }
        });
    }

    loadShopInfo() {
        this.shopService.getShopById(this.shopId).subscribe({
            next: (res) => this.shop = res,
            error: (err) => console.error('Error loading shop info', err)
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
            error: (err) => {
                console.error('Error loading shop products', err);
                this.isLoading = false;
            }
        });
    }

    onPageChange(newPage: number) {
        if (newPage >= 0 && newPage < this.totalPages) {
            this.page = newPage;
            this.loadShopProducts();
        }
    }
}
