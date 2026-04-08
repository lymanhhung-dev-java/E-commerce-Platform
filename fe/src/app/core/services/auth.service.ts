import { Injectable, signal, inject, PLATFORM_ID } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../environments/environment';
import { tap } from 'rxjs';
import { Router } from '@angular/router';
import { isPlatformBrowser } from '@angular/common';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private apiUrl = `${environment.apiUrl}/auth`;
  private platformId = inject(PLATFORM_ID); // 1. Inject PLATFORM_ID

  // 2. Khởi tạo mặc định là false để tránh lỗi trên Server
  isLoggedIn = signal<boolean>(false);

  constructor(private http: HttpClient, private router: Router) {
    // 3. Chỉ kiểm tra token nếu đang ở trình duyệt
    if (isPlatformBrowser(this.platformId)) {
      this.isLoggedIn.set(this.hasToken());
    }
  }

  private hasToken(): boolean {
    if (isPlatformBrowser(this.platformId)) {
      return !!localStorage.getItem('access_token');
    }
    return false;
  }

  login(credentials: any) {
    return this.http.post<any>(`${this.apiUrl}/acces-token`, credentials).pipe(
      tap(response => {
        if (isPlatformBrowser(this.platformId)) {
          localStorage.setItem('access_token', response.accessToken);
          localStorage.setItem('refresh_token', response.refreshToken);
        }
        this.isLoggedIn.set(true);
      })
    );
  }

  register(userData: any) {
    // Expected to return a text response (e.g. "Mã xác thực đã được gửi...")
    return this.http.post(`${this.apiUrl}/register`, userData, { responseType: 'text' });
  }

  verifyRegister(email: string, otp: string) {
    return this.http.post(`${this.apiUrl}/verify-register`, { email, otp });
  }

  loginWithGoogle(code: string) {
    return this.http.post<any>(`${this.apiUrl}/google`, { code }).pipe(
      tap(response => {
        if (isPlatformBrowser(this.platformId)) {
          localStorage.setItem('access_token', response.accessToken);
          localStorage.setItem('refresh_token', response.refreshToken);
        }
        this.isLoggedIn.set(true);
      })
    );
  }

  sendForgotPasswordCode(email: string) {
    return this.http.post(`${this.apiUrl}/forgot-password/send-code`, { email }, { responseType: 'text' });
  }

  resetPassword(payload: { email: string; otp: string; newPassword: string }) {
    return this.http.post(`${this.apiUrl}/forgot-password/reset`, payload, { responseType: 'text' });
  }

  logout() {
    if (isPlatformBrowser(this.platformId)) {
      localStorage.removeItem('access_token');
      localStorage.removeItem('refresh_token');
    }
    this.isLoggedIn.set(false);
    this.router.navigate(['/login']);
  }
}