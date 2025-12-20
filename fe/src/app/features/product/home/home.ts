import { Component, inject, OnInit, PLATFORM_ID } from '@angular/core'; 
import { CommonModule, isPlatformBrowser } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ProductService } from '../../../core/services/product.service';
import { CategoryService } from '../../../core/services/category.service';
import { ProductCardComponent } from '../product-card/product-card';
import { Product } from '../../../core/models/product';
import { Category } from '../../../core/models/category';

@Component({
  selector: 'app-home',
  standalone: true,
  imports: [CommonModule, ProductCardComponent, FormsModule],
  templateUrl: './home.html',
  styleUrl: './home.css'
})
export class HomeComponent implements OnInit {
  private productService = inject(ProductService);
  private categoryService = inject(CategoryService);
  private platformId = inject(PLATFORM_ID);
  

  products: Product[] = [];
  categories: Category[] = [];
  isLoading = false;


  currentPage = 0;
  pageSize = 12;
  totalPages = 0;
  keyword = '';
  selectedCategoryId: number | null = null;
  
  selectedPriceRange: string = 'all'; 
  minPrice?: number;
  maxPrice?: number;

  ngOnInit() {
    this.loadCategories();
    this.loadData();
  }

  toggleCategory(cat: Category) {
    cat.expanded = !cat.expanded;
  }
  loadCategories() {
    this.categoryService.getCategories().subscribe({
      next: (res) => this.categories = res,
      error: (err) => console.error('Lỗi tải danh mục:', err)
    });
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