import { Routes } from '@angular/router';
import { LoginComponent } from './features/auth/login/login.component';
import { RegisterComponent } from './features/auth/register/register';
import { ProfileComponent } from './features/user/profile/profile';
import { DetailProductComponent } from './features/product/product-detail/product-detail';
import { HomeComponent } from './features/home/home'; 
import { CartComponent } from './features/cart/cart';

export const routes: Routes = [
    { path: '', component: HomeComponent },
    { path: 'login', component: LoginComponent },
    { path: 'register', component: RegisterComponent },
    { path: 'profile', component: ProfileComponent },
    { path: 'product/:id', component: DetailProductComponent },
    { path: 'cart', component: CartComponent },
    { path: '**', redirectTo: '', pathMatch: 'full' }
];