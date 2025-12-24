import { Component, inject, OnInit } from '@angular/core';
import { FormBuilder, ReactiveFormsModule } from '@angular/forms';
import { UserService } from '../../../core/services/user.service';
import { ToastrService } from 'ngx-toastr';
import { CommonModule } from '@angular/common';
import { User } from '../../../core/models/user';

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

  user: User | null = null;
  selectedFile: File | null = null;
  previewUrl: string | null = null;

  profileForm = this.fb.group({
    fullName: [''],
    phoneNumber: [''],
    email: [{value: '', disabled: true}], 
    username: [{value: '', disabled: true}]
  });

  ngOnInit() {
    this.loadProfile();
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
}