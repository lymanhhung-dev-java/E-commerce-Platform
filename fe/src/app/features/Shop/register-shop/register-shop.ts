import { Component, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { ToastrService } from 'ngx-toastr';
import { ShopService } from '../../../core/services/shop.Service';
import { LocationService, Province, Ward } from '../../../core/services/location.service';


@Component({
  selector: 'app-register-shop',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './register-shop.html',
  styleUrl: './register-shop.css'
})
export class RegisterShopComponent implements OnInit {
  private fb = inject(FormBuilder);
  private shopService = inject(ShopService);
  private toastr = inject(ToastrService);
  private router = inject(Router);
  private locationService = inject(LocationService);

  selectedFile: File | null = null;
  previewUrl: string | null = null;
  isSubmitting = false;

  provinces: Province[] = [];
  wards: Ward[] = [];

  registerForm = this.fb.group({
    shopName: ['', [Validators.required, Validators.minLength(3)]],
    city: ['', Validators.required],
    ward: ['', Validators.required],
    street: ['', Validators.required],
    description: [''],
  });

  ngOnInit() {
    this.loadProvinces();
    this.registerForm.get('city')?.valueChanges.subscribe(cityName => {
      if (cityName) {
        this.locationService.getWardsByProvinceName(cityName).subscribe(wards => {
          this.wards = wards;
          const currentWard = this.registerForm.get('ward')?.value;
          if (currentWard && !this.wards.find(w => w.name === currentWard)) {
            this.registerForm.get('ward')?.setValue('', { emitEvent: false });
          }
        });
      } else {
        this.wards = [];
        this.registerForm.get('ward')?.setValue('', { emitEvent: false });
      }
    });
  }

  loadProvinces() {
    this.locationService.getProvinces().subscribe({
      next: (res) => this.provinces = res,
      error: () => this.toastr.error('Lỗi tải danh sách tỉnh/thành')
    });
  }

  onFileSelected(event: any) {
    const file = event.target.files[0];
    if (file) {
      if (!file.type.startsWith('image/')) {
        this.toastr.warning('Vui lòng chọn file ảnh');
        return;
      }
      
      this.selectedFile = file;
      
      // Tạo preview
      const reader = new FileReader();
      reader.onload = (e) => this.previewUrl = e.target?.result as string;
      reader.readAsDataURL(file);
    }
  }

  onSubmit() {
    if (this.registerForm.invalid) {
      this.toastr.warning('Vui lòng điền đầy đủ thông tin bắt buộc');
      return;
    }

    this.isSubmitting = true;

    // Bước 1: Kiểm tra xem có logo không -> Upload trước
    if (this.selectedFile) {
      this.shopService.uploadLogo(this.selectedFile).subscribe({
        next: (res) => {
          this.submitRegistration(res.url);
        },
        error: () => {
          this.toastr.error('Lỗi khi upload logo');
          this.isSubmitting = false;
        }
      });
    } else {
      this.submitRegistration(''); 
    }
  }

  submitRegistration(logoUrl: string) {
    const v = this.registerForm.value;
    const fullAddress = `${v.street}, ${v.ward}, ${v.city}`;
    const formData = {
      shopName: v.shopName, 
      address: fullAddress,
      description: v.description,
      logoUrl: logoUrl
    };

    this.shopService.registerShop(formData).subscribe({
      next: () => {
        this.toastr.success('Gửi yêu cầu thành công! Vui lòng chờ Admin duyệt.');
        this.router.navigate(['/']); 
      },
      error: (err) => {
        this.toastr.error(err.error?.message || 'Đăng ký thất bại');
        this.isSubmitting = false;
      }
    });
  }
}