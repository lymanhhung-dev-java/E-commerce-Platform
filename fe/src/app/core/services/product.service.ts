import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { environment } from '../../../environments/environment';
import { Product, ProductResponse } from '../models/product';
import { map, Observable } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class ProductService {
  private http = inject(HttpClient);
  private apiUrl = `${environment.apiUrl}/products`;
  private apiMerchantUrl = `${environment.apiUrl}/merchant/products`;
  private apiUploadUrl = `${environment.apiUrl}/upload/product`;



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
      .set('sort', 'createdAt,desc');

    if (search) params = params.set('search', search);
    if (categoryId) params = params.set('categoryId', categoryId);
    if (minPrice) params = params.set('minPrice', minPrice);
    if (maxPrice) params = params.set('maxPrice', maxPrice);

    return this.http.get<ProductResponse>(this.apiUrl, { params });
  }

  searchProducts(keyword: string) {
    return this.http.get<any>(`${this.apiUrl}/search?keyword=${keyword}`);
  }

  getProductById(id: number) {
    return this.http.get<Product>(`${this.apiUrl}/${id}`);
  }

  getProductReviews(productId: number, page: number = 0, size: number = 5): Observable<any> {
    const params = new HttpParams()
      .set('page', page)
      .set('size', size)
      .set('sort', 'createdAt,desc');
    return this.http.get<any>(`${environment.apiUrl}/reviews/product/${productId}`, { params });
  }
  //----------------- // merchant // ---------------------------------------------------------------------------------------//
  getMerchantProducts(
    keyword: string,
    categoryId: number | null,
    status: boolean | null,
    page: number,
    size: number
  ): Observable<any> {
    let params = new HttpParams()
      .set('page', page)
      .set('size', size);

    if (keyword) params = params.set('keyword', keyword);
    if (categoryId) params = params.set('categoryId', categoryId);

    if (status !== null) {
      params = params.set('status', status);
    }

    return this.http.get<any>(this.apiMerchantUrl, { params });
  }

  uploadFile(file: File): Observable<string> {
    const formData = new FormData();
    formData.append('file', file);

    return this.http.post<any>(this.apiUploadUrl, formData).pipe(
      map(response => {
        return response.url || response.secure_url || response.data || response;
      })
    );
  }

  createProduct(data: any) {
    return this.http.post(this.apiMerchantUrl, data);
  }

  onToggleStatus(id: number) {
    return this.http.put(`${this.apiMerchantUrl}/${id}/status`, {}, { responseType: 'text' });
  }

  // Hàm xóa mềm (ẩn sản phẩm)
  deleteProduct(id: number) {
    return this.http.delete(`${this.apiMerchantUrl}/${id}`, { responseType: 'text' });
  }

  updateProduct(id: number, data: any) {
    return this.http.put(`${this.apiMerchantUrl}/${id}`, data);
  }

}