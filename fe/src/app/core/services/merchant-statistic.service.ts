import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { environment } from '../../../environments/environment';
import { Observable } from 'rxjs';
import { StatisticResponse } from '../models/StatisticResponse';

export interface FinancialReportResponse {
  totalOriginalRevenue: number;
  totalShopVoucherDiscount: number;
  totalCommissionFee: number;
  actualBalanceAdded: number;
  month?: number;
  year?: number;
}

@Injectable({ providedIn: 'root' })
export class MerchantStatisticService {
  private http = inject(HttpClient);
  private apiUrl = `${environment.apiUrl}/merchant/statistics`;

  /** GET /api/merchant/statistics/revenue?type=... */
  getRevenueStats(type: 'WEEK' | 'MONTH' | 'YEAR', month?: number, year?: number): Observable<StatisticResponse[]> {
    let params = new HttpParams().set('type', type);
    if (month) params = params.set('month', month);
    if (year) params = params.set('year', year);
    return this.http.get<StatisticResponse[]>(`${this.apiUrl}/revenue`, { params });
  }

  /** GET /api/merchant/statistics/financial-report  (all-time) */
  getFinancialReport(): Observable<FinancialReportResponse> {
    return this.http.get<FinancialReportResponse>(`${this.apiUrl}/financial-report`);
  }

  /** GET /api/merchant/statistics/financial-report/monthly?month=&year=  */
  getMonthlyFinancialReport(month?: number, year?: number): Observable<FinancialReportResponse> {
    let params = new HttpParams();
    if (month) params = params.set('month', month);
    if (year) params = params.set('year', year);
    return this.http.get<FinancialReportResponse>(`${this.apiUrl}/financial-report/monthly`, { params });
  }

  getDashboardActions(): Observable<MerchantDashboardActionResponse> {
    return this.http.get<MerchantDashboardActionResponse>(`${this.apiUrl}/dashboard-actions`);
  }
}

export interface MerchantDashboardActionResponse {
  todayRevenue: number;
  newOrdersToday: number;
  lowStockProductCount: number;
  pendingOrderCount: number;
  unreadMessageCount: number;
  frozenBalance: number;
  availableBalance: number;
}