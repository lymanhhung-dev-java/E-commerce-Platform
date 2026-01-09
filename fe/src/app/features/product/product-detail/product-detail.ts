import { Component, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { ProductService } from '../../../core/services/product.service';
import { Product } from '../../../core/models/product';
import { ToastrService } from 'ngx-toastr';
import { CartService } from '../../../core/services/cart.service';

@Component({
  selector: 'app-detail-product',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './product-detail.html',
  styleUrl: './product-detail.css'
})
export class DetailProductComponent implements OnInit {
  private route = inject(ActivatedRoute);
  private productService = inject(ProductService);
  private toastr = inject(ToastrService);
  private  cartService = inject(CartService);

  product: Product | null = null;
  mainImage: string = '';
  quantity: number = 1;
  description : string = "";
  thumbnails: string[] = []; 
  isZoomOpen: boolean = false;

  ngOnInit() {
    this.route.paramMap.subscribe(params => {
      const id = Number(params.get('id'));
      if (id) {
        this.loadProduct(id);
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
    this.quantity++;
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