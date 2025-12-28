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
    { label: 'Dashboard', icon: 'bi-grid-fill', route: '/admin/dashboard' },
    { label: 'Users', icon: 'bi-people-fill', route: '/admin/users' },
    { label: 'Shops Approvals', icon: 'bi-shop', route: '/admin/shopsApprovals' },
    { label: 'Shops ', icon: 'bi-shop', route: '/admin/shops' },
    { label: 'Categories', icon: 'bi-tags-fill', route: '/admin/categories' },
    { label: 'Products', icon: 'bi-box-seam-fill', route: '/admin/products' },
    

  ];
  
  systemItems = [
     { label: 'Settings', icon: 'bi-gear-fill', route: '/admin/settings' },
     { label: 'Help Center', icon: 'bi-question-circle-fill', route: '/admin/help' },
  ];
}