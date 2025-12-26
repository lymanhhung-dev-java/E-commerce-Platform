import { Component, Input , inject} from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { Product } from '../../../core/models/product';
import { CartService } from '../../../core/services/cart.service';

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

  addToCart( event : Event) {
    event.stopPropagation();
    event.preventDefault();
    this.cartService.addToCart(this.product);
  }

}