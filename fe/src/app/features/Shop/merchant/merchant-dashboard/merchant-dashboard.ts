import { Component, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { ShopService } from '../../../../core/services/shop.Service'; 

@Component({
  selector: 'app-merchant-dashboard',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './merchant-dashboard.html',
  styleUrls: ['./merchant-dashboard.css']
})
export class MerchantDashboardComponent implements OnInit {
  private shopService = inject(ShopService);

  shop: any = null;
  currentDate = new Date();

  // Mock Data: Thống kê
  stats = {
    revenue: 12450.00,
    revenueGrowth: 12.5,
    orders: 340,
    ordersGrowth: 5.2,
    products: 52,
    newProducts: 0,
    rating: 4.8,
    ratingCount: 120
  };

  // Mock Data: Đơn hàng gần đây
  recentOrders = [
    {
      id: '#ORD-00345',
      product: 'Wireless Headphones',
      customer: 'Sarah Smith',
      avatar: 'https://i.pravatar.cc/150?img=5',
      date: 'Oct 12, 2023',
      amount: 129.00,
      status: 'COMPLETED'
    },
    {
      id: '#ORD-00344',
      product: 'Smart Watch Series 5',
      customer: 'Michael Chen',
      avatar: 'https://i.pravatar.cc/150?img=3',
      date: 'Oct 11, 2023',
      amount: 249.50,
      status: 'PENDING'
    },
    {
      id: '#ORD-00343',
      product: 'Gaming Mouse',
      customer: 'Tom Hardy',
      avatar: 'https://i.pravatar.cc/150?img=12',
      date: 'Oct 10, 2023',
      amount: 59.99,
      status: 'CANCELLED'
    },
    {
      id: '#ORD-00342',
      product: 'Mechanical Keyboard',
      customer: 'Jessica Lee',
      avatar: 'https://i.pravatar.cc/150?img=1',
      date: 'Oct 09, 2023',
      amount: 89.99,
      status: 'SHIPPING'
    }
  ];

  ngOnInit() {
    this.getShopInfo();
  }

  getShopInfo() {
    this.shopService.getCurrentShop().subscribe({
      next: (res) => this.shop = res,
      error: (err) => console.error(err)
    });
  }

  // Helper: Màu sắc trạng thái đơn hàng
  getStatusClass(status: string): string {
    switch (status) {
      case 'COMPLETED': return 'bg-success bg-opacity-10 text-success';
      case 'PENDING': return 'bg-warning bg-opacity-10 text-warning';
      case 'CANCELLED': return 'bg-danger bg-opacity-10 text-danger';
      case 'SHIPPING': return 'bg-primary bg-opacity-10 text-primary';
      default: return 'bg-secondary bg-opacity-10 text-secondary';
    }
  }
}