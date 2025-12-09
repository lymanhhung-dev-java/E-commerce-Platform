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
  fb = inject(FormBuilder);
  authService = inject(AuthService);
  router = inject(Router);
  toastr = inject(ToastrService);

  registerForm = this.fb.group({
    username: ['', [Validators.required, Validators.minLength(3)]],
    email: ['', [Validators.required, Validators.email]],
    password: ['', [Validators.required, Validators.minLength(6)]],
    confirmPassword: ['', Validators.required],
    fullName: ['', Validators.required],
    phone: ['', Validators.required]
  });

  onSubmit() {
    if (this.registerForm.valid) {
      const val = this.registerForm.value;
      
      // Kiểm tra mật khẩu khớp nhau (Validation đơn giản)
      if (val.password !== val.confirmPassword) {
        this.toastr.error('Mật khẩu xác nhận không khớp!');
        return;
      }

      this.authService.register(val).subscribe({
        next: () => {
          this.toastr.success('Đăng ký thành công! Hãy đăng nhập.');
          this.router.navigate(['/login']);
        },
        error: (err) => {
          this.toastr.error(err.error?.message || 'Đăng ký thất bại');
        }
      });
    }
  }
}