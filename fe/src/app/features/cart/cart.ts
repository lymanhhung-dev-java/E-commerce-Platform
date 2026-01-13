import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { CartService } from '../../core/services/cart.service';
import { FormsModule } from '@angular/forms';
import { ToastrService } from 'ngx-toastr';

@Component({
  selector: 'app-cart',
  standalone: true,
  imports: [CommonModule, RouterLink, FormsModule],
  templateUrl: './cart.html',
  styleUrl: './cart.css'
})
export class CartComponent {
  cartService = inject(CartService);
  toastr = inject(ToastrService);

  shippingCost = 25000;
  discount = 20.00;

  get finalTotal() {
    return this.cartService.subTotalSelected()
      + this.shippingCost
      - this.discount;
  }

  increaseQty(itemId: number, currentQty: number) {
    const item = this.cartService.cartItems().find(i => i.product.id === itemId);

    if (item && currentQty >= item.product.stockQuantity) {
      this.toastr.warning('Số lượng bạn chọn đã đạt mức tối đa của sản phẩm này');
      return;
    }

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
