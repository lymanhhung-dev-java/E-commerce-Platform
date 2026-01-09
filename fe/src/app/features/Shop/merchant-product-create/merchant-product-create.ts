import { Component, inject, ViewChild, ElementRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { ToastrService } from 'ngx-toastr';
import { ProductService } from '../../../core/services/product.service';
import { CategoryService } from '../../../core/services/category.service';


@Component({
  selector: 'app-merchant-product-create',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterModule],
  templateUrl: './merchant-product-create.html',
  styleUrls: ['./merchant-product-create.css'] 
})
export class MerchantProductCreateComponent {
  // Inject Services
  private fb = inject(FormBuilder);
  private productService = inject(ProductService);
  private categoryService = inject(CategoryService);
  private router = inject(Router);
  private toastr = inject(ToastrService);

  @ViewChild('mainInput') mainInput!: ElementRef;
  @ViewChild('detailInput') detailInput!: ElementRef;

  productForm: FormGroup;
  categories: any[] = [];
  isSubmitting = false;

  mainImageFile: File | null = null;
  mainImagePreview: string | null = null;

  detailImageFiles: File[] = [];
  detailImagesPreview: string[] = [];

  constructor() {
    this.productForm = this.fb.group({
      name: ['', Validators.required],
      price: [0, [Validators.required, Validators.min(1)]],
      stock: [10, [Validators.required, Validators.min(1)]],
      categoryId: [null, Validators.required],
      description: ['']
    });
    this.loadCategories();
  }

  loadCategories() {
    this.categoryService.getFlatCategories().subscribe({
      next: (res) => this.categories = res
    });
  }


  triggerMainInput() {
    this.mainInput.nativeElement.click();
  }

  onMainImageSelect(event: any) {
    const file = event.target.files[0];
    if (file) {
      this.mainImageFile = file;
      const reader = new FileReader();
      reader.onload = () => this.mainImagePreview = reader.result as string;
      reader.readAsDataURL(file);
    }
    event.target.value = '';
  }

  removeMainImage(event: Event) {
    event.stopPropagation();
    this.mainImageFile = null;
    this.mainImagePreview = null;
  }

  triggerDetailInput() {
    this.detailInput.nativeElement.click();
  }

  onDetailImagesSelect(event: any) {
    const files = event.target.files;
    if (files) {
      const remainingSlots = 5 - this.detailImageFiles.length;
      
      for (let i = 0; i < files.length; i++) {
        if (i >= remainingSlots) {
             this.toastr.warning('Chỉ được chọn tối đa 5 ảnh phụ');
             break;
        }
        
        const file = files[i];
        this.detailImageFiles.push(file);
        
        const reader = new FileReader();
        reader.onload = () => this.detailImagesPreview.push(reader.result as string);
        reader.readAsDataURL(file);
      }
    }
    event.target.value = '';
  }

  removeDetailImage(index: number) {
    this.detailImageFiles.splice(index, 1);
    this.detailImagesPreview.splice(index, 1);
  }

  async onSubmit() {
    if (this.productForm.invalid) {
      this.toastr.warning('Vui lòng điền đủ thông tin');
      return;
    }
    if (!this.mainImageFile) {
      this.toastr.warning('Vui lòng chọn ảnh đại diện');
      return;
    }

    this.isSubmitting = true;

    try {

      const mainUrl = await this.productService.uploadFile(this.mainImageFile).toPromise();

      const detailUploads = this.detailImageFiles.map(f => this.productService.uploadFile(f).toPromise());
      const detailUrls = (await Promise.all(detailUploads)).filter(url => url !== undefined) as string[];

      const payload = {
        ...this.productForm.value,
        mainImageUrl: mainUrl,
        detailImageUrls: detailUrls
      };

      this.productService.createProduct(payload).subscribe({
        next: () => {
          this.toastr.success('Tạo sản phẩm thành công!');
          this.router.navigate(['/merchant/products']);
        },
        error: () => {
          this.toastr.error('Lỗi khi tạo sản phẩm');
          this.isSubmitting = false;
        }
      });

    } catch (err) {
      this.toastr.error('Lỗi khi upload ảnh');
      this.isSubmitting = false;
    }
  }
}