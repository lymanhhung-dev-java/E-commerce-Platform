import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../environments/environment';

@Injectable({ providedIn: 'root' })
export class CheckoutService {
  private http = inject(HttpClient);
  private apiUrl = `${environment.apiUrl}/checkout`;

  // Backend trả về List<Long> -> Frontend hứng number[]
  checkout(data: any) {
    return this.http.post<number[]>(this.apiUrl, data);
  }
}