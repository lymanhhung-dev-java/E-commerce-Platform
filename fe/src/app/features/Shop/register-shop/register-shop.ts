import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { ToastrService } from 'ngx-toastr';
import { ShopService } from '../../../core/services/shop.Service';


@Component({
  selector: 'app-register-shop',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './register-shop.html',
  styleUrl: './register-shop.css'
})
export class RegisterShopComponent {
  private fb = inject(FormBuilder);
  private shopService = inject(ShopService);
  private toastr = inject(ToastrService);
  private router = inject(Router);

  selectedFile: File | null = null;
  previewUrl: string | null = null;
  isSubmitting = false;

  registerForm = this.fb.group({
    shopName: ['', [Validators.required, Validators.minLength(3)]],
    address: ['', Validators.required],
    description: [''],
  });

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
    const formData = {
      shopName: this.registerForm.value.shopName, 
      address: this.registerForm.value.address,
      description: this.registerForm.value.description,
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