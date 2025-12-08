import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../environments/environment';

@Injectable({ providedIn: 'root' })
export class UserService {
  private apiUrl = `${environment.apiUrl}`;

  constructor(private http: HttpClient) {}

  getMyProfile() {
    return this.http.get<any>(`${this.apiUrl}/profile/me`);
  }

  updateProfile(data: any) {
    return this.http.put<any>(`${this.apiUrl}/profile/update`, data);
  }

  changePassword(data: any) {
    return this.http.put(`${this.apiUrl}/profile/change-password`, data);
  }

  uploadAvatar(file: File) {
    const formData = new FormData();
    formData.append('file', file);
    return this.http.post<any>(`${this.apiUrl}/upload/avatar`, formData);
  }
}