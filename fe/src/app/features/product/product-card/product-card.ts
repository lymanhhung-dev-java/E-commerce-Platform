import { Component, Input, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { Product } from '../../../core/models/product';
import { CartService } from '../../../core/services/cart.service';
import { ToastrService } from 'ngx-toastr';

@Component({
  selector: 'app-product-card',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './product-card.html',
  styleUrl: './product-card.css'
})
export class ProductCardComponent {
  @Input() product!: Product;

  cartService = inject(CartService);
  toastr = inject(ToastrService);

  addToCart(event: Event) {
    event.stopPropagation();
    event.preventDefault();
    this.cartService.addToCart(this.product, 1).subscribe({
      next: () => { },
      error: (err) => {
        this.toastr.error(
          err?.error?.message || 'Sản phẩm đã hết hàng'
        );
      }
    });
  }
}