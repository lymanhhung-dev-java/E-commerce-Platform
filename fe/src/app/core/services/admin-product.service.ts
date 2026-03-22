import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { environment } from '../../../environments/environment';
import { Observable } from 'rxjs';

export interface AdminProductResponse {
  id: number;
  name: string;
  price: number;
  imageUrl: string;
  shopName: string;
  categoryName: string;
  isActive: boolean; 
}

@Injectable({ providedIn: 'root' })
export class AdminProductService {
  private http = inject(HttpClient);
  private apiUrl = `${environment.apiUrl}/admin/products`;

  getProducts(
    page: number = 0,
    size: number = 10,
    keyword?: string,
    status?: boolean,
    shopId?: number,
    minPrice?: number,
    maxPrice?: number
  ): Observable<any> {
    let params = new HttpParams()
      .set('page', page)
      .set('size', size)
      .set('sort', 'createdAt,desc');

    if (keyword) params = params.set('keyword', keyword);
    if (status !== undefined && status !== null) {
      params = params.set('status', status);
    }
    if (shopId) params = params.set('shopId', shopId);
    if (minPrice !== undefined && minPrice !== null) params = params.set('minPrice', minPrice);
    if (maxPrice !== undefined && maxPrice !== null) params = params.set('maxPrice', maxPrice);

    return this.http.get<any>(this.apiUrl, { params });
  }

  toggleProductStatus(id: number): Observable<string> {
    return this.http.put(`${this.apiUrl}/${id}/toggle-status`, {}, { responseType: 'text' });
  }
}