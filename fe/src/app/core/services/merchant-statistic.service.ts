import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { environment } from '../../../environments/environment';
import { Observable } from 'rxjs';
import { StatisticResponse } from '../models/StatisticResponse';




@Injectable({ providedIn: 'root' })
export class MerchantStatisticService {
  private http = inject(HttpClient);
  private apiUrl = `${environment.apiUrl}/merchant/statistics`;

  // Gọi API: GET /api/merchant/statistics/revenue?type=...
  getRevenueStats(type: 'WEEK' | 'MONTH' | 'YEAR', month?: number, year?: number): Observable<StatisticResponse[]> {
    let params = new HttpParams().set('type', type);
    
    if (month) params = params.set('month', month);
    if (year) params = params.set('year', year);

    return this.http.get<StatisticResponse[]>(`${this.apiUrl}/revenue`, { params });
  }
}