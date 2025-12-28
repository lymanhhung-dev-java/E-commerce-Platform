import { Component, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ShopService } from '../../../../core/services/shop.Service';
import { RouterModule } from '@angular/router';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ToastrService } from 'ngx-toastr';
import { ReactiveFormsModule } from '@angular/forms';
 
@Component({
  selector: 'app-merchant-dashboard',
  standalone: true,
  imports: [CommonModule, RouterModule, ReactiveFormsModule],
  templateUrl: './merchant-dashboard.html',
  styleUrl: './merchant-dashboard.css'
})
export class MerchantDashboardComponent implements OnInit {
  shopService = inject(ShopService);
  toastr = inject(ToastrService);
  
  shop: any = null;
  isLoading = true;
  status: 'PENDING' | 'ACTIVE' | 'REJECTED' | 'UNKNOWN' = 'UNKNOWN';

  isEditing = false;
    resubmitForm: FormGroup;

  constructor(private fb: FormBuilder) {
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

  startEdit() {
  this.isEditing = true;
  this.resubmitForm.patchValue({
    shopName: this.shop.shopName,
    address: this.shop.address,
    description: this.shop.description,
    logoUrl: this.shop.logoUrl
  });
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
        console.error('Lỗi lấy thông tin shop', err);
        this.isLoading = false;
      }
    });
  }
  onResubmit() {
  if (this.resubmitForm.invalid) return;
  
  this.shopService.resubmitShop(this.resubmitForm.value).subscribe({
    next: () => {
      this.toastr.success('Đã gửi lại yêu cầu thành công!');
      this.isEditing = false;
      this.fetchShopInfo(); 
    },
    error: (err) => this.toastr.error(err.error?.message || 'Lỗi gửi lại')
  });
}

}