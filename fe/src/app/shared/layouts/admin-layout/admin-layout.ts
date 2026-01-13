import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterOutlet, RouterLink, RouterLinkActive } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';

@Component({
  selector: 'app-admin-layout',
  standalone: true,
  imports: [CommonModule, RouterOutlet, RouterLink, RouterLinkActive],
  templateUrl: './admin-layout.html',
  styleUrl: './admin-layout.css'
})
export class AdminLayoutComponent {
  authService = inject(AuthService);

  menuItems = [
    { label: 'Trang chủ', icon: 'bi-grid-fill', route: '/admin/dashboard' },
    { label: 'Người dùng', icon: 'bi-people-fill', route: '/admin/users' },
    { label: 'Yêu cầu ', icon: 'bi-shop', route: '/admin/shopsApprovals' },
    { label: 'Quản lý cửa hàng', icon: 'bi-shop', route: '/admin/shops' },
    { label: 'Quản lý danh mục', icon: 'bi-tags-fill', route: '/admin/categories' },
    { label: 'Quản lý sản phẩm', icon: 'bi-box-seam-fill', route: '/admin/products' },
    { label: 'Yêu cầu rút tiền', icon: 'bi-bag-fill', route: '/admin/wallets' },
  ];
  
  systemItems = [
     { label: 'Cài đặt', icon: 'bi-gear-fill', route: '/admin/settings' },
     { label: 'Trung tâm hỗ trợ', icon: 'bi-question-circle-fill', route: '/admin/help' },
  ];
}