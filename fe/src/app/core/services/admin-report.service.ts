import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

export interface ReportResponse {
  id: number;
  username: string;
  shopName: string;
  orderId: number;
  reasonType: string;
  description: string;
  status: 'PENDING' | 'RESOLVED' | 'REJECTED';
  adminNote: string;
  createdAt: string;
}

@Injectable({
  providedIn: 'root'
})
export class AdminReportService {
  private http = inject(HttpClient);
  private apiUrl = `${environment.apiUrl}/admin/reports`;

  getAllReports(): Observable<ReportResponse[]> {
    return this.http.get<ReportResponse[]>(this.apiUrl);
  }

  resolveReport(id: number, resolution: 'RESOLVED' | 'REJECTED', adminNote: string): Observable<string> {
    return this.http.put(`${this.apiUrl}/${id}/resolve`, { resolution, adminNote }, { responseType: 'text' });
  }
}
