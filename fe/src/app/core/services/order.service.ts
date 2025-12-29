import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { environment } from '../../../environments/environment';
import { Order } from '../models/order';
import { Observable } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class OrderService {
  private http = inject(HttpClient);
  private apiUrl = `${environment.apiUrl}/orders`; 
  private apiMerchantUrl = `${environment.apiUrl}/merchant/orders`; 


  getMyOrders(page: number, size: number, search?: string, status?: string) {
    let params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());

    if (search) params = params.set('search', search);
    if (status && status !== 'ALL') params = params.set('status', status);

    return this.http.get<any>(`${this.apiUrl}/my-orders`, { params });
  }

  //-------------------------------------Merchant--------------------------------------.

  getShopOrders(page: number = 0, size: number = 10){
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());

    return this.http.get<any>(this.apiMerchantUrl, { params });
  }

  updateOderStatus(orderId: number, status: string) : Observable<String> {
    const params = new HttpParams().set('status', status);
    return this.http.put(`${this.apiMerchantUrl}/${orderId}/status`, {}, { 
      params, 
      responseType: 'text' });
  }

  


}