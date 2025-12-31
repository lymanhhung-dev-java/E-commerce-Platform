import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../environments/environment';
import { Observable } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class CheckoutService {
  private http = inject(HttpClient);
  private apiUrl = `${environment.apiUrl}/checkout`;

  checkout(data: any) {
    return this.http.post<number[]>(this.apiUrl, data);
  }

  getPaymentQrUrl(orderId: number): Observable<string> {
    return this.http.get(`${this.apiUrl}/${orderId}/payment-qr`, { responseType: 'text' });
  }

  // 2. Kiểm tra trạng thái thanh toán
  checkPaymentStatus(orderId: number): Observable<boolean> {
    return this.http.get<boolean>(`${this.apiUrl}/${orderId}/payment-status`);
  }
  
  cancelOrder(orderId: number): Observable<any> {
    return this.http.post(`${this.apiUrl}/${orderId}/cancel`, {});
  }
}