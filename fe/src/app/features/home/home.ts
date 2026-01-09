import { Component, inject, OnInit, OnDestroy, PLATFORM_ID } from '@angular/core';
import { CommonModule, isPlatformBrowser } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ProductService } from '../../core/services/product.service';
import { ActivatedRoute, Router } from '@angular/router';
import { ProductCardComponent } from '../product/product-card/product-card';
import { Product } from '../../core/models/product';
import { Category } from '../../core/models/category';

@Component({
  selector: 'app-home',
  standalone: true,
  imports: [CommonModule, ProductCardComponent, FormsModule],
  templateUrl: './home.html',
  styleUrl: './home.css'
})
export class HomeComponent implements OnInit, OnDestroy {
  private productService = inject(ProductService);
  private route = inject(ActivatedRoute);
  private platformId = inject(PLATFORM_ID);


  products: Product[] = [];
  isLoading = false;

  // Banners
  banners: string[] = [
    '/images/banners/banner1.png',
    '/images/banners/banner2.png',
    '/images/banners/banner3.png'
  ];
  currentBannerIndex = 0;
  private bannerInterval: any;

  currentPage = 0;
  pageSize = 12;
  totalPages = 0;
  keyword: string = '';
  selectedCategoryId: number | null = null;
  selectedPriceRange: string = 'all';
  minPrice?: number;
  maxPrice?: number;

  ngOnInit() {
    this.route.queryParams.subscribe(params => {
      this.selectedCategoryId = params['categoryId'] ? Number(params['categoryId']) : null;
      this.keyword = params['search'] || '';
      this.loadData();
    });

    if (isPlatformBrowser(this.platformId)) {
      this.bannerInterval = setInterval(() => {
        this.nextBanner();
      }, 5000);
    }
  }

  ngOnDestroy() {
    if (this.bannerInterval) {
      clearInterval(this.bannerInterval);
    }
  }

  nextBanner() {
    this.currentBannerIndex = (this.currentBannerIndex + 1) % this.banners.length;
  }

  prevBanner() {
    this.currentBannerIndex = (this.currentBannerIndex - 1 + this.banners.length) % this.banners.length;
  }

  setBanner(index: number) {
    this.currentBannerIndex = index;
  }

  toggleCategory(cat: Category) {
    cat.expanded = !cat.expanded;
  }
  loadData() {
    this.isLoading = true;

    this.productService.getProducts(
      this.currentPage,
      this.pageSize,
      this.keyword,
      this.selectedCategoryId || undefined,
      this.minPrice,
      this.maxPrice
    ).subscribe({
      next: (res: any) => {
        this.products = res.content;
        this.totalPages = res.totalPages;
        this.isLoading = false;

        if (isPlatformBrowser(this.platformId)) {
          window.scrollTo({ top: 0, behavior: 'smooth' });
        }
      },
      error: (err) => {
        console.error('Lỗi tải data:', err);
        this.isLoading = false;
      }
    });
  }

  onSearch() {
    this.currentPage = 0;
    this.loadData();
  }

  onCategorySelect(catId: number | null) {
    this.selectedCategoryId = catId;
    this.currentPage = 0;
    this.loadData();
  }

  onPriceChange() {
    switch (this.selectedPriceRange) {
      case 'under5':
        this.minPrice = undefined; this.maxPrice = 5000000; break;
      case '5to10':
        this.minPrice = 5000000; this.maxPrice = 10000000; break;
      case '10to20':
        this.minPrice = 10000000; this.maxPrice = 20000000; break;
      case 'over20':
        this.minPrice = 20000000; this.maxPrice = undefined; break;
      default:
        this.minPrice = undefined; this.maxPrice = undefined;
    }
    this.currentPage = 0;
    this.loadData();
  }

  onPageChange(page: number) {
    if (page >= 0 && page < this.totalPages) {
      this.currentPage = page;
      this.loadData();
    }
  }
}