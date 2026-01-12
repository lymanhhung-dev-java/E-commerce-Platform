import { Component, inject, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { ProductService } from '../../../core/services/product.service';
import { Product } from '../../../core/models/product';
import { ToastrService } from 'ngx-toastr';
import { CartService } from '../../../core/services/cart.service';
import { StarRatingComponent } from '../../../shared/components/star-rating/star-rating.component';

@Component({
  selector: 'app-detail-product',
  standalone: true,
  imports: [CommonModule, RouterLink, StarRatingComponent],
  templateUrl: './product-detail.html',
  styleUrl: './product-detail.css'
})
export class DetailProductComponent implements OnInit {
  private route = inject(ActivatedRoute);
  private productService = inject(ProductService);
  private toastr = inject(ToastrService);
  private cartService = inject(CartService);
  private cdr = inject(ChangeDetectorRef);

  product: Product | null = null;
  mainImage: string = '';
  quantity: number = 1;
  description: string = "";
  thumbnails: string[] = [];
  isZoomOpen: boolean = false;

  ngOnInit() {
    this.route.paramMap.subscribe(params => {
      const id = Number(params.get('id'));
      if (id) {
        // Load product and reviews in parallel for better performance
        this.loadProduct(id);
        this.loadReviews(id);
      }
    });
  }

  loadProduct(id: number) {
    this.productService.getProductById(id).subscribe({
      next: (res) => {
        this.product = res;
        this.mainImage = res.imageUrl || 'https://via.placeholder.com/500';

        if (res.productImages && res.productImages.length > 0) {
          this.thumbnails = [this.mainImage, ...res.productImages];
        } else {
          this.thumbnails = [this.mainImage];
        }
      },
      error: (err) => {
        console.error(err);
        this.toastr.error('Không tìm thấy sản phẩm');
      }
    });
  }

  reviews: any[] = [];
  totalReviews = 0;
  averageRating = 0;

  loadReviews(productId: number) {
    this.productService.getProductReviews(productId, 0, 10).subscribe({
      next: (res) => {
        // Assuming response structure has content as array and totalElements
        this.reviews = res.content || [];
        this.totalReviews = res.totalElements || 0;
        this.calculateAverageRating();
        this.cdr.detectChanges();
      },
      error: (err) => {
        console.error('Error loading reviews:', err);
      }
    });
  }

  calculateAverageRating() {
    if (this.reviews.length === 0) {
      this.averageRating = 0;
      return;
    }
    const sum = this.reviews.reduce((acc, review) => acc + review.rating, 0);
    this.averageRating = sum / this.reviews.length;
  }

  openZoom() {
    this.isZoomOpen = true;
  }

  closeZoom() {
    this.isZoomOpen = false;
  }

  changeImage(img: string) {
    this.mainImage = img;
  }

increaseQty() {
  if (this.product && this.quantity < this.product.stockQuantity) {
    this.quantity++;
  }
}

  decreaseQty() {
    if (this.quantity > 1) this.quantity--;
  }

  addToCart() {
    if (this.product) {
      this.cartService.addToCart(this.product!, this.quantity);
    }
  }
}