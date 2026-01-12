import { Component, EventEmitter, Input, Output } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
    selector: 'app-star-rating',
    standalone: true,
    imports: [CommonModule],
    template: `
    <div class="d-inline-flex align-items-center gap-1" [style.cursor]="readonly ? 'default' : 'pointer'">
      <i *ngFor="let star of stars; let i = index"
         class="bi"
         [ngClass]="{
           'bi-star-fill text-warning': i < currentRating,
           'bi-star text-muted': i >= currentRating
         }"
         [style.font-size.px]="size"
         (mouseenter)="onHover(i + 1)"
         (mouseleave)="onLeave()"
         (click)="rate(i + 1)">
      </i>
    </div>
  `
})
export class StarRatingComponent {
    @Input() rating: number = 0;
    @Input() readonly: boolean = false;
    @Input() size: number = 16;
    @Output() ratingChange = new EventEmitter<number>();

    hoverRating: number = 0;

    get currentRating(): number {
        return this.hoverRating || this.rating;
    }

    get stars(): number[] {
        return Array(5).fill(0);
    }

    onHover(rating: number) {
        if (!this.readonly) {
            this.hoverRating = rating;
        }
    }

    onLeave() {
        if (!this.readonly) {
            this.hoverRating = 0;
        }
    }

    rate(rating: number) {
        if (!this.readonly) {
            this.rating = rating;
            this.ratingChange.emit(this.rating);
        }
    }
}
