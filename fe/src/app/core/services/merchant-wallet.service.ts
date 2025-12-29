import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../environments/environment';
import { Observable } from 'rxjs';

// DTO khớp với Backend
export interface WalletOverviewResponse {
  totalRevenue: number;
  periodRevenue: number;
  currentBalance: number;
}

export interface WithdrawRequest {
  amount: number;
  bankName: string;
  accountNumber: string;
  accountName?: string;
}

@Injectable({ providedIn: 'root' })
export class MerchantWalletService {
  private http = inject(HttpClient);
  private apiUrl = `${environment.apiUrl}/merchant/wallet`; 

  // 1. Lấy thông tin tổng quan ví
  getWalletOverview(): Observable<WalletOverviewResponse> {
    return this.http.get<WalletOverviewResponse>(`${this.apiUrl}/overview`);
  }

  // 2. Gửi yêu cầu rút tiền
  requestWithdraw(data: WithdrawRequest): Observable<string> {
    return this.http.post(`${this.apiUrl}/withdraw`, data, { responseType: 'text' });
  }

  // 3. Lấy lịch sử rút tiền (Nếu cần)
  getWithdrawHistory(page: number = 0, size: number = 10): Observable<any> {
     return this.http.get<any>(`${this.apiUrl}/history?page=${page}&size=${size}`);
  }
}