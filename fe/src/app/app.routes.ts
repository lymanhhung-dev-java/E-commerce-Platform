import { Routes } from '@angular/router';
import { LoginComponent } from './features/auth/login/login.component';
import { RegisterComponent } from './features/auth/register/register';
import { ProfileComponent } from './features/user/profile/profile';
import { DetailProductComponent } from './features/product/product-detail/product-detail';
import { HomeComponent } from './features/home/home'; 
import { CartComponent } from './features/cart/cart';
import { CheckoutComponent } from './features/checkout/checkout';
import { AdminLayoutComponent } from './shared/layouts/admin-layout/admin-layout';
import { MainLayoutComponent } from './shared/layouts/main-layoyt/main-layout';
import { UserListComponent } from './features/admin/user/user-list';
import { CategoryListComponent } from './features/admin/category/category';



export const routes: Routes = [
    {
        path: '',
        component: MainLayoutComponent,
        children: [
            { path: '', component: HomeComponent },
            { path: 'login', component: LoginComponent },
            { path: 'register', component: RegisterComponent },
            { path: 'profile', component: ProfileComponent },
            { path: 'product/:id', component: DetailProductComponent },
            { path: 'cart', component: CartComponent },
            { path: 'checkout', component: CheckoutComponent },
        ]
    },

    
    {
        path: 'admin',
        component: AdminLayoutComponent,
        children: [
            { path: 'users', component: UserListComponent },
            { path: 'categories', component: CategoryListComponent },
        ]
    },

    { path: '**', redirectTo: '', pathMatch: 'full' }
];