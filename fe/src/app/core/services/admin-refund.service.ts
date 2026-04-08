import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface Refund {
  id: number;
  orderId: number;
  amount: number;
  customerBankInfo: string;
  reason: string;
  status: string;
  createdAt: string;
}

@Injectable({
  providedIn: 'root'
})
export class AdminRefundService {
  private apiUrl = 'http://localhost:8080/api/admin/refunds';

  constructor(private http: HttpClient) {}

  getPendingRefunds(): Observable<Refund[]> {
    return this.http.get<Refund[]>(`${this.apiUrl}/pending`);
  }

  confirmRefund(refundId: number): Observable<string> {
    return this.http.post(`${this.apiUrl}/${refundId}/confirm`, null, { responseType: 'text' });
  }
}
