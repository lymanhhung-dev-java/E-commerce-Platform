import { Component, inject, OnInit } from '@angular/core';
import { Router, RouterLink, RouterLinkActive } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';
import { CategoryService } from '../../../core/services/category.service'; 
import { Category } from '../../../core/models/category'; 
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { CartService } from '../../../core/services/cart.service';

@Component({
  selector: 'app-header',
  standalone: true,
  imports: [RouterLink, CommonModule, FormsModule],
  templateUrl: './header.component.html', 
  styleUrl: './header.component.css'        
})
export class HeaderComponent implements OnInit {
  authService = inject(AuthService);
  categoryService = inject(CategoryService); 
  router = inject(Router);
  cartService = inject(CartService);

  categories: Category[] = [];
  selectedCategoryName: string = 'All Categories';

  ngOnInit() {
    this.loadCategories();
  }

  loadCategories() {
    this.categoryService.getCategories().subscribe({
      next: (res) => {
        this.categories = res;
      },
      error: (err) => console.error('Lỗi tải danh mục:', err)
    });
  }


  onSelectCategory(catId: number | null, catName: string) {
    this.selectedCategoryName = catName;
    
    this.router.navigate(['/'], { 
      queryParams: catId ? { categoryId: catId } : {} 
    });
  }

  onSearch(keyword: string) {
     this.router.navigate(['/'], { queryParams: { search: keyword }, queryParamsHandling: 'merge' });
  }
}