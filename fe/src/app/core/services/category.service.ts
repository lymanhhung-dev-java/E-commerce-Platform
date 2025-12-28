import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../environments/environment';
import { Category } from '../models/category';

@Injectable({ providedIn: 'root' })
export class CategoryService {
  private http = inject(HttpClient);
  private apiUrl = `${environment.apiUrl}/categories`; // API: /api/categories
  private adminUrl  = `${environment.apiUrl}/admin/categories`;


  getCategories() {
    return this.http.get<Category[]>(this.apiUrl);
  }

  getFlatCategories() {
    return this.http.get<any[]>(`${this.apiUrl}/flat`);
  }
  getAllCategories() {
    return this.http.get<any[]>(`${this.apiUrl}/flat`);
  }
  createCategory(data: any) {
    return this.http.post(this.adminUrl, data);
  }
  updateCategory(id: number, data: any) {
    return this.http.put(`${this.adminUrl}/${id}`, data);
  }
  deleteCategory(id: number) {
    return this.http.delete(`${this.adminUrl}/${id}`, { responseType: 'text' });
  }

}