import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, FormsModule, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { ProductService } from '../../../core/services/product.service'; 
import { CategoryService } from '../../../core/services/category.service';
import { forkJoin, Observable, of } from 'rxjs';
import { switchMap } from 'rxjs/operators';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-merchant-product-form',
  standalone: true,
  imports: [
    CommonModule, 
    ReactiveFormsModule, 
    FormsModule,
    RouterModule 
  ],
  templateUrl: './merchant-update-product.html',
  styleUrls: ['./merchant-update-product.css'] 
})
export class MerchantProductFormComponent implements OnInit {
  productForm: FormGroup;
  isEditMode = false;
  productId: number | null = null;
  categories: any[] = [];
  
  mainImageFile: File | null = null;
  mainImagePreview: string = '';

  existingDetailImages: string[] = []; 
  newDetailImageFiles: File[] = [];   
  newDetailImagePreviews: string[] = []; 

  isLoading = false;

  constructor(
    private fb: FormBuilder,
    private productService: ProductService,
    private categoryService: CategoryService,
    private route: ActivatedRoute,
    private router: Router
  ) {
    this.productForm = this.fb.group({
      name: ['', Validators.required],
      price: [0, [Validators.required, Validators.min(0)]],
      stock: [0, [Validators.required, Validators.min(0)]],
      description: [''],
      categoryId: [null, Validators.required],
      isActive: [true]
    });
  }

  ngOnInit(): void {
    // 1. Load danh sách danh mục
    this.categoryService.getFlatCategories().subscribe({
      next: (res) => this.categories = res,
      error: (err) => console.error('Lỗi load category', err)
    });

    // 2. Kiểm tra URL xem có phải Edit Mode không
    this.route.params.subscribe(params => {
      if (params['id']) {
        this.productId = +params['id'];
        this.isEditMode = true;
        this.loadProductData(this.productId);
      }
    });
  }

  // Load dữ liệu cũ lên form
  loadProductData(id: number) {
    this.productService.getProductById(id).subscribe({
      next: (product) => {
        this.productForm.patchValue({
          name: product.name,
          price: product.price,
          stock: product.stockQuantity, // Mapping field từ BE
          description: product.description,
          categoryId: product.categoryId,
          isActive: true // Mặc định true hoặc lấy từ BE nếu có field này
        });

        // Xử lý ảnh chính cũ
        this.mainImagePreview = product.imageUrl;

        // Xử lý danh sách ảnh chi tiết cũ
        if (product.productImages && Array.isArray(product.productImages)) {
          this.existingDetailImages = product.productImages;
        }
      },
      error: (err) => console.error('Lỗi load sản phẩm', err)
    });
  }

  // --- XỬ LÝ ẢNH CHÍNH ---
  onMainImageSelected(event: any) {
    if (event.target.files.length > 0) {
      const file = event.target.files[0];
      this.mainImageFile = file;
      
      // Tạo preview
      const reader = new FileReader();
      reader.onload = (e: any) => this.mainImagePreview = e.target.result;
      reader.readAsDataURL(file);
    }
  }

  // --- XỬ LÝ ẢNH CHI TIẾT ---
  onDetailImagesSelected(event: any) {
    if (event.target.files.length > 0) {
      const files = Array.from(event.target.files) as File[];
      
      files.forEach(file => {
        this.newDetailImageFiles.push(file);
        
        // Tạo preview cho từng file
        const reader = new FileReader();
        reader.onload = (e: any) => this.newDetailImagePreviews.push(e.target.result);
        reader.readAsDataURL(file);
      });
    }
  }

  removeExistingDetailImage(index: number) {
    this.existingDetailImages.splice(index, 1);
  }

  removeNewDetailImage(index: number) {
    this.newDetailImageFiles.splice(index, 1);
    this.newDetailImagePreviews.splice(index, 1);
  }

  // --- SUBMIT FORM ---
  onSubmit() {
    if (this.productForm.invalid) {
      alert('Vui lòng điền đầy đủ thông tin bắt buộc');
      return;
    }

    this.isLoading = true;

    // 1. Tạo các Task upload ảnh
    const uploadTasks: Observable<any>[] = [];

    // Task upload ảnh chính (nếu có chọn mới)
    let mainImageObs: Observable<string | null> = of(null);
    if (this.mainImageFile) {
      mainImageObs = this.productService.uploadFile(this.mainImageFile);
    }

    // Task upload các ảnh chi tiết mới
    const detailImageObsList = this.newDetailImageFiles.map(file => 
      this.productService.uploadFile(file)
    );

    // 2. Thực hiện upload song song (forkJoin)
    forkJoin({
      newMainUrl: mainImageObs,
      newDetailUrls: detailImageObsList.length > 0 ? forkJoin(detailImageObsList) : of([])
    }).subscribe({
      next: (results) => {
        // 3. Chuẩn bị dữ liệu gửi BE
        const formValue = this.productForm.value;
        
        // Xác định ảnh chính: Nếu có upload mới thì dùng url mới, không thì dùng url cũ (đang lưu ở preview)
        const finalMainImage = results.newMainUrl ? results.newMainUrl : this.mainImagePreview;
        
        // Xác định ảnh chi tiết: Gộp ảnh cũ (chưa bị xóa) + ảnh mới vừa upload
        const finalDetailImages = [
          ...this.existingDetailImages, 
          ...(results.newDetailUrls as string[])
        ];

        const payload = {
          ...formValue,
          image: finalMainImage,          // Field cho ảnh chính
          detailImages: finalDetailImages // Field cho danh sách ảnh chi tiết
        };

        // Create cần map field mainImageUrl nếu BE yêu cầu khác
        if (!this.isEditMode) {
           payload['mainImageUrl'] = finalMainImage;
           payload['detailImageUrls'] = finalDetailImages; // Create dùng detailImageUrls, Update dùng detailImages (tùy BE)
        }

        // 4. Gọi API Save
        this.saveData(payload);
      },
      error: (err) => {
        console.error(err);
        this.isLoading = false;
        alert('Lỗi khi upload ảnh');
      }
    });
  }

  saveData(payload: any) {
    if (this.isEditMode && this.productId) {
      this.productService.updateProduct(this.productId, payload).subscribe({
        next: () => {
          alert('Cập nhật sản phẩm thành công!');
          this.router.navigate(['/merchant/products']);
        },
        error: (err) => {
          console.error(err);
          alert('Lỗi cập nhật sản phẩm');
          this.isLoading = false;
        }
      });
    } else {
      // Logic Create (đã có từ trước)
      this.productService.createProduct(payload).subscribe({
        next: () => {
          alert('Thêm sản phẩm thành công!');
          this.router.navigate(['/merchant/products']);
        },
        error: (err) => {
          console.error(err);
          alert('Lỗi thêm sản phẩm');
          this.isLoading = false;
        }
      });
    }
  }
}