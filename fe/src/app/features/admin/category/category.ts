import { Component, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { CategoryService } from '../../../core/services/category.service';
import { ToastrService } from 'ngx-toastr';

declare var bootstrap: any;

@Component({
  selector: 'app-category-list',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './category.html',
  styleUrl: './category.css'
})
export class CategoryListComponent implements OnInit {
  categoryService = inject(CategoryService);
  fb = inject(FormBuilder);
  toastr = inject(ToastrService);

  categories: any[] = [];     
  flatCategories: any[] = []; 

  isEditMode = false;
currentCategoryId: number | null = null;

  categoryForm = this.fb.group({
    name: ['', Validators.required],
    parentId: [null],
    isActive: [true]
  });

  ngOnInit() {
    this.loadData();
  }

  loadData() {
    this.categoryService.getFlatCategories().subscribe({
      next: (res) => {
        this.categories = res;     
        this.flatCategories = res; 
      },
      error: () => this.toastr.error('Lỗi tải dữ liệu')
    });
  }
  openCreateModal() {
    this.isEditMode = false;
    this.currentCategoryId = null;
    this.categoryForm.reset({ isActive: true, parentId: null });
    this.showModal();
  }

  openEditModal(category: any) {
    this.isEditMode = true;
    this.currentCategoryId = category.id;

    // Đổ dữ liệu cũ vào Form
    this.categoryForm.patchValue({
      name: category.name.replace(/^-+ /, ''), 
      parentId: category.parentId,
      isActive: category.active 
    });

    this.showModal();
  }
 
  onSubmit() {
    if (this.categoryForm.invalid) {
      this.toastr.warning('Vui lòng nhập tên danh mục');
      return;
    }

    const categoryData = {
      name: this.categoryForm.value.name,
      parentId: this.categoryForm.value.parentId,
      isActive: this.categoryForm.value.isActive
    };

    if (this.isEditMode && this.currentCategoryId) {
      // === LOGIC UPDATE ===
      this.categoryService.updateCategory(this.currentCategoryId, categoryData).subscribe({
        next: () => {
          this.toastr.success('Cập nhật thành công!');
          this.finishAction();
        },
        error: (err) => this.toastr.error(err.error?.message || 'Lỗi cập nhật')
      });
    } else {
      // === LOGIC CREATE ===
      this.categoryService.createCategory(categoryData).subscribe({
        next: () => {
          this.toastr.success('Tạo mới thành công!');
          this.finishAction();
        },
        error: (err) => this.toastr.error('Lỗi tạo mới')
      });
    }
  }

  onDelete(id: number) {
    if (confirm('Bạn có chắc chắn muốn xóa danh mục này?')) {
      this.categoryService.deleteCategory(id).subscribe({
        next: () => {
          this.toastr.success('Xóa thành công');
          this.loadData();
        },
        error: (err) => this.toastr.error(err.error || 'Không thể xóa danh mục này (có thể do đang chứa danh mục con)')
      });
    }
  }

  showModal() {
    const modalElement = document.getElementById('addCategoryModal');
    const modal = new bootstrap.Modal(modalElement);
    modal.show();
  }

  closeModal() {
    const modalElement = document.getElementById('addCategoryModal');
    const modal = bootstrap.Modal.getInstance(modalElement);
    modal?.hide();
  }

  finishAction() {
    this.loadData();
    this.closeModal();
    this.categoryForm.reset();
  }
}
