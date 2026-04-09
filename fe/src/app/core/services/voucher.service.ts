import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Voucher, VoucherRequest } from '../models/voucher';
import { environment } from '../../../environments/environment';

export interface PageableResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
}

@Injectable({
  providedIn: 'root'
})
export class VoucherService {
  private adminApiUrl = `${environment.apiUrl}/admin/vouchers`;
  private merchantApiUrl = `${environment.apiUrl}/merchant/vouchers`;
  private userApiUrl = `${environment.apiUrl}/user/vouchers`;

  constructor(private http: HttpClient) {}

  // ================= ADMIN APIs =================
  getAdminVouchers(page: number = 0, size: number = 10, keyword?: string): Observable<PageableResponse<Voucher>> {
    let params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());
    if (keyword) {
      params = params.set('keyword', keyword);
    }
    return this.http.get<PageableResponse<Voucher>>(this.adminApiUrl, { params });
  }

  getAdminVoucherById(id: number): Observable<Voucher> {
    return this.http.get<Voucher>(`${this.adminApiUrl}/${id}`);
  }

  createAdminVoucher(request: VoucherRequest): Observable<Voucher> {
    return this.http.post<Voucher>(this.adminApiUrl, request);
  }

  updateAdminVoucher(id: number, request: VoucherRequest): Observable<Voucher> {
    return this.http.put<Voucher>(`${this.adminApiUrl}/${id}`, request);
  }

  deleteAdminVoucher(id: number): Observable<string> {
    return this.http.delete(`${this.adminApiUrl}/${id}`, { responseType: 'text' });
  }

  toggleAdminVoucherStatus(id: number): Observable<string> {
    return this.http.patch(`${this.adminApiUrl}/${id}/toggle-status`, {}, { responseType: 'text' });
  }

  // ================= MERCHANT APIs =================
  getMerchantVouchers(page: number = 0, size: number = 10, keyword?: string): Observable<PageableResponse<Voucher>> {
    let params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());
    if (keyword) {
      params = params.set('keyword', keyword);
    }
    return this.http.get<PageableResponse<Voucher>>(this.merchantApiUrl, { params });
  }

  getMerchantVoucherById(id: number): Observable<Voucher> {
    return this.http.get<Voucher>(`${this.merchantApiUrl}/${id}`);
  }

  createMerchantVoucher(request: VoucherRequest): Observable<Voucher> {
    return this.http.post<Voucher>(this.merchantApiUrl, request);
  }

  updateMerchantVoucher(id: number, request: VoucherRequest): Observable<Voucher> {
    return this.http.put<Voucher>(`${this.merchantApiUrl}/${id}`, request);
  }

  deleteMerchantVoucher(id: number): Observable<string> {
    return this.http.delete(`${this.merchantApiUrl}/${id}`, { responseType: 'text' });
  }

  toggleMerchantVoucherStatus(id: number): Observable<string> {
    return this.http.patch(`${this.merchantApiUrl}/${id}/toggle-status`, {}, { responseType: 'text' });
  }

  // ================= USER APIs =================
  saveVoucher(code: string): Observable<any> {
    return this.http.post<any>(`${this.userApiUrl}/save/${code}`, {});
  }

  getMySavedVouchers(page: number = 0, size: number = 10): Observable<any> {
    let params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());
    return this.http.get<any>(this.userApiUrl, { params });
  }

  getShopPublicVouchers(shopId: number): Observable<Voucher[]> {
    return this.http.get<Voucher[]>(`${environment.apiUrl}/shops/${shopId}/vouchers`);
  }
}
