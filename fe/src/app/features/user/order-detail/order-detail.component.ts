import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { OrderService } from '../../../core/services/order.service';
import { Order } from '../../../core/models/order';
import { ToastrService } from 'ngx-toastr';
import { ReviewModalComponent } from '../review-modal/review-modal.component';
import { OrderItem } from '../../../core/models/order';

@Component({
    selector: 'app-order-detail',
    standalone: true,
    imports: [CommonModule, RouterModule, ReviewModalComponent],
    templateUrl: './order-detail.component.html',
    styleUrl: './order-detail.component.css'
})
export class OrderDetailComponent implements OnInit {
    private route = inject(ActivatedRoute);
    private router = inject(Router);
    private orderService = inject(OrderService);
    private toastr = inject(ToastrService);

    order: Order | null = null;
    isLoading = true;

    ngOnInit() {
        this.route.params.subscribe(params => {
            const orderId = Number(params['id']);
            if (orderId) {
                this.loadOrder(orderId);
            } else {
                this.router.navigate(['/profile']);
            }
        });
    }

    loadOrder(id: number) {
        this.isLoading = true;
        this.orderService.getOrderById(id).subscribe({
            next: (res) => {
                this.order = res;
                this.isLoading = false;
            },
            error: () => {
                this.isLoading = false;
                this.toastr.error('Không tìm thấy đơn hàng');
                this.router.navigate(['/profile']);
            }
        });
    }

    isCanceling = false;

    cancelOrder() {
        if (!this.order) return;
        if (!confirm('Bạn có chắc chắn muốn hủy đơn hàng #' + this.order.id + ' không?')) return;

        this.isCanceling = true;
        this.orderService.cancelOrder(this.order.id).subscribe({
            next: () => {
                this.toastr.success('Đã hủy đơn hàng thành công');
                this.loadOrder(this.order!.id);
                this.isCanceling = false;
            },
            error: (err) => {
                this.toastr.error(err.error?.message || 'Không thể hủy đơn hàng');
                this.isCanceling = false;
            }
        });
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

    selectedItem: OrderItem | null = null;
    isReviewModalOpen = false;

    openReviewModal(item: OrderItem) {
        this.selectedItem = item;
        this.isReviewModalOpen = true;
    }

    closeReviewModal() {
        this.isReviewModalOpen = false;
        this.selectedItem = null;
    }

    onReviewSuccess() {
        this.toastr.success('Cập nhật trạng thái đơn hàng...');
        if (this.order) {
            // Refresh order to get updated isReviewed status
            this.loadOrder(this.order.id);
        }
    }

    /** Nhóm các items theo shopName để hiển thị theo từng shop */
    getShopGroups(): { shopName: string; items: OrderItem[] }[] {
        if (!this.order?.items) return [];
        const map = new Map<string, OrderItem[]>();
        for (const item of this.order.items) {
            const key = item.shopName || 'Cửa hàng';
            if (!map.has(key)) map.set(key, []);
            map.get(key)!.push(item);
        }
        return Array.from(map.entries()).map(([shopName, items]) => ({ shopName, items }));
    }

    isLastShop(group: { shopName: string }): boolean {
        const groups = this.getShopGroups();
        return groups[groups.length - 1]?.shopName === group.shopName;
    }
}
