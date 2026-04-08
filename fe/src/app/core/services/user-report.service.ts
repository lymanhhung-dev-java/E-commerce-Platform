import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

export interface ReportCreateRequest {
  shopId?: number;
  orderId?: number;
  reasonType: string;
  description: string;
}

@Injectable({
  providedIn: 'root'
})
export class UserReportService {
  private http = inject(HttpClient);
  private apiUrl = `${environment.apiUrl}/reports`;

  createReport(request: ReportCreateRequest): Observable<string> {
    return this.http.post(this.apiUrl, request, { responseType: 'text' });
  }
}
