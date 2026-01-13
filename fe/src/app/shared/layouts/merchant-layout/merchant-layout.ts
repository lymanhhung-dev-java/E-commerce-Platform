import { Component, inject, OnInit, PLATFORM_ID} from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Router } from '@angular/router';
import { isPlatformBrowser } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { ToastrService } from 'ngx-toastr';
import { ShopService } from '../../../core/services/shop.Service'; 

interface MenuItem {
  label: string;
  icon: string;
  route: string;
  badge?: number; 
}

@Component({
  selector: 'app-merchant-layout',
  standalone: true,
  imports: [CommonModule, RouterModule, ReactiveFormsModule],
  templateUrl: './merchant-layout.html',
  styleUrls: ['./merchant-layout.css']
})
export class MerchantLayoutComponent implements OnInit {

  private platformId = inject(PLATFORM_ID);
  private shopService = inject(ShopService);
  private toastr = inject(ToastrService);
  private fb = inject(FormBuilder);
  private router = inject(Router);

 
  shop: any = null;
  isLoading = true;
  status: 'PENDING' | 'ACTIVE' | 'REJECTED' | 'BANNED' | 'UNKNOWN' = 'UNKNOWN';
  sidebarOpen = true;


  isEditing = false;
  resubmitForm: FormGroup;

  
  mainMenuItems: MenuItem[] = [
    { label: 'Trang chủ', icon: 'bi-grid-fill', route: '/merchant/dashboard' },
    { label: 'Danh sách đơn hàng', icon: 'bi-bag', route: '/merchant/orders', }, 
    { label: 'Quản lý sản phẩm', icon: 'bi-box-seam', route: '/merchant/products' },
    { label: 'Doanh thu', icon: 'bi-people', route: '/merchant/wallets' },
  ];

  settingsMenuItems: MenuItem[] = [
    { label: 'Cài đặt chung', icon: 'bi-gear', route: '/merchant/settings' },
    { label: 'Hỗ trợ', icon: 'bi-headset', route: '/merchant/support' }
  ];

  constructor() {
    this.resubmitForm = this.fb.group({
      shopName: ['', Validators.required],
      address: ['', Validators.required],
      description: [''],
      logoUrl: ['']
    });
  }

  ngOnInit() {
    if (isPlatformBrowser(this.platformId)) {
      this.fetchShopInfo();
    }
  }

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
        this.isLoading = false;
      }
    });
  }

  toggleSidebar() {
    this.sidebarOpen = !this.sidebarOpen;
  }

  logout() {
    localStorage.removeItem('token');
; 
    
    this.toastr.info('Đã đăng xuất thành công');
    this.router.navigate(['/auth/login']);
  }

  startEdit() {
    this.isEditing = true;
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
        this.fetchShopInfo(); 
      },
      error: (err) => {
        this.toastr.error(err.error?.message || 'Lỗi khi gửi lại yêu cầu');
      }
    });
  }
}