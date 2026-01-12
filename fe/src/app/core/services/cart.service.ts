import { Injectable, computed, signal, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../environments/environment';
import { Product } from '../models/product';
import { CartItem } from '../models/cart';
import { ToastrService } from 'ngx-toastr';

interface CartItemResponse {
  id: number;
  productId: number;
  productName: string;
  productImageUrl: string;
  quantity: number;
  price: number;
  subTotal: number;
  stockQuantity: number;
  shopName: string;
}

@Injectable({ providedIn: 'root' })
export class CartService {
  private http = inject(HttpClient);
  private toastr = inject(ToastrService);

  // URL API: http://localhost:8080/api/cart
  private apiUrl = `${environment.apiUrl}/cart`;


  cartItems = signal<CartItem[]>([]);


  totalCount = computed(() =>
    this.cartItems().length
  );

  subTotal = computed(() =>
    this.cartItems().reduce((acc, item) => acc + (item.product.price * item.quantity), 0)
  );

  totalAmount = computed(() => this.subTotal());

  constructor() {
    this.loadCart();
  }

  // 1. Lấy giỏ hàng từ Backend (GET /api/cart)
  loadCart() {
    this.http.get<CartItemResponse[]>(this.apiUrl).subscribe({
      next: (response) => {
        const mappedItems: CartItem[] = response.map(item => ({
          product: {
            id: item.productId,
            name: item.productName,
            price: item.price,
            imageUrl: item.productImageUrl,
            stockQuantity: item.stockQuantity,
            shopName: item.shopName
          } as Product,
          quantity: item.quantity,
          selected: false
        }));

        this.cartItems.set(mappedItems);
      },
      error: (err) => {
        if (err.status === 401) {
          this.cartItems.set([]);
        } else {
          console.error('Lỗi tải giỏ hàng:', err);
        }
      }
    });
  }

  // 2. Thêm vào giỏ (POST /api/cart/add)
  addToCart(product: Product, quantity: number = 1) {
    const currentItem = this.cartItems().find(item => item.product.id === product.id);
    const currentQty = currentItem ? currentItem.quantity : 0;

    if (currentQty + quantity > product.stockQuantity) {
      this.toastr.warning('Số lượng bạn chọn đã đạt mức tối đa của sản phẩm này');
      return;
    }

    this.http.post(`${this.apiUrl}/add`, {
      productId: product.id,
      quantity: quantity
    }, { responseType: 'text' })
      .subscribe({
        next: () => {
          this.toastr.success('Đã thêm vào giỏ hàng');
          this.loadCart();
        },
        error: (err) => {
          if (err.status === 401) {
            this.toastr.warning('Vui lòng đăng nhập để mua hàng');
          } else {
            // Try to handle text response if it's not JSON
            const msg = err.error || err.message || 'Lỗi khi thêm vào giỏ';
            this.toastr.error(msg);
          }
        }
      });
  }

  // 3. Cập nhật số lượng (PUT /api/cart/update)
  updateQuantity(productId: number, quantity: number) {
    if (quantity <= 0) {
      this.removeFromCart(productId);
      return;
    }

    const oldCart = this.cartItems();
    this.cartItems.update(items =>
      items.map(item => item.product.id === productId ? { ...item, quantity } : item)
    );

    this.http.put(`${this.apiUrl}/update`, {
      productId: productId,
      quantity: quantity
    }, { responseType: 'text' })
      .subscribe({
        error: (err) => {
          this.cartItems.set(oldCart);
          const msg = err.error?.message || 'Không thể cập nhật số lượng';
          this.toastr.error(msg);
        }
      });
  }

  // 4. Xóa sản phẩm (DELETE /api/cart/remove)
  removeFromCart(productId: number) {
    const oldCart = this.cartItems();
    this.cartItems.update(items => items.filter(i => i.product.id !== productId));

    this.http.delete(`${this.apiUrl}/remove`, {
      params: { productId: productId.toString() },
      responseType: 'text'
    }).subscribe({
      error: (err) => {
        this.cartItems.set(oldCart);
        this.toastr.error('Lỗi khi xóa sản phẩm');
      }
    });
  }

  clearCart() {
    this.cartItems.set([]);
  }

  selectedCount() {
    return this.cartItems().filter(i => i.selected).length;
  }

  subTotalSelected() {
    return this.cartItems()
      .filter(i => i.selected)
      .reduce(
        (sum, i) => sum + i.product.price * i.quantity,
        0
      );
  }
}

