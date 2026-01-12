import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { environment } from '../../../environments/environment';

@Injectable({ providedIn: 'root' })
export class ShopService {
  private http = inject(HttpClient);
  private apiUrl = `${environment.apiUrl}`;
  private uploadUrl = `${environment.apiUrl}/upload/avatar`;

  registerShop(data: any) {
    return this.http.post(`${this.apiUrl}/merchant/shops/register`, data);
  }

  getCurrentShop() {
    return this.http.get<any>(`${this.apiUrl}/merchant/shops/current`);
  }

  uploadLogo(file: File) {
    const formData = new FormData();
    formData.append('file', file);
    return this.http.post<{ url: string }>(this.uploadUrl, formData);
  }

  getPendingShops(page: number, size: number) {
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());

    return this.http.get<any>(`${this.apiUrl}/admin/shops/requests`, { params });
  }
  getShopsForAdmin(keyword: string, status: string, page: number, size: number) {
    let params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());

    if (keyword) params = params.set('keyword', keyword);
    if (status) params = params.set('status', status);

    return this.http.get<any>(`${this.apiUrl}/admin/shops`, { params });
  }

  approveShop(shopId: number, isApproved: boolean) {
    const params = new HttpParams().set('isApproved', isApproved);

    return this.http.put(
      `${this.apiUrl}/admin/shops/${shopId}/approve`,
      {},
      { params, responseType: 'text' }
    );
  }

  banShop(id: number) {
    return this.http.put(`${this.apiUrl}/admin/shops/${id}/ban`, {}, { responseType: 'text' });
  }

  resubmitShop(data: any) {
    return this.http.put(`${this.apiUrl}/merchant/shops/resubmit`, data);
  }

  updateShopInfo(data: any) {
    return this.http.put(`${this.apiUrl}/merchant/shops/info`, data);
  }
}