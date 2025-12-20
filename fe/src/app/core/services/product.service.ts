import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { environment } from '../../../environments/environment';
import { Product, ProductResponse } from '../models/product';

@Injectable({ providedIn: 'root' })
export class ProductService {
  private http = inject(HttpClient);
  private apiUrl = `${environment.apiUrl}/products`; // API: http://localhost:8080/api/products

  getProducts(
    page: number = 0, 
    size: number = 10,
    search?: string,
    categoryId?: number,
    minPrice?: number,
    maxPrice?: number
  ) {
    let params = new HttpParams()
      .set('page', page)
      .set('size', size)
      .set('sort', 'createdAt,desc'); // Mặc định sort theo controller

    if (search) params = params.set('search', search);
    if (categoryId) params = params.set('categoryId', categoryId);
    if (minPrice) params = params.set('minPrice', minPrice);
    if (maxPrice) params = params.set('maxPrice', maxPrice);

    // Trả về ProductResponse (dạng Page của Spring)
    return this.http.get<ProductResponse>(this.apiUrl, { params });
  }


  // Tìm kiếm sản phẩm
  searchProducts(keyword: string) {
    return this.http.get<any>(`${this.apiUrl}/search?keyword=${keyword}`);
  }
  
  // Lấy chi tiết
  getProductById(id: number) {
    return this.http.get<Product>(`${this.apiUrl}/${id}`);
  }
}