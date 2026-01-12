import { Component, inject, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { ToastrService } from 'ngx-toastr';

import { CartService } from '../../core/services/cart.service';
import { CheckoutService } from '../../core/services/checkout.service';
import { AddressService } from '../../core/services/address.service';
import { Address } from '../../core/models/address';
import { interval, Subscription, switchMap, takeWhile } from 'rxjs';

@Component({
  selector: 'app-checkout',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink], // 2. Thêm RouterLink vào đây
  templateUrl: './checkout.html',
  styleUrl: './checkout.css'
})
export class CheckoutComponent implements OnInit {
  fb = inject(FormBuilder);
  router = inject(Router);
  toastr = inject(ToastrService);
  cdr = inject(ChangeDetectorRef);
  cartService = inject(CartService);
  checkoutService = inject(CheckoutService);
  addressService = inject(AddressService);

  showQrModal: boolean = false;
  qrCodeUrl: string = '';
  currentOrderId: number | null = null;

  countdownTime: number = 180;
  displayTime: string = '03:00';
  isPaymentSuccess: boolean = false;
  isExpired: boolean = false;

  private countdownSubscription: Subscription | null = null;
  private pollingSubscription: Subscription | null = null;

  savedAddresses: Address[] = [];
  selectedAddressIndex: any = 'null';

  // Form giữ nguyên
  checkoutForm = this.fb.group({
    receiverName: ['', Validators.required],
    phone: ['', [Validators.required, Validators.pattern('^[0-9]{10}$')]],
    address: ['', Validators.required],
    city: ['', Validators.required],
    note: [''],
    paymentMethod: ['COD', Validators.required]
  });


  paymentMethods = [
    {
      code: 'COD',
      name: 'Thanh toán khi nhận hàng (COD)',
      icon: 'bi-cash-stack',
      description: 'Thanh toán bằng tiền mặt khi nhận hàng. Phí thu hộ: 0đ.'
    },
    {
      code: 'BANK_TRANSFER',
      name: 'Chuyển khoản ngân hàng',
      icon: 'bi-credit-card',
      description: 'Thực hiện chuyển khoản vào STK công ty. Đơn hàng sẽ được xử lý sau khi nhận tiền.'
    }
  ];

  ngOnInit() {
    this.loadSavedAddresses();
  }

  onBankTransferCheckout(orderId: number) {
    this.currentOrderId = orderId;

    // 1. Lấy mã QR
    this.checkoutService.getPaymentQrUrl(orderId).subscribe(url => {
      this.qrCodeUrl = url;
      this.showQrModal = true;

      // 2. Bắt đầu đếm ngược và kiểm tra tự động
      this.startPaymentProcess();
    });
  }

  startPaymentProcess() {
    this.countdownTime = 180; // Reset về 3 phút
    this.isPaymentSuccess = false;
    this.isExpired = false;

    //Đếm ngược thời gian (1s chạy 1 lần) ---
    this.countdownSubscription = interval(1000).subscribe(() => {
      this.countdownTime--;
      this.displayTime = this.formatTime(this.countdownTime);

      this.cdr.detectChanges();

      if (this.countdownTime <= 0) {
        this.stopPaymentProcess();
        this.isExpired = true;
        this.cdr.detectChanges(); // Cập nhật lần cuối để hiện thông báo hết hạn
      }
    });

    this.pollingSubscription = interval(5000)
      .pipe(
        switchMap(() => this.checkoutService.checkPaymentStatus(this.currentOrderId!)),
        takeWhile(() => this.countdownTime > 0 && !this.isPaymentSuccess)
      )
      .subscribe((isPaid: boolean) => {
        if (isPaid) {
          this.handlePaymentSuccess();
        } else {
          console.log('Đang kiểm tra thanh toán... Chưa thấy tiền.');
        }
      });
  }
  handlePaymentSuccess() {
    this.isPaymentSuccess = true;
    this.stopPaymentProcess();
    this.cdr.detectChanges();
    setTimeout(() => {
      this.showQrModal = false;
      this.router.navigate(['/order-success']);
    }, 3500);
  }

  stopPaymentProcess() {
    // Hủy các luồng đếm giờ và gọi API
    if (this.countdownSubscription) {
      this.countdownSubscription.unsubscribe();
    }
    if (this.pollingSubscription) {
      this.pollingSubscription.unsubscribe();
    }
  }

  loadSavedAddresses() {
    this.addressService.getMyAddresses().subscribe({
      next: (res) => {
        this.savedAddresses = res;
        // Tự động chọn địa chỉ mặc định (nếu có)
        const defaultIndex = res.findIndex(a => a.isDefault);
        if (defaultIndex !== -1) {
          this.selectedAddressIndex = defaultIndex;
          this.onSelectAddress(res[defaultIndex]);
        }
      }
    });
  }

  // --- LOGIC MỚI: Xử lý sự kiện khi chọn từ Dropdown ---
  onAddressChange(event: any) {
    const index = event.target.value;
    this.selectedAddressIndex = index;

    if (index !== "" && index !== null && index !== "null") {
      // Nếu chọn 1 địa chỉ cụ thể
      const selectedAddr = this.savedAddresses[Number(index)];
      if (selectedAddr) {
        this.onSelectAddress(selectedAddr);
      }
    } else {
      // Nếu chọn "-- Chọn địa chỉ --" hoặc reset
      this.onUseNewAddress();
    }
  }
  // ----------------------------------------------------

  // Hàm điền dữ liệu vào form
  onSelectAddress(addr: Address) {
    this.checkoutForm.patchValue({
      receiverName: addr.receiverName,
      phone: addr.phoneNumber,
      // Tách hoặc gộp địa chỉ tùy logic của bạn. 
      // Ở giao diện mới ta có 1 ô "Địa chỉ", nên gộp lại cho gọn:
      address: `${addr.street}, ${addr.ward}, ${addr.district}`,
      city: addr.city
    });
  }

  closeModal() {
    if (this.isPaymentSuccess) return; // Nếu đã thành công thì ko cho hủy

    // Nếu hết giờ hoặc người dùng bấm nút Quay lại -> Gọi API hủy đơn
    if (this.currentOrderId) {
      this.stopPaymentProcess(); // Dừng check ngay

      this.checkoutService.cancelOrder(this.currentOrderId).subscribe({
        next: () => {
          this.toastr.info('Đã hủy đơn hàng. Giỏ hàng đã được khôi phục.');
          this.showQrModal = false;
          this.currentOrderId = null;
          this.cartService.loadCart(); // Load lại giỏ hàng cũ
        },
        error: (err) => {
          const msg = err.error?.message || '';
          // CASE ĐẶC BIỆT: Backend phát hiện tiền đã vào rồi -> Không cho hủy
          if (msg.includes('đã vào tài khoản') || msg.includes('thành công')) {
            this.handlePaymentSuccess();
            this.toastr.success('Phát hiện tiền vừa vào! Đơn hàng được xác nhận.');
          } else {
            this.toastr.error('Lỗi khi hủy đơn: ' + msg);
          }
        }
      });
    } else {
      this.showQrModal = false;
    }
  }

  formatTime(seconds: number): string {
    const minutes: number = Math.floor(seconds / 60);
    const remainingSeconds: number = seconds % 60;
    return `${this.pad(minutes)}:${this.pad(remainingSeconds)}`;
  }
  pad(val: number): string {
    return val < 10 ? `0${val}` : `${val}`;
  }

  // Reset form để nhập mới
  onUseNewAddress() {
    this.checkoutForm.reset();
    this.checkoutForm.patchValue({ paymentMethod: 'COD' });
  }

  shippingCost = 25000;
  discount = 20.00;

  get selectedItems() {
    return this.cartService.cartItems().filter(item => item.selected);
  }

  get finalTotal() {
    return this.cartService.subTotalSelected() + this.shippingCost - this.discount;
  }

  onSubmit() {
    if (this.checkoutForm.invalid) {
      this.checkoutForm.markAllAsTouched();
      this.toastr.warning('Vui lòng điền đầy đủ thông tin giao hàng');
      return;
    }

    const formValue = this.checkoutForm.value;

    const itemsPayload = this.selectedItems.map(item => ({
      productId: item.product.id,
      quantity: item.quantity
    }));

    if (itemsPayload.length === 0) {
      this.toastr.error('Vui lòng chọn sản phẩm để thanh toán!');
      return;
    }

    const finalAddress = `${formValue.address}, ${formValue.city} (Người nhận: ${formValue.receiverName})`;

    const requestData = {
      items: itemsPayload,
      shippingAddress: finalAddress,
      shippingPhone: formValue.phone,
      note: formValue.note,
      paymentMethod: formValue.paymentMethod
    };

    this.checkoutService.checkout(requestData).subscribe({
      next: (response: any) => {
        let orderIds: number[] = [];
        if (Array.isArray(response)) orderIds = response;
        else if (response?.orderId) orderIds = response.orderId;
        else if (response?.id) orderIds = [response.id];

        this.cartService.loadCart();

        if (formValue.paymentMethod === 'BANK_TRANSFER' && orderIds.length > 0) {
          this.onBankTransferCheckout(orderIds[0]);
        } else {
          this.toastr.success('Đặt hàng thành công!');
          this.router.navigate(['/profile/orders']);
        }
      },
      error: (err) => this.toastr.error(err.error?.message || 'Lỗi đặt hàng')
    });
  }

  ngOnDestroy() {
    this.stopPaymentProcess();
  }
}