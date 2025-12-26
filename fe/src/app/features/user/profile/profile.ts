import { Component, inject, OnInit,PLATFORM_ID } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { UserService } from '../../../core/services/user.service';
import { ToastrService } from 'ngx-toastr';
import { CommonModule,isPlatformBrowser } from '@angular/common';
import { User } from '../../../core/models/user';
import { Address } from '../../../core/models/address';
import { AddressService } from '../../../core/services/address.service';


@Component({
  selector: 'app-profile',
  standalone: true,
  imports: [ReactiveFormsModule, CommonModule],
  templateUrl: './profile.html',
  styleUrl: './profile.css'
})
export class ProfileComponent implements OnInit {
  fb = inject(FormBuilder);
  userService = inject(UserService);
  toastr = inject(ToastrService);
  addressService = inject(AddressService);
  platformId = inject(PLATFORM_ID);
  addresses: Address[] = [];
  showAddressForm = false;
  isEditingAddress = false;
  currentAddressId: number | null = null;

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

  ngOnInit() {
    if (isPlatformBrowser(this.platformId)) {
      this.loadProfile();
      this.loadAddresses();
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
    if (this.addressForm.invalid) return;

    const val = this.addressForm.value as Address;

    if (this.isEditingAddress && this.currentAddressId) {
      this.addressService.updateAddress(this.currentAddressId, val).subscribe({
        next: () => {
          this.toastr.success('Cập nhật địa chỉ thành công');
          this.loadAddresses();
          this.showAddressForm = false;
        },
        error: () => this.toastr.error('Lỗi cập nhật')
      });
    } else {
      this.addressService.createAddress(val).subscribe({
        next: () => {
          this.toastr.success('Thêm địa chỉ mới thành công');
          this.loadAddresses();
          this.showAddressForm = false;
        },
        error: () => this.toastr.error('Lỗi thêm mới')
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