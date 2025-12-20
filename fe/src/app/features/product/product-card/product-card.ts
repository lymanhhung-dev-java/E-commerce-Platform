import { Component, Input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { Product } from '../../../core/models/product';

@Component({
  selector: 'app-product-card',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './product-card.html', // Trỏ đến file HTML
  styleUrl: './product-card.css'      // Trỏ đến file CSS
})
export class ProductCardComponent {
  @Input() product!: Product;
}