import { Component, inject } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { AuthService } from '../../../core/services/auth.service';
import { Router, RouterLink } from '@angular/router';
import { ToastrService } from 'ngx-toastr';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-register',
  standalone: true,
  imports: [ReactiveFormsModule, CommonModule, RouterLink],
  templateUrl: './register.html', 
  styleUrl: './register.css'     
})
export class RegisterComponent {
  private fb = inject(FormBuilder);
  private authService = inject(AuthService);
  private router = inject(Router);
  private toastr = inject(ToastrService);

  showPassword = false;
  showConfirmPassword = false;
  
  step = 1;
  registeredEmail = '';

  registerForm = this.fb.group({
    username: ['', [Validators.required, Validators.minLength(3)]],
    email: ['', [Validators.required, Validators.email]],
    password: ['', [Validators.required, Validators.minLength(6)]],
    confirmPassword: ['', Validators.required],
    fullName: ['', Validators.required],
    phone: ['', Validators.required]
  });

  otpForm = this.fb.group({
    otp: ['', [Validators.required, Validators.minLength(4), Validators.maxLength(4)]]
  });

  onSubmit() {
    if (this.registerForm.valid) {
      const val = this.registerForm.value;
      
      if (val.password !== val.confirmPassword) {
        this.toastr.error('Mật khẩu xác nhận không khớp!');
        return;
      }

      this.authService.register(val).subscribe({
        next: (res) => {
          this.toastr.success('Mã xác thực đã được gửi đến email của bạn.');
          this.registeredEmail = val.email as string;
          this.step = 2; // Move to OTP step
        },
        error: (err) => {
          let errorMessage = 'Đăng ký thất bại. Vui lòng thử lại.';
          try {
            // When responseType is 'text', standard JSON error responses come as strings.
            const parsedError = typeof err.error === 'string' ? JSON.parse(err.error) : err.error;
            errorMessage = parsedError?.message || errorMessage;
          } catch (e) {}
          this.toastr.error(errorMessage);
        }
      });
    } else {
      this.toastr.warning('Vui lòng điền đầy đủ thông tin hợp lệ.');
      this.registerForm.markAllAsTouched();
    }
  }

  onVerifyOtp() {
    if (this.otpForm.valid) {
      const otp = this.otpForm.value.otp as string;
      this.authService.verifyRegister(this.registeredEmail, otp).subscribe({
        next: (res) => {
          this.toastr.success('Xác thực thành công! Hãy đăng nhập.');
          this.router.navigate(['/login']);
        },
        error: (err) => {
          const errorMessage = err.error?.message || 'Xác thực thất bại. Vui lòng thử lại.';
          this.toastr.error(errorMessage);
        }
      });
    } else {
      this.toastr.warning('Vui lòng nhập mã OTP gồm 4 ký tự.');
      this.otpForm.markAllAsTouched();
    }
  }
}