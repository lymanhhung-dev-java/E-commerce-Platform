import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { environment } from '../../../environments/environment';
import { Observable } from 'rxjs';

export interface Withdrawal {
  id: number;
  shop: {
    id: number;
    name: string;
  };
  amount: number;
  bankName: string;
  accountNumber: string;
  accountName: string;
  accountType: string;
  status: 'PENDING' | 'APPROVED' | 'REJECTED';
  createdAt: string;
  updatedAt: string;
  rejectReason?: string;
}

@Injectable({ providedIn: 'root' })
export class AdminWithdrawalService {
  private http = inject(HttpClient);
  private apiUrl = `${environment.apiUrl}/admin/withdrawals`;
  getWithdrawals(page: number, size: number, status?: string): Observable<any> {
    let params = new HttpParams()
      .set('page', page)
      .set('size', size);

    if (status && status !== 'ALL') {
      params = params.set('status', status);
    }

    return this.http.get<any>(this.apiUrl, { params });
  }
  updateStatus(id: number, status: 'APPROVED' | 'REJECTED', rejectReason?: string): Observable<any> {
    const body = { status, rejectReason };
    return this.http.put(`${this.apiUrl}/${id}/status`, body, { responseType: 'text' });
  }
}