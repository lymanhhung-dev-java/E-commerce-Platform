import { Component, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Router } from '@angular/router';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { ToastrService } from 'ngx-toastr';
import { ShopService } from '../../../core/services/shop.Service'; // Đảm bảo đúng đường dẫn file service

// Interface định nghĩa cấu trúc 1 mục Menu
interface MenuItem {
  label: string;
  icon: string;
  route: string;
  badge?: number; // Số thông báo (ví dụ: Orders: 12)
}

@Component({
  selector: 'app-merchant-layout',
  standalone: true,
  imports: [CommonModule, RouterModule, ReactiveFormsModule],
  templateUrl: './merchant-layout.html',
  styleUrls: ['./merchant-layout.css']
})
export class MerchantLayoutComponent implements OnInit {
  // Inject Services
  private shopService = inject(ShopService);
  private toastr = inject(ToastrService);
  private fb = inject(FormBuilder);
  private router = inject(Router);

  // --- STATE VARIABLES ---
  shop: any = null;
  isLoading = true;
  status: 'PENDING' | 'ACTIVE' | 'REJECTED' | 'BANNED' | 'UNKNOWN' = 'UNKNOWN';
  sidebarOpen = true;

  // Variables cho Form Resubmit (Trường hợp Shop bị từ chối)
  isEditing = false;
  resubmitForm: FormGroup;

  // --- MENU CONFIGURATION ---
  
  // 1. Menu chính
  mainMenuItems: MenuItem[] = [
    { label: 'Dashboard', icon: 'bi-grid-fill', route: '/merchant/dashboard' },
    { label: 'Orders', icon: 'bi-bag', route: '/merchant/orders', badge: 12 }, // Demo badge
    { label: 'Products', icon: 'bi-box-seam', route: '/merchant/products' },
    { label: 'Customers', icon: 'bi-people', route: '/merchant/customers' },
    { label: 'Reports', icon: 'bi-bar-chart', route: '/merchant/reports' }
  ];

  // 2. Menu cài đặt (Phía dưới)
  settingsMenuItems: MenuItem[] = [
    { label: 'General', icon: 'bi-gear', route: '/merchant/settings' },
    { label: 'Support', icon: 'bi-headset', route: '/merchant/support' }
  ];

  constructor() {
    // Khởi tạo Form Resubmit
    this.resubmitForm = this.fb.group({
      shopName: ['', Validators.required],
      address: ['', Validators.required],
      description: [''],
      logoUrl: ['']
    });
  }

  ngOnInit() {
    this.fetchShopInfo();
  }

  // --- 1. LOGIC LẤY THÔNG TIN SHOP ---
  fetchShopInfo() {
    this.isLoading = true;
    this.shopService.getCurrentShop().subscribe({
      next: (res) => {
        this.shop = res;
        this.status = res.status;
        this.isLoading = false;
      },
      error: (err) => {
        console.error('Không tải được thông tin Shop:', err);
        // Nếu lỗi 403/401 hoặc chưa có shop, layout sẽ xử lý hiển thị tương ứng
        this.isLoading = false;
      }
    });
  }

  // --- 2. LOGIC LAYOUT (SIDEBAR & HEADER) ---
  toggleSidebar() {
    this.sidebarOpen = !this.sidebarOpen;
  }

  logout() {
    // Xóa token và điều hướng về trang Login
    localStorage.removeItem('token');
    // Xóa thêm các thông tin user khác nếu có
    // localStorage.removeItem('user'); 
    
    this.toastr.info('Đã đăng xuất thành công');
    this.router.navigate(['/auth/login']);
  }

  // --- 3. LOGIC XỬ LÝ KHI SHOP BỊ TỪ CHỐI (REJECTED) ---
  startEdit() {
    this.isEditing = true;
    // Đổ dữ liệu hiện tại vào form để người dùng sửa
    if (this.shop) {
      this.resubmitForm.patchValue({
        shopName: this.shop.shopName,
        address: this.shop.address,
        description: this.shop.description,
        logoUrl: this.shop.logoUrl
      });
    }
  }

  onResubmit() {
    if (this.resubmitForm.invalid) {
      this.toastr.warning('Vui lòng điền đầy đủ thông tin');
      return;
    }

    this.shopService.resubmitShop(this.resubmitForm.value).subscribe({
      next: () => {
        this.toastr.success('Đã gửi lại hồ sơ thành công!');
        this.isEditing = false;
        this.fetchShopInfo(); // Tải lại để cập nhật trạng thái mới (thường là PENDING)
      },
      error: (err) => {
        this.toastr.error(err.error?.message || 'Lỗi khi gửi lại yêu cầu');
      }
    });
  }
}