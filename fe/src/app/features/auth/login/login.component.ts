import { Component, inject } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { AuthService } from '../../../core/services/auth.service';
import { UserService } from '../../../core/services/user.service';
import { Router, RouterLink } from '@angular/router';
import { ToastrService } from 'ngx-toastr';
import { CommonModule } from '@angular/common';
import { environment } from '../../../../environments/environment';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [ReactiveFormsModule, CommonModule, RouterLink],
  templateUrl: './login.component.html',
  styleUrl: './login.component.css'
})
export class LoginComponent {
  fb = inject(FormBuilder);
  authService = inject(AuthService);
  router = inject(Router);
  toastr = inject(ToastrService);
  userService = inject(UserService);

  showPassword: boolean = false;

  loginForm = this.fb.group({
    username: ['', Validators.required],
    password: ['', Validators.required]
  });

  onSubmit() {
    if (this.loginForm.valid) {
      this.authService.login(this.loginForm.value).subscribe({
        next: () => {

          this.userService.getMyProfile().subscribe({
            next: (user) => {
              this.toastr.success(`Chào mừng trở lại, ${user.fullName}!`);

              if (user.role === 'ADMIN' || user.role === 'ROLE_ADMIN') {
                this.router.navigate(['/admin/dashboard']);
              } else {
                this.router.navigate(['/']);
              }
            },
            error: () => {
              this.router.navigate(['/']);
            }
          });
        },
        error: (err) => {
          this.toastr.error('Sai tài khoản hoặc mật khẩu!');
          console.error(err);
        }
      });
    }
  }

  loginWithGoogle() {
    const { googleClientId, googleRedirectUri } = environment;
    const scope = 'email profile openid';
    const responseType = 'code';

    // Construct the Google OAuth URL
    const googleAuthUrl = `https://accounts.google.com/o/oauth2/v2/auth?client_id=${googleClientId}&redirect_uri=${encodeURIComponent(googleRedirectUri)}&response_type=${responseType}&scope=${encodeURIComponent(scope)}`;

    // Redirect the user
    window.location.href = googleAuthUrl;
  }
}