import { Component, OnInit, inject, PLATFORM_ID } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';
import { ToastrService } from 'ngx-toastr';
import { UserService } from '../../../core/services/user.service';
import { isPlatformBrowser } from '@angular/common';

@Component({
    selector: 'app-google-callback',
    standalone: true,
    template: '<div style="text-align:center; margin-top: 50px;">Processing Google Login...</div>',
})
export class GoogleCallbackComponent implements OnInit {
    private route = inject(ActivatedRoute);
    private router = inject(Router);
    private authService = inject(AuthService);
    private userService = inject(UserService);
    private toastr = inject(ToastrService);
    private platformId = inject(PLATFORM_ID);

    ngOnInit() {
        if (isPlatformBrowser(this.platformId)) {
            this.route.queryParams.subscribe(params => {
                const code = params['code'];
                if (code) {
                    this.authService.loginWithGoogle(code).subscribe({
                        next: (response) => {
                            this.userService.getMyProfile().subscribe({
                                next: (user) => {
                                    this.toastr.success(`Welcome back, ${user.fullName}!`);
                                    if (user.role === 'ADMIN' || user.role === 'ROLE_ADMIN') {
                                        this.router.navigate(['/admin/dashboard']);
                                    } else {
                                        this.router.navigate(['/']);
                                    }
                                },
                                error: (err) => {
                                    this.router.navigate(['/'])
                                }
                            });
                        },
                        error: (err) => {
                            this.toastr.error('Google login failed. Please try again.');
                            this.router.navigate(['/login']);
                        }
                    });
                } else {
                    this.router.navigate(['/login']);
                }
            });
        }
    }
}
