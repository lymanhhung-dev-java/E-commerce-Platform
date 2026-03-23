import { Component, inject, OnInit, PLATFORM_ID } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators, FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { UserService } from '../../../core/services/user.service';
import { ToastrService } from 'ngx-toastr';
import { CommonModule, isPlatformBrowser } from '@angular/common';
import { User } from '../../../core/models/user';
import { Address } from '../../../core/models/address';
import { AddressService } from '../../../core/services/address.service';
import { LocationService, Province, Ward } from '../../../core/services/location.service';
import { AuthService } from '../../../core/services/auth.service';
import { OrderService } from '../../../core/services/order.service';
import { Order } from '../../../core/models/order';
import { CartService } from '../../../core/services/cart.service';
import { VoucherService } from '../../../core/services/voucher.service';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../../environments/environment';
import { forkJoin } from 'rxjs';
import Swal from 'sweetalert2';


@Component({
  selector: 'app-profile',
  standalone: true,
  imports: [ReactiveFormsModule, CommonModule, FormsModule],
  templateUrl: './profile.html',
  styleUrl: './profile.css'
})
export class ProfileComponent implements OnInit {
  fb = inject(FormBuilder);
  userService = inject(UserService);
  toastr = inject(ToastrService);
  addressService = inject(AddressService);
  locationService = inject(LocationService);
  platformId = inject(PLATFORM_ID);
  authService = inject(AuthService);
  router = inject(Router);
  orderService = inject(OrderService);
  cartService = inject(CartService);
  voucherService = inject(VoucherService);
  private http = inject(HttpClient);
  private cartApiUrl = `${environment.apiUrl}/cart`;



  orders: Order[] = [];
  currentPage: number = 0;
  pageSize: number = 5;
  totalElements: number = 0;
  totalPages: number = 0;

  keyword: string = '';
  selectedStatus: string = 'ALL';
  cancelingOrderId: number | null = null;

  addresses: Address[] = [];
  showAddressForm = false;
  isEditingAddress = false;
  currentAddressId: number | null = null;
  provinces: Province[] = [];
  wards: Ward[] = [];

  activeTab: 'info' | 'security' | 'address' | 'orders' | 'vouchers' = 'info';

  savedVouchers: any[] = [];
  voucherCodeInput: string = '';
  voucherPage: number = 0;
  voucherTotalPages: number = 0;

  addressForm = this.fb.group({
    receiverName: ['', Validators.required],
    street: ['', Validators.required],
    ward: ['', Validators.required],
    city: ['', Validators.required],
    phoneNumber: ['', [Validators.required, Validators.pattern('^[0-9]{10}$')]],
    isDefault: [false]
  });

  user: User | null = null;
  selectedFile: File | null = null;
  previewUrl: string | null = null;

  profileForm = this.fb.group({
    fullName: [''],
    phoneNumber: [''],
    email: [{ value: '', disabled: true }],
    username: [{ value: '', disabled: true }]
  });

  passwordForm = this.fb.group({
    currentPassword: ['', Validators.required],
    newPassword: ['', [Validators.required, Validators.minLength(6)]],
    confirmPassword: ['', Validators.required]
  });

  ngOnInit() {
    if (isPlatformBrowser(this.platformId)) {
      this.loadProfile();
      this.loadAddresses();
      this.loadOrders();
      this.loadProvinces();

      this.addressForm.get('city')?.valueChanges.subscribe(cityName => {
        if (cityName) {
          this.locationService.getWardsByProvinceName(cityName).subscribe(wards => {
            this.wards = wards;
            const currentWard = this.addressForm.get('ward')?.value;
            if (currentWard && !this.wards.find(w => w.name === currentWard)) {
              this.addressForm.get('ward')?.setValue('', { emitEvent: false });
            }
          });
        } else {
          this.wards = [];
          this.addressForm.get('ward')?.setValue('', { emitEvent: false });
        }
      });
    }

  }

  loadProvinces() {
    this.locationService.getProvinces().subscribe({
      next: (res) => this.provinces = res,
      error: () => this.toastr.error('Lỗi tải danh sách tỉnh/thành')
    });
  }

  viewOrderDetail(orderId: number) {
    this.router.navigate(['/profile/order', orderId]);
  }

  switchTab(tab: 'info' | 'security' | 'address' | 'orders' | 'vouchers') {
    this.activeTab = tab;
    this.showAddressForm = false;
    if (tab === 'vouchers') {
      this.loadVouchers();
    }
  }

  loadVouchers() {
    this.voucherService.getMySavedVouchers(this.voucherPage, 10).subscribe({
      next: (res) => {
        if (res.content) {
          this.savedVouchers = res.content;
          this.voucherTotalPages = res.totalPages;
        } else {
          this.savedVouchers = res;
        }
      },
      error: () => this.toastr.error('Lỗi tải mã giảm giá')
    });
  }

  onSaveVoucher() {
    if (!this.voucherCodeInput.trim()) {
      this.toastr.warning('Vui lòng nhập mã giảm giá');
      return;
    }
    this.voucherService.saveVoucher(this.voucherCodeInput.trim()).subscribe({
      next: () => {
        this.toastr.success('Lưu mã giảm giá thành công');
        this.voucherCodeInput = '';
        this.voucherPage = 0;
        this.loadVouchers();
      },
      error: (err) => this.toastr.error(err.error?.message || 'Lỗi lưu mã giảm giá')
    });
  }

  onVoucherPageChange(page: number) {
    if (page >= 0 && page < this.voucherTotalPages) {
      this.voucherPage = page;
      this.loadVouchers();
    }
  }

  loadProfile() {
    this.userService.getMyProfile().subscribe({
      next: (res) => {
        this.user = res;
        this.previewUrl = res.avatarUrl;
        this.profileForm.patchValue({
          fullName: res.fullName,
          phoneNumber: res.phoneNumber,
          email: res.email,
          username: res.username
        });
      },
      error: () => this.toastr.error('Lỗi tải thông tin cá nhân')
    });
  }

  onChangePassword() {
    if (this.passwordForm.invalid) return;

    const { newPassword, confirmPassword, currentPassword } = this.passwordForm.value;
    if (newPassword !== confirmPassword) {
      this.toastr.error('Mật khẩu xác nhận không khớp');
      return;
    }

    const payload = {
      oldPassword: currentPassword,
      newPassword: newPassword,
      confirmPassword: confirmPassword
    };

    this.userService.changePassword(payload).subscribe({
      next: (res) => {
        this.toastr.success('Đổi mật khẩu thành công');
        this.passwordForm.reset();
      },
      error: (err) => {
        this.toastr.error(err.error?.message || 'Đổi mật khẩu thất bại');
      }
    });
  }

  loadAddresses() {
    this.addressService.getMyAddresses().subscribe({
      next: (res) => this.addresses = res,
      error: () => this.toastr.error('Lỗi tải danh sách địa chỉ')
    });
  }

  onFileSelected(event: any) {
    const file = event.target.files[0];
    if (file) {
      this.selectedFile = file;
      const reader = new FileReader();
      reader.onload = (e: any) => this.previewUrl = e.target.result;
      reader.readAsDataURL(file);
    }
  }

  loadOrders() {
    this.orderService.getMyOrders(this.currentPage, this.pageSize, this.keyword, this.selectedStatus)
      .subscribe({
        next: (res: any) => {
          // Kiểm tra kết quả trả về
          if (res.content) {
            // Trường hợp 1: Backend trả về Page (Đã phân trang)
            this.orders = res.content;
            this.totalPages = res.totalPages;
            this.totalElements = res.totalElements;
          } else if (Array.isArray(res)) {
            // Trường hợp 2: Backend trả về List thường (Chưa phân trang)
            this.orders = res;
            this.totalPages = 1;
          } else {
            this.orders = [];
          }
        },
        error: () => {
          this.toastr.error('Lỗi tải danh sách đơn hàng');
          this.orders = [];
        }
      });
  }

  onSearch() {
    this.currentPage = 0;
    this.loadOrders();
  }

  onFilterStatus() {
    this.currentPage = 0;
    this.loadOrders();
  }

  selectOrderStatus(status: string) {
    this.selectedStatus = status;
    this.currentPage = 0;
    this.loadOrders();
  }

  cancelOrder(orderId: number) {
    Swal.fire({
      title: 'Xác nhận hủy',
      text: 'Bạn có chắc muốn hủy đơn hàng #' + orderId + '?',
      icon: 'warning',
      showCancelButton: true,
      showCloseButton: true,
      confirmButtonColor: '#d33',
      cancelButtonColor: '#6c757d',
      confirmButtonText: 'Xác nhận hủy',
      cancelButtonText: 'Thoát'
    }).then((result) => {
      if (result.isConfirmed) {
        this.cancelingOrderId = orderId;
        this.orderService.cancelOrder(orderId).subscribe({
          next: () => {
            this.toastr.success('Đã hủy đơn hàng #' + orderId);
            this.cancelingOrderId = null;
            this.loadOrders();
          },
          error: (err) => {
            this.toastr.error(err.error?.message || 'Không thể hủy đơn hàng');
            this.cancelingOrderId = null;
          }
        });
      }
    });
  }

  reorderingId: number | null = null;

  reorder(order: Order) {
    if (this.reorderingId === order.id) return;
    this.reorderingId = order.id;

    const reorderProductIds = new Set(order.items.map(i => i.productId));

    const requests = order.items.map(item =>
      this.http.post(`${this.cartApiUrl}/add`, { productId: item.productId, quantity: item.quantity }, { responseType: 'text' })
    );

    forkJoin(requests).subscribe({
      next: () => {
        // Reload cart, rồi chỉ select các sản phẩm của đơn này
        this.http.get<any[]>(this.cartApiUrl).subscribe({
          next: (cartData) => {
            // Map sang CartItem và chỉ select items thuộc đơn hàng này
            const cartItems = cartData.map((item: any) => ({
              product: {
                id: item.productId,
                name: item.productName,
                price: item.price,
                imageUrl: item.productImageUrl,
                stockQuantity: item.stockQuantity,
                shopName: item.shopName
              },
              quantity: item.quantity,
              selected: reorderProductIds.has(item.productId)
            }));
            this.cartService.cartItems.set(cartItems);
            this.reorderingId = null;
            this.toastr.success('Đang chuyển sang thanh toán...');
            this.router.navigate(['/checkout']);
          },
          error: () => {
            this.cartService.loadCart();
            this.reorderingId = null;
            this.router.navigate(['/checkout']);
          }
        });
      },
      error: (err) => {
        this.toastr.error(err.error?.message || 'Có lỗi khi thêm vào giỏ hàng');
        this.reorderingId = null;
      }
    });
  }

  onPageChange(page: number) {
    if (page >= 0 && page < this.totalPages) {
      this.currentPage = page;
      this.loadOrders();
    }
  }



  getStatusClass(status: string): string {
    switch (status) {
      case 'DELIVERED': return 'bg-success-subtle text-success border-success-subtle';
      case 'SHIPPED': return 'bg-primary-subtle text-primary border-primary-subtle';
      case 'PENDING': return 'bg-warning-subtle text-warning border-warning-subtle';
      case 'CANCELED': return 'bg-danger-subtle text-danger border-danger-subtle';
      default: return 'bg-light text-secondary';
    }
  }

  onSubmit() {

    if (this.selectedFile) {
      this.userService.uploadAvatar(this.selectedFile).subscribe({
        next: (res) => {
          const newAvatarUrl = res.url;
          this.updateUserInfo(newAvatarUrl);
        },
        error: () => this.toastr.error('Lỗi upload ảnh!')
      });
    } else {
      this.updateUserInfo(this.user?.avatarUrl || '');
    }
  }

  updateUserInfo(avatarUrl: string) {
    const updateData = {
      ...this.profileForm.value,
      avatarUrl: avatarUrl
    };

    this.userService.updateProfile(updateData).subscribe({
      next: (res) => {
        this.toastr.success('Cập nhật hồ sơ thành công!');
        this.user = res;
        this.selectedFile = null;
      },
      error: () => this.toastr.error('Lỗi cập nhật hồ sơ')
    });
  }

  openAddAddress() {
    this.showAddressForm = true;
    this.isEditingAddress = false;
    this.addressForm.reset({ isDefault: false });
  }

  openEditAddress(addr: Address) {
    this.showAddressForm = true;
    this.isEditingAddress = true;
    this.currentAddressId = addr.id || null;

    if (addr.city) {
      this.locationService.getWardsByProvinceName(addr.city).subscribe(wards => {
        this.wards = wards;
        this.addressForm.patchValue(addr);
      });
    } else {
      this.addressForm.patchValue(addr);
    }
  }

  saveAddress() {

    if (this.addressForm.invalid) {
      this.addressForm.markAllAsTouched();
      this.toastr.warning('Vui lòng kiểm tra lại thông tin nhập!');
      return;
    }

    const val = this.addressForm.value as Address;

    if (this.isEditingAddress && this.currentAddressId) {
      this.addressService.updateAddress(this.currentAddressId, val).subscribe({
        next: () => {
          this.toastr.success('Cập nhật địa chỉ thành công');
          this.loadAddresses();
          this.showAddressForm = false;
        },
        error: (err) => this.toastr.error(err.error?.message || 'Lỗi cập nhật')
      });
    } else {
      this.addressService.createAddress(val).subscribe({
        next: () => {
          this.toastr.success('Thêm địa chỉ mới thành công');
          this.loadAddresses();
          this.showAddressForm = false;
        },
        error: (err) => this.toastr.error(err.error?.message || 'Lỗi thêm mới')
      });
    }
  }

  deleteAddress(id: number) {
    Swal.fire({
      title: 'Xác nhận xóa',
      text: 'Bạn có chắc muốn xóa địa chỉ này?',
      icon: 'warning',
      showCancelButton: true,
      showCloseButton: true,
      confirmButtonColor: '#d33',
      cancelButtonColor: '#6c757d',
      confirmButtonText: 'Xác nhận xóa',
      cancelButtonText: 'Thoát'
    }).then((result) => {
      if (result.isConfirmed) {
        this.addressService.deleteAddress(id).subscribe({
          next: () => {
            this.toastr.success('Đã xóa địa chỉ');
            this.loadAddresses();
          },
          error: () => this.toastr.error('Lỗi khi xóa')
        });
      }
    });
  }

}