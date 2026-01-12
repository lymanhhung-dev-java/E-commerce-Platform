import { Component, EventEmitter, Input, Output, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { StarRatingComponent } from '../../../shared/components/star-rating/star-rating.component';
import { OrderItem, ReviewRequest } from '../../../core/models/order';
import { OrderService } from '../../../core/services/order.service';
import { ToastrService } from 'ngx-toastr';

@Component({
  selector: 'app-review-modal',
  standalone: true,
  imports: [CommonModule, FormsModule, StarRatingComponent],
  template: `
    <div class="modal-backdrop fade show" *ngIf="isOpen"></div>
    <div class="modal fade show d-block" *ngIf="isOpen" tabindex="-1" role="dialog">
      <div class="modal-dialog modal-dialog-centered" role="document">
        <div class="modal-content rounded-4 border-0 shadow">
          <div class="modal-header border-bottom-0 pb-0">
            <h5 class="modal-title fw-bold">Đánh giá sản phẩm</h5>
            <button type="button" class="btn-close" (click)="close()"></button>
          </div>
          
          <div class="modal-body pt-2" *ngIf="item">
            <div class="d-flex align-items-center gap-3 mb-4">
              <img [src]="item.productImageUrl || 'https://via.placeholder.com/60'" 
                   class="rounded-3 border object-fit-contain" 
                   style="width: 60px; height: 60px;">
              <div>
                <h6 class="mb-1 text-dark line-clamp-2">{{ item.productName }}</h6>
                <span class="badge bg-light text-secondary border">Mã SP: #{{ item.productId }}</span>
              </div>
            </div>

            <div class="text-center mb-4">
              <label class="d-block text-muted mb-2 small">Bạn cảm thấy sản phẩm thế nào?</label>
              <app-star-rating 
                [(rating)]="rating" 
                [size]="32"
                class="justify-content-center">
              </app-star-rating>
              <div class="text-warning small mt-1 fw-bold" style="height: 20px;">
                {{ getRatingLabel() }}
              </div>
            </div>

            <div class="mb-3">
              <textarea 
                class="form-control bg-light border-0" 
                rows="4" 
                [(ngModel)]="comment"
                placeholder="Hãy chia sẻ cảm nhận của bạn về sản phẩm này nhé (tối thiểu 10 ký tự)..."
                [class.is-invalid]="isSubmitted && comment.length < 10"></textarea>
              <div class="invalid-feedback">
                Nội dung đánh giá cần ít nhất 10 ký tự.
              </div>
              <div class="text-end text-muted small mt-1">
                {{ comment.length }}/500
              </div>
            </div>
          </div>

          <div class="modal-footer border-top-0 pt-0">
            <button type="button" class="btn btn-light rounded-pill px-4" (click)="close()">Hủy</button>
            <button type="button" 
                    class="btn btn-primary rounded-pill px-4 fw-bold" 
                    [disabled]="isLoading || rating === 0"
                    (click)="submitReview()">
              <span *ngIf="isLoading" class="spinner-border spinner-border-sm me-2" role="status" aria-hidden="true"></span>
              Gửi đánh giá
            </button>
          </div>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .line-clamp-2 {
      display: -webkit-box;
      -webkit-line-clamp: 2;
      -webkit-box-orient: vertical;
      overflow: hidden;
    }
  `]
})
export class ReviewModalComponent {
  @Input() isOpen = false;
  @Input() item: OrderItem | null = null;
  @Output() closeEvent = new EventEmitter<void>();
  @Output() successEvent = new EventEmitter<void>();

  private orderService = inject(OrderService);
  private toastr = inject(ToastrService);

  rating = 0;
  comment = '';
  isLoading = false;
  isSubmitted = false;

  getRatingLabel(): string {
    switch (this.rating) {
      case 1: return 'Tệ';
      case 2: return 'Không hài lòng';
      case 3: return 'Bình thường';
      case 4: return 'Hài lòng';
      case 5: return 'Tuyệt vời';
      default: return '';
    }
  }

  close() {
    this.isOpen = false;
    this.resetForm();
    this.closeEvent.emit();
  }

  resetForm() {
    this.rating = 0;
    this.comment = '';
    this.isSubmitted = false;
  }

  submitReview() {
    this.isSubmitted = true;

    if (this.rating === 0) {
      this.toastr.warning('Vui lòng chọn số sao đánh giá');
      return;
    }

    if (this.comment.length < 10) {
      return;
    }

    if (!this.item) return;

    console.log('Submitting review for item:', this.item); // Debug logging

    if (!this.item.productId) {
      this.toastr.error('Lỗi: Không tìm thấy Product ID của món hàng.');
      return;
    }

    this.isLoading = true;

    // Using productId as requested
    const reviewRequest: ReviewRequest = {
      productId: this.item.productId,
      rating: this.rating,
      comment: this.comment
    };

    this.orderService.createReview(reviewRequest).subscribe({
      next: () => {
        this.isLoading = false;
        this.toastr.success('Cảm ơn bạn đã đánh giá sản phẩm!');
        this.successEvent.emit();
        this.close();
      },
      error: (err) => {
        this.isLoading = false;
        if (err.status === 400 && err.error?.message?.includes('reviewed')) {
          this.toastr.error('Bạn đã đánh giá sản phẩm này rồi');
        } else {
          this.toastr.error('Có lỗi xảy ra, vui lòng thử lại');
        }
      }
    });
  }
}
