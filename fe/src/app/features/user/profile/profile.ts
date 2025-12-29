import { Component, inject, OnInit,PLATFORM_ID } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators,FormsModule } from '@angular/forms';
import { UserService } from '../../../core/services/user.service';
import { ToastrService } from 'ngx-toastr';
import { CommonModule,isPlatformBrowser } from '@angular/common';
import { User } from '../../../core/models/user';
import { Address } from '../../../core/models/address';
import { AddressService } from '../../../core/services/address.service';
import { AuthService } from '../../../core/services/auth.service';
import { OrderService } from '../../../core/services/order.service';
import { Order } from '../../../core/models/order';


@Component({
  selector: 'app-profile',
  standalone: true,
  imports: [ReactiveFormsModule, CommonModule,FormsModule],
  templateUrl: './profile.html',
  styleUrl: './profile.css'
})
export class ProfileComponent implements OnInit {
  fb = inject(FormBuilder);
  userService = inject(UserService);
  toastr = inject(ToastrService);
  addressService = inject(AddressService);
  platformId = inject(PLATFORM_ID);
  authService = inject(AuthService);
  orderService = inject(OrderService);



  orders: Order[] = [];
  currentPage: number = 0;
  pageSize: number = 5;
  totalElements: number = 0;
  totalPages: number = 0;
  
  keyword: string = '';      
  selectedStatus: string = 'ALL';

  addresses: Address[] = [];
  showAddressForm = false;
  isEditingAddress = false;
  currentAddressId: number | null = null;

  activeTab: 'info' | 'security' | 'address' | 'orders' = 'info' ;

  addressForm = this.fb.group({
    receiverName: ['', Validators.required],
    street: ['', Validators.required],
    ward: ['', Validators.required],
    district: ['', Validators.required],
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
    }

  }

  switchTab(tab: 'info' | 'security' | 'address' | 'orders') {
    this.activeTab = tab;
    this.showAddressForm = false; 
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
    
    const { newPassword, confirmPassword } = this.passwordForm.value;
    if (newPassword !== confirmPassword) {
      this.toastr.error('Mật khẩu xác nhận không khớp');
      return;
    }

    this.userService.changePassword(this.passwordForm.value).subscribe({
      next: () => {
        this.toastr.success('Đổi mật khẩu thành công');
        this.passwordForm.reset();
      },
      error: (err) => this.toastr.error(err.error?.message || 'Đổi mật khẩu thất bại')
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
    this.addressForm.patchValue(addr);
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
    if (confirm('Bạn có chắc muốn xóa địa chỉ này?')) {
      this.addressService.deleteAddress(id).subscribe({
        next: () => {
          this.toastr.success('Đã xóa địa chỉ');
          this.loadAddresses();
        },
        error: () => this.toastr.error('Lỗi khi xóa')
      });
    }
  }

}