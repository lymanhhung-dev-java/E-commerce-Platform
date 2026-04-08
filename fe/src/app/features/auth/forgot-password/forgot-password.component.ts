import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';
import Swal from 'sweetalert2';

@Component({
  selector: 'app-forgot-password',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterModule],
  templateUrl: './forgot-password.component.html',
  styleUrls: ['./forgot-password.component.css']
})
export class ForgotPasswordComponent {
  private fb = inject(FormBuilder);
  private authService = inject(AuthService);
  private router = inject(Router);

  emailForm: FormGroup;
  resetForm: FormGroup;

  step: 1 | 2 = 1;
  isLoading = false;
  showPassword = false;
  showConfirmPassword = false;

  constructor() {
    this.emailForm = this.fb.group({
      email: ['', [Validators.required, Validators.email]]
    });

    this.resetForm = this.fb.group({
      otp: ['', [Validators.required, Validators.minLength(4)]],
      newPassword: ['', [Validators.required, Validators.minLength(6)]],
      confirmPassword: ['', [Validators.required]]
    }, { validators: this.passwordMatchValidator });
  }

  passwordMatchValidator(g: FormGroup) {
    return g.get('newPassword')?.value === g.get('confirmPassword')?.value
      ? null : { mismatch: true };
  }

  onSendCode() {
    if (this.emailForm.invalid) return;
    this.isLoading = true;
    const email = this.emailForm.value.email;

    this.authService.sendForgotPasswordCode(email).subscribe({
      next: (res) => {
        this.isLoading = false;
        this.step = 2; // Move to step 2
      },
      error: (err) => {
        this.isLoading = false;
        Swal.fire('Lỗi', err.error?.message || 'Không thể gửi OTP. Vui lòng kiểm tra lại email.', 'error');
      }
    });
  }

  onResetPassword() {
    if (this.resetForm.invalid) return;
    this.isLoading = true;
    
    const payload = {
      email: this.emailForm.value.email,
      otp: this.resetForm.value.otp,
      newPassword: this.resetForm.value.newPassword
    };

    this.authService.resetPassword(payload).subscribe({
      next: (res) => {
        this.isLoading = false;
        Swal.fire({
          icon: 'success',
          title: 'Thành công!',
          text: 'Mật khẩu đã được cấp lại. Vui lòng đăng nhập.',
          confirmButtonText: 'Đăng nhập ngay'
        }).then(() => {
          this.router.navigate(['/login']);
        });
      },
      error: (err) => {
        this.isLoading = false;
        Swal.fire('Thất bại', err.error?.message || 'Mã OTP không hợp lệ hoặc đã hết hạn.', 'error');
      }
    });
  }
}
