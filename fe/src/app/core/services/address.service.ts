import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../environments/environment';
import { Address } from '../models/address';

@Injectable({ providedIn: 'root' })
export class AddressService {
  private http = inject(HttpClient);
  private apiUrl = `${environment.apiUrl}/addresses`;

  getMyAddresses() {
    return this.http.get<Address[]>(this.apiUrl);
  }

  createAddress(address: Address) {
    return this.http.post<Address>(this.apiUrl, address);
  }

  updateAddress(id: number, address: Address) {
    return this.http.put<Address>(`${this.apiUrl}/${id}`, address);
  }

  deleteAddress(id: number) {
    return this.http.delete(`${this.apiUrl}/${id}`, { responseType: 'text' });
  }
}