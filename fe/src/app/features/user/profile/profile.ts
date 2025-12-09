import { Component, inject, OnInit } from '@angular/core';
import { FormBuilder, ReactiveFormsModule } from '@angular/forms';
import { UserService } from '../../../core/services/user.service';
import { ToastrService } from 'ngx-toastr';
import { CommonModule } from '@angular/common';

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

  user: any = null;
  selectedFile: File | null = null;
  previewUrl: string | null = null;

  profileForm = this.fb.group({
    fullName: [''],
    phoneNumber: [''],
    email: [{value: '', disabled: true}], // Email thường không cho sửa
    username: [{value: '', disabled: true}]
  });

  ngOnInit() {
    this.loadProfile();
  }

  loadProfile() {
    this.userService.getMyProfile().subscribe({
      next: (res) => {
        this.user = res;
        this.previewUrl = res.avatarUrl; // Hiển thị avatar hiện tại
        
        // Điền dữ liệu vào form
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

  // Khi người dùng chọn file ảnh
  onFileSelected(event: any) {
    const file = event.target.files[0];
    if (file) {
      this.selectedFile = file;
      
      // Tạo preview ảnh ngay lập tức để User xem trước
      const reader = new FileReader();
      reader.onload = (e: any) => this.previewUrl = e.target.result;
      reader.readAsDataURL(file);
    }
  }

  onSubmit() {
    // Bước 1: Nếu có chọn file mới -> Upload trước
    if (this.selectedFile) {
      this.userService.uploadAvatar(this.selectedFile).subscribe({
        next: (res) => {
          const newAvatarUrl = res.url; // URL từ Cloudinary trả về
          this.updateUserInfo(newAvatarUrl); // Gọi tiếp hàm cập nhật info
        },
        error: () => this.toastr.error('Lỗi upload ảnh!')
      });
    } else {
      // Nếu không đổi ảnh -> Cập nhật thông tin text thôi
      this.updateUserInfo(this.user.avatarUrl);
    }
  }

  // Bước 2: Cập nhật thông tin vào DB (UserServiceImpl)
  updateUserInfo(avatarUrl: string) {
    const updateData = {
      ...this.profileForm.value,
      avatarUrl: avatarUrl
    };

    this.userService.updateProfile(updateData).subscribe({
      next: (res) => {
        this.toastr.success('Cập nhật hồ sơ thành công!');
        this.user = res; // Cập nhật lại giao diện
        this.selectedFile = null; // Reset file
      },
      error: () => this.toastr.error('Lỗi cập nhật hồ sơ')
    });
  }
}