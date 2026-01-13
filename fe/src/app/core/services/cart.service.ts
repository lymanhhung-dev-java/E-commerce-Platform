import { Injectable, computed, signal, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../environments/environment';
import { Product } from '../models/product';
import { CartItem } from '../models/cart';
import { ToastrService } from 'ngx-toastr';
import { forkJoin, of } from 'rxjs';
import { map, switchMap, tap } from 'rxjs/operators';
import { ProductService } from './product.service';
import { AuthService } from './auth.service';
import { Router } from '@angular/router';

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
  private productService = inject(ProductService);
  private router = inject(Router);
  private authService = inject(AuthService);

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
    // Left empty intentionally to let HeaderComponent control loading
  }

  // 1. Helper: Observable lấy giỏ hàng (tách ra để tái sử dụng)
  private fetchCart() {
    if (!this.authService.isLoggedIn()) {
      return of([]);
    }
    return this.http.get<CartItemResponse[]>(this.apiUrl).pipe(
      switchMap(response => {
        if (!response || response.length === 0) {
          return of([]);
        }

        const requests = response.map(item =>
          this.productService.getProductById(item.productId).pipe(
            map(product => ({
              ...item,
              shopName: product.shopName || 'Shop',
              stockQuantity: product.stockQuantity
            }))
          )
        );

        return forkJoin(requests);
      }),
      map(updatedItems => {
        return updatedItems.map((item: any) => ({
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
        } as CartItem));
      })
    );
  }

  // Reload cart (giữ nguyên behavior cũ: gọi và update signal)
  loadCart() {
    this.fetchCart().subscribe({
      next: (items) => this.cartItems.set(items),
      error: (err) => {
        if (err.status === 401) {
          this.cartItems.set([]);
        } else {
          console.error('Lỗi tải giỏ hàng:', err);
        }
      }
    });
  }

  // --- LOGIC MUA NGAY ---
  buyNow(product: Product, quantity: number) {
    // Check stock cơ bản ở client trước
    if (quantity > product.stockQuantity) {
      this.toastr.warning('Số lượng mua vượt quá tồn kho');
      return;
    }

    // 1. Gọi API thêm vào giỏ
    this.http.post(`${this.apiUrl}/add`, {
      productId: product.id,
      quantity: quantity
    }, { responseType: 'text' })
      .pipe(
        // 2. Sau khi thêm thành công, load lại giỏ mới nhất
        switchMap(() => this.fetchCart())
      )
      .subscribe({
        next: (items) => {
          // 3. Update signal logic: chỉ chọn item vừa mua
          const updatedItems = items.map(item => {
            if (item.product.id === product.id) {
              return { ...item, selected: true }; // Chọn item này
            }
            return { ...item, selected: false }; // Bỏ chọn các item khác
          });

          this.cartItems.set(updatedItems);

          // 4. Chuyển hướng sang Checkout
          this.toastr.success('Đang chuyển đến trang thanh toán...');
          this.router.navigate(['/checkout']);
        },
        error: (err) => {
          if (err.status === 401) {
            this.toastr.warning('Vui lòng đăng nhập để mua hàng');
            this.router.navigate(['/login']);
          } else {
            const msg = err.error || err.message || 'Lỗi khi mua ngay';
            this.toastr.error(msg);
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

