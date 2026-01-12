import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { CartService } from '../../core/services/cart.service';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-cart',
  standalone: true,
  imports: [CommonModule, RouterLink, FormsModule],
  templateUrl: './cart.html',
  styleUrl: './cart.css'
})
export class CartComponent {
  cartService = inject(CartService);

  shippingCost = 0;
  taxEstimate = 55.04;
  discount = 20.00;

  get finalTotal() {
    return this.cartService.subTotalSelected()
      + this.taxEstimate
      - this.discount;
  }

  increaseQty(itemId: number, currentQty: number) {
    this.cartService.updateQuantity(itemId, currentQty + 1);
  }

decreaseQty(itemId: number, currentQty: number) {
  if (currentQty <= 1) {
    return;
  }
  this.cartService.updateQuantity(itemId, currentQty - 1);
}

  removeItem(itemId: number) {
    if (confirm('Bạn có chắc muốn xóa sản phẩm này?')) {
      this.cartService.removeFromCart(itemId);
    }
  }
}
