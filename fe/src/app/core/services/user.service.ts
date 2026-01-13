import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { environment } from '../../../environments/environment';

@Injectable({ providedIn: 'root' })
export class UserService {
  private apiUrl = `${environment.apiUrl}`;

  constructor(private http: HttpClient) { }

  getMyProfile() {
    return this.http.get<any>(`${this.apiUrl}/profile/me`);
  }

  updateProfile(data: any) {
    return this.http.put<any>(`${this.apiUrl}/profile/update`, data);
  }

  changePassword(data: any) {
    return this.http.put(`${this.apiUrl}/profile/change-password`, data, { responseType: 'text' });
  }

  uploadAvatar(file: File) {
    const formData = new FormData();
    formData.append('file', file);
    return this.http.post<any>(`${this.apiUrl}/upload/avatar`, formData);
  }

  getAllUsers(page: number, size: number, keyword?: string, status?: string, isShopOwner?: boolean) {
    let params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());

    if (keyword) params = params.set('keyword', keyword);
    if (status && status !== 'ALL') params = params.set('status', status);
    if (isShopOwner !== undefined && isShopOwner !== null) {
      params = params.set('isShopOwner', isShopOwner.toString());
    }

    return this.http.get<any>(`${this.apiUrl}/admin/users`, { params });

  }
  // Gọi PUT /api/admin/users/{id}/status?status=...
  updateUserStatus(userId: number, status: 'ACTIVE' | 'INACTIVE') {

    return this.http.put(
      `${this.apiUrl}/admin/users/${userId}/status`,
      {},
      {
        params: { status: status },
        responseType: 'text'
      }
    );
  }
}