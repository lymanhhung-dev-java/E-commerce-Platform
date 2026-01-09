import { Component, inject, OnInit, AfterViewInit, ViewChild, ElementRef, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { ShopService } from '../../../../core/services/shop.Service';
import { MerchantStatisticService } from '../../../../core/services/merchant-statistic.service';
import { StatisticResponse } from '../../../../core/models/StatisticResponse';
import { Chart, registerables } from 'chart.js';

// Đăng ký các thành phần của Chart.js
Chart.register(...registerables);

@Component({
  selector: 'app-merchant-dashboard',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './merchant-dashboard.html',
  styleUrls: ['./merchant-dashboard.css']
})
export class MerchantDashboardComponent implements OnInit, OnDestroy {
  private shopService = inject(ShopService);
  private statisticService = inject(MerchantStatisticService);

  shop: any = null;
  currentDate = new Date();

  // --- CHART VARIABLES ---
  @ViewChild('revenueChart') revenueChartRef!: ElementRef;
  chart: any;
  selectedType: 'WEEK' | 'MONTH' | 'YEAR' = 'MONTH'; // Mặc định xem theo Tháng
  isLoadingChart = false;

  // Mock Data: Các thẻ thống kê (Giữ nguyên hoặc cần API riêng để lấy tổng)
  stats = {
    revenue: 0, // Sẽ update từ API Chart
    revenueGrowth: 12.5,
    orders: 340,
    ordersGrowth: 5.2,
    products: 52,
    newProducts: 0,
    rating: 4.8,
    ratingCount: 120
  };

  // Mock Data: Đơn hàng gần đây (Giữ nguyên)
  recentOrders = [
    { id: '#ORD-00345', product: 'Wireless Headphones', customer: 'Sarah Smith', avatar: 'https://i.pravatar.cc/150?img=5', date: 'Oct 12, 2023', amount: 129.00, status: 'COMPLETED' },
    { id: '#ORD-00344', product: 'Smart Watch Series 5', customer: 'Michael Chen', avatar: 'https://i.pravatar.cc/150?img=3', date: 'Oct 11, 2023', amount: 249.50, status: 'PENDING' },
    { id: '#ORD-00343', product: 'Gaming Mouse', customer: 'Tom Hardy', avatar: 'https://i.pravatar.cc/150?img=12', date: 'Oct 10, 2023', amount: 59.99, status: 'CANCELLED' },
  ];

  ngOnInit() {
    this.getShopInfo();
    // Gọi API load chart sau khi view init xong (thực tế gọi ở đây cũng được nhưng vẽ chart cần canvas tồn tại)
    setTimeout(() => {
      this.loadRevenueStatistics(this.selectedType);
    }, 100);
  }

  ngOnDestroy() {
    if (this.chart) {
      this.chart.destroy();
    }
  }

  getShopInfo() {
    this.shopService.getCurrentShop().subscribe({
      next: (res) => this.shop = res,
      error: (err) => console.error(err)
    });
  }

  // --- LOGIC CHART ---
  loadRevenueStatistics(type: 'WEEK' | 'MONTH' | 'YEAR') {
    this.selectedType = type;
    this.isLoadingChart = true;

    // Hủy chart cũ nếu có để vẽ lại
    if (this.chart) {
      this.chart.destroy();
    }

    this.statisticService.getRevenueStats(type).subscribe({
      next: (data) => {
        this.isLoadingChart = false;
        this.renderChart(data);
        
        // Tính tổng doanh thu từ biểu đồ để hiển thị lên thẻ "Total Revenue"
        this.stats.revenue = data.reduce((sum, item) => sum + item.value, 0);
      },
      error: (err) => {
        console.error('Lỗi tải thống kê', err);
        this.isLoadingChart = false;
      }
    });
  }

  renderChart(data: StatisticResponse[]) {
    const labels = data.map(d => d.label);
    const values = data.map(d => d.value);

    const ctx = this.revenueChartRef.nativeElement.getContext('2d');

    // Tạo Gradient màu cho đẹp
    const gradient = ctx.createLinearGradient(0, 0, 0, 400);
    gradient.addColorStop(0, 'rgba(13, 110, 253, 0.2)'); // Màu primary nhạt
    gradient.addColorStop(1, 'rgba(13, 110, 253, 0)');

    this.chart = new Chart(ctx, {
      type: 'line', // Hoặc 'bar'
      data: {
        labels: labels,
        datasets: [{
          label: 'Doanh thu (VNĐ)',
          data: values,
          borderColor: '#0d6efd', // Bootstrap primary color
          backgroundColor: gradient,
          borderWidth: 2,
          pointBackgroundColor: '#fff',
          pointBorderColor: '#0d6efd',
          pointRadius: 4,
          pointHoverRadius: 6,
          fill: true,
          tension: 0.4 // Độ cong của đường
        }]
      },
      options: {
        responsive: true,
        maintainAspectRatio: false,
        plugins: {
          legend: { display: false },
          tooltip: {
            callbacks: {
              label: (context) => {
                let label = context.dataset.label || '';
                if (label) label += ': ';
                if (context.parsed.y !== null) {
                  label += new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(context.parsed.y);
                }
                return label;
              }
            }
          }
        },
        scales: {
          y: {
            beginAtZero: true,
            grid: { color: '#f0f0f0' },
            ticks: {
              callback: (value) => {
                 // Rút gọn số hiển thị trục Y (vd: 1tr, 2tr)
                 if (typeof value === 'number' && value >= 1000000) return (value / 1000000) + 'M';
                 if (typeof value === 'number' && value >= 1000) return (value / 1000) + 'K';
                 return value;
              }
            }
          },
          x: {
            grid: { display: false }
          }
        }
      }
    });
  }

  getStatusClass(status: string): string {
    switch (status) {
      case 'COMPLETED': case 'DELIVERED': return 'bg-success bg-opacity-10 text-success';
      case 'PENDING': case 'PROCESSING': return 'bg-warning bg-opacity-10 text-warning';
      case 'CANCELLED': case 'RETURNED': return 'bg-danger bg-opacity-10 text-danger';
      case 'SHIPPING': return 'bg-primary bg-opacity-10 text-primary';
      default: return 'bg-secondary bg-opacity-10 text-secondary';
    }
  }
}