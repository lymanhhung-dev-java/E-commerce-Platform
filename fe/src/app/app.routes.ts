import { Routes } from '@angular/router';
import { LoginComponent } from './features/auth/login/login.component';
import { RegisterComponent } from './features/auth/register/register';
import { ProfileComponent } from './features/user/profile/profile';
// 1. Import HomeComponent
import { HomeComponent } from './features/product/home/home'; 

export const routes: Routes = [
    // 2. SỬA DÒNG NÀY: Thay vì redirectTo 'profile', ta gán component là HomeComponent
    { path: '', component: HomeComponent },
    
    { path: 'login', component: LoginComponent },
    { path: 'register', component: RegisterComponent },
    { path: 'profile', component: ProfileComponent },

    // 3. (Khuyên dùng) Thêm dòng này để xử lý link sai (404) -> Tự động về Trang chủ
    { path: '**', redirectTo: '', pathMatch: 'full' }
];