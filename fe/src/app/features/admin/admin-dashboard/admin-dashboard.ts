import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { RouterModule } from '@angular/router';
import { environment } from '../../../../environments/environment';

@Component({
  selector: 'app-admin-dashboard',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './admin-dashboard.html'
})
export class AdminDashboardComponent implements OnInit {
  private http = inject(HttpClient);
  
  stats: any = {
    totalUsers: 0,
    totalShops: 0,
    pendingShopRequests: 0,
    pendingWithdrawals: 0,
    totalRevenue: 0
  };

  ngOnInit() {
    this.loadStats();
  }

  loadStats() {
    this.http.get(`${environment.apiUrl}/admin/dashboard/stats`)
      .subscribe({
        next: (res: any) => this.stats = res,
        error: (err) => console.error('Lỗi tải stats dashboard', err)
      });
  }
}