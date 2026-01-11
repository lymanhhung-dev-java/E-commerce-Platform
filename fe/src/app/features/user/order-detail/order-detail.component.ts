import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { OrderService } from '../../../core/services/order.service';
import { Order } from '../../../core/models/order';
import { ToastrService } from 'ngx-toastr';

@Component({
    selector: 'app-order-detail',
    standalone: true,
    imports: [CommonModule, RouterModule],
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

    getStatusClass(status: string): string {
        switch (status) {
            case 'DELIVERED': return 'bg-success-subtle text-success border-success-subtle';
            case 'SHIPPED': return 'bg-primary-subtle text-primary border-primary-subtle';
            case 'PENDING': return 'bg-warning-subtle text-warning border-warning-subtle';
            case 'CANCELED': return 'bg-danger-subtle text-danger border-danger-subtle';
            default: return 'bg-light text-secondary';
        }
    }
}
