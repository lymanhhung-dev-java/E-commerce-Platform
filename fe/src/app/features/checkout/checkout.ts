import { Component, inject, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators, FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { ToastrService } from 'ngx-toastr';

import { CartService } from '../../core/services/cart.service';
import { CheckoutService } from '../../core/services/checkout.service';
import { AddressService } from '../../core/services/address.service';
import { Address } from '../../core/models/address';
import { LocationService, Province, Ward } from '../../core/services/location.service';
import { VoucherService } from '../../core/services/voucher.service';
import { interval, Subscription, switchMap, takeWhile } from 'rxjs';

@Component({
  selector: 'app-checkout',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink, FormsModule], // 2. Thêm RouterLink vào đây
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
  locationService = inject(LocationService);
  voucherService = inject(VoucherService);

  showQrModal: boolean = false;
  qrInfo: any = null;
  currentOrderId: number | null = null;

  countdownTime: number = 180;
  displayTime: string = '03:00';
  isPaymentSuccess: boolean = false;
  isExpired: boolean = false;

  private countdownSubscription: Subscription | null = null;
  private pollingSubscription: Subscription | null = null;

  savedAddresses: Address[] = [];
  selectedAddressIndex: any = 'null';

  provinces: Province[] = [];
  wards: Ward[] = [];

  savedVouchers: any[] = [];
  shopVouchers: any[] = [];
  systemVouchers: any[] = [];
  
  selectedShopVoucherId: number | null = null;
  selectedSystemVoucherId: number | null = null;
  shopDiscountAmount: number = 0;
  systemDiscountAmount: number = 0;

  showVoucherModal: boolean = false;
  activeVoucherTab: 'SHOP' | 'SYSTEM' = 'SHOP';

  // Form giữ nguyên
  checkoutForm = this.fb.group({
    receiverName: ['', Validators.required],
    phone: ['', [Validators.required, Validators.pattern('^[0-9]{10}$')]],
    street: ['', Validators.required],
    ward: ['', Validators.required],
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
    this.loadProvinces();
    this.loadMyVouchers();

    this.checkoutForm.get('city')?.valueChanges.subscribe(cityName => {
      if (cityName) {
        this.locationService.getWardsByProvinceName(cityName).subscribe(wards => {
          this.wards = wards;
          const currentWard = this.checkoutForm.get('ward')?.value;
          if (currentWard && !this.wards.find(w => w.name === currentWard)) {
            this.checkoutForm.get('ward')?.setValue('', { emitEvent: false });
          }
        });
      } else {
        this.wards = [];
        this.checkoutForm.get('ward')?.setValue('', { emitEvent: false });
      }
    });
  }

  loadProvinces() {
    this.locationService.getProvinces().subscribe({
      next: (res) => this.provinces = res,
      error: () => this.toastr.error('Lỗi tải danh sách tỉnh/thành')
    });
  }

  loadMyVouchers() {
    this.voucherService.getMySavedVouchers(0, 100).subscribe({
      next: (res) => {
        const vouchers = res.content || res;
        this.savedVouchers = vouchers;
        this.shopVouchers = vouchers.filter((v: any) => v.ownerType === 'SHOP');
        this.systemVouchers = vouchers.filter((v: any) => v.ownerType === 'SYSTEM');
        
        // Filter out Shop Vouchers to only include those matching the cart items' shop.
        // Assuming all items in checkout are from the same shop for now.
        if (this.selectedItems.length > 0) {
           const shopIdStr = this.selectedItems[0].product.shopId || (this.selectedItems[0].product as any).shop?.id;
           if (shopIdStr) {
               this.shopVouchers = this.shopVouchers.filter(v => v.shopId == shopIdStr);
           }
        }
      }
    });
  }

  calculateDiscounts() {
    const subTotal = this.cartService.subTotalSelected();
    this.shopDiscountAmount = 0;
    this.systemDiscountAmount = 0;

    if (this.selectedShopVoucherId) {
       const v = this.shopVouchers.find(v => v.voucherId == this.selectedShopVoucherId);
       if (v) {
          if (v.discountType === 'FIXED') {
             this.shopDiscountAmount = v.discountValue;
          } else {
             this.shopDiscountAmount = (subTotal * v.discountValue) / 100;
             if (v.maxDiscount && this.shopDiscountAmount > v.maxDiscount) {
                this.shopDiscountAmount = v.maxDiscount;
             }
          }
       }
    }

    const amountAfterShop = Math.max(0, subTotal - this.shopDiscountAmount);

    if (this.selectedSystemVoucherId) {
       const v = this.systemVouchers.find(v => v.voucherId == this.selectedSystemVoucherId);
       if (v) {
          if (v.discountType === 'FIXED') {
             this.systemDiscountAmount = v.discountValue;
          } else {
             this.systemDiscountAmount = (amountAfterShop * v.discountValue) / 100;
             if (v.maxDiscount && this.systemDiscountAmount > v.maxDiscount) {
                this.systemDiscountAmount = v.maxDiscount;
             }
          }
       }
    }
  }

  onVoucherChange() {
     this.calculateDiscounts();
  }

  openVoucherModal(tab: 'SHOP' | 'SYSTEM') {
    this.activeVoucherTab = tab;
    this.showVoucherModal = true;
  }

  closeVoucherModal() {
    this.showVoucherModal = false;
  }

  selectVoucher(voucher: any, type: 'SHOP' | 'SYSTEM') {
    if (type === 'SHOP') {
      if (this.selectedShopVoucherId === voucher.voucherId) {
        this.selectedShopVoucherId = null; // deselect
      } else {
        this.selectedShopVoucherId = voucher.voucherId;
      }
    } else {
      if (this.selectedSystemVoucherId === voucher.voucherId) {
        this.selectedSystemVoucherId = null; // deselect
      } else {
        this.selectedSystemVoucherId = voucher.voucherId;
      }
    }
    this.onVoucherChange();
    this.closeVoucherModal();
    this.toastr.success('Áp dụng Voucher thành công!');
  }

  getSelectedShopVoucher() {
    return this.shopVouchers.find(v => v.voucherId === this.selectedShopVoucherId);
  }

  getSelectedSystemVoucher() {
    return this.systemVouchers.find(v => v.voucherId === this.selectedSystemVoucherId);
  }

  onBankTransferCheckout(orderId: number) {
    this.currentOrderId = orderId;

    // 1. Lấy mã QR
    this.checkoutService.getPaymentQrUrl(orderId).subscribe(res => {
      this.qrInfo = res;
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
    if (addr.city) {
      this.locationService.getWardsByProvinceName(addr.city).subscribe(wards => {
        this.wards = wards;
        this.checkoutForm.patchValue({
          receiverName: addr.receiverName,
          phone: addr.phoneNumber,
          street: addr.street,
          ward: addr.ward,
          city: addr.city
        });
      });
    } else {
      this.checkoutForm.patchValue({
        receiverName: addr.receiverName,
        phone: addr.phoneNumber,
        street: addr.street,
        ward: addr.ward,
        city: addr.city
      });
    }
  }

  closeModal() {
    if (this.isPaymentSuccess) return; // Nếu đã thành công thì ko cho hủy

    // Nếu hết giờ hoặc người dùng bấm nút Quay lại -> Gọi API hủy đơn
    if (this.currentOrderId) {
      this.stopPaymentProcess(); // Dừng check ngay

      this.checkoutService.cancelOrder(this.currentOrderId).subscribe({
        next: () => {
          this.toastr.info('Đã hủy đơn hàng');
          this.showQrModal = false;
          this.currentOrderId = null;
          this.cartService.loadCart();
          this.router.navigate(['/cart']); // Chuyển hướng về giỏ hàng
        },
        error: (err) => {
          console.error('Cancel Order Error:', err);
          let msg = '';
          if (typeof err.error === 'string') {
            msg = err.error;
          } else if (err.error?.message) {
            msg = err.error.message;
          } else {
            msg = err.message || 'Lỗi không xác định';
          }

          // CASE 1: 401 Unauthorized (Guest hoặc Hết phiên)
          if (err.status === 401) {
            this.toastr.warning('Phiên làm việc hết hạn. Giao dịch sẽ tự động xử lý sau.');
            this.showQrModal = false;
            this.currentOrderId = null;
            this.cartService.loadCart(); // Vẫn load lại cart để đảm bảo sync
            return;
          }

          // CASE 2: Backend phát hiện tiền đã vào rồi -> Không cho hủy
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

  shippingCost = 0;

  get selectedItems() {
    return this.cartService.cartItems().filter(item => item.selected);
  }

  get finalTotal() {
    return Math.max(0, this.cartService.subTotalSelected() + this.shippingCost - this.shopDiscountAmount - this.systemDiscountAmount);
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

    const finalAddress = `${formValue.street}, ${formValue.ward}, ${formValue.city} (Người nhận: ${formValue.receiverName})`;

    const requestData: any = {
      items: itemsPayload,
      shippingAddress: finalAddress,
      shippingPhone: formValue.phone,
      note: formValue.note,
      paymentMethod: formValue.paymentMethod
    };

    if (this.selectedShopVoucherId) {
      requestData.shopVoucherId = this.selectedShopVoucherId;
    }
    if (this.selectedSystemVoucherId) {
      requestData.systemVoucherId = this.selectedSystemVoucherId;
    }

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

  copyToClipboard(text: string, fieldName: string) {
    if (navigator.clipboard) {
      navigator.clipboard.writeText(text).then(() => {
        this.toastr.success(`Đã sao chép ${fieldName}`);
      }).catch(err => {
        this.toastr.error('Lỗi khi sao chép');
      });
    } else {
      // Fallback
      const textArea = document.createElement("textarea");
      textArea.value = text;
      document.body.appendChild(textArea);
      textArea.select();
      try {
        document.execCommand('copy');
        this.toastr.success(`Đã sao chép ${fieldName}`);
      } catch (err) {
        this.toastr.error('Lỗi khi sao chép');
      }
      document.body.removeChild(textArea);
    }
  }

  ngOnDestroy() {
    this.stopPaymentProcess();
  }
}