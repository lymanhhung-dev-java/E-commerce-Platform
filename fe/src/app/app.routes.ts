import { Routes } from '@angular/router';
import { LoginComponent } from './features/auth/login/login.component';
import { GoogleCallbackComponent } from './features/auth/google-callback/google-callback.component';
import { RegisterComponent } from './features/auth/register/register';
import { ProfileComponent } from './features/user/profile/profile';
import { OrderDetailComponent } from './features/user/order-detail/order-detail.component';
import { DetailProductComponent } from './features/product/product-detail/product-detail';
import { HomeComponent } from './features/home/home';
import { CartComponent } from './features/cart/cart';
import { CheckoutComponent } from './features/checkout/checkout';
import { AdminLayoutComponent } from './shared/layouts/admin-layout/admin-layout';
import { MainLayoutComponent } from './shared/layouts/main-layoyt/main-layout';
import { MerchantLayoutComponent } from './shared/layouts/merchant-layout/merchant-layout';
import { RegisterShopComponent } from './features/Shop/register-shop/register-shop';
import { MerchantDashboardComponent } from './features/Shop/merchant/merchant-dashboard/merchant-dashboard';
import { MerchantProductListComponent } from './features/Shop/merchant-product/merchant-product';
import { MerchantProductCreateComponent } from './features/Shop/merchant-product-create/merchant-product-create';
import { MerchantProductFormComponent } from './features/Shop/merchant-update-product/merchant-update-product';
import { MerchantOrderListComponent } from './features/Shop/merchant-oder-list/merchant-order-list';
import { MerchantWalletComponent } from './features/Shop/MerchantWallet/merchant-wallet';
import { AdminDashboardComponent } from './features/admin/admin-dashboard/admin-dashboard';
import { WithdrawalRequestListComponent } from './features/admin/withdrawal-request-list/withdrawal-request-list';
import { UserListComponent } from './features/admin/user/user-list';
import { CategoryListComponent } from './features/admin/category/category';
import { ShopRequestListComponent } from './features/admin/shop-request-list/shop-request-list';
import { ShopManagementComponent } from './features/admin/shop-management/shop-management';
import { ProductManagementComponent } from './features/admin/product-management/product-management';





export const routes: Routes = [
    {
        path: '',
        component: MainLayoutComponent,
        children: [
            { path: '', component: HomeComponent },
            { path: 'login', component: LoginComponent },
            { path: 'auth/google/callback', component: GoogleCallbackComponent },
            { path: 'register', component: RegisterComponent },
            { path: 'profile', component: ProfileComponent },
            { path: 'profile/order/:id', component: OrderDetailComponent },
            { path: 'product/:id', component: DetailProductComponent },
            { path: 'cart', component: CartComponent },
            { path: 'checkout', component: CheckoutComponent },
            { path: 'register-shop', component: RegisterShopComponent },
            { path: 'shop/:id', loadComponent: () => import('./features/shop-detail/shop-detail').then(m => m.ShopDetailComponent) },


        ]
    },

    {
        path: 'merchant',
        component: MerchantLayoutComponent,
        children: [
            { path: 'dashboard', component: MerchantDashboardComponent },
            { path: 'products', component: MerchantProductListComponent },
            { path: 'products/create', component: MerchantProductCreateComponent },
            { path: 'products/edit/:id', component: MerchantProductFormComponent },
            { path: 'orders', component: MerchantOrderListComponent },
            { path: 'wallets', component: MerchantWalletComponent },


        ]
    },

    {
        path: 'admin',
        component: AdminLayoutComponent,
        children: [
            { path: 'dashboard', component: AdminDashboardComponent },
            { path: 'users', component: UserListComponent },
            { path: 'categories', component: CategoryListComponent },
            { path: 'shopsApprovals', component: ShopRequestListComponent },
            { path: 'shops', component: ShopManagementComponent },
            { path: 'wallets', component: WithdrawalRequestListComponent },
            { path: 'products', component: ProductManagementComponent },

        ]
    },



    { path: '**', redirectTo: '', pathMatch: 'full' }
];