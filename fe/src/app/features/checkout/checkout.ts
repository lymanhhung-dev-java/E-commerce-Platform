import { Component, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { ToastrService } from 'ngx-toastr';

import { CartService } from '../../core/services/cart.service';
import { CheckoutService } from '../../core/services/checkout.service';
import { AddressService } from '../../core/services/address.service';
import { Address } from '../../core/models/address';

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

  cartService = inject(CartService);
  checkoutService = inject(CheckoutService);
  addressService = inject(AddressService);

  savedAddresses: Address[] = [];

  // Form giữ nguyên
  checkoutForm = this.fb.group({
    receiverName: ['', Validators.required],
    phone: ['', [Validators.required, Validators.pattern('^[0-9]{10}$')]],
    address: ['', Validators.required],
    city: ['', Validators.required],
    note: [''],
    paymentMethod: ['COD', Validators.required]
  });

  ngOnInit() {
    this.loadSavedAddresses();
  }

  loadSavedAddresses() {
    this.addressService.getMyAddresses().subscribe({
      next: (res) => {
        this.savedAddresses = res;
        // Tự động chọn địa chỉ mặc định (nếu có)
        const defaultAddr = res.find(a => a.isDefault);
        if (defaultAddr) {
          this.onSelectAddress(defaultAddr);
        }
      }
    });
  }

  // --- LOGIC MỚI: Xử lý sự kiện khi chọn từ Dropdown ---
  onAddressChange(event: any) {
    const index = event.target.value; // Lấy giá trị value (là index trong mảng)

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

  // Reset form để nhập mới
  onUseNewAddress() {
    this.checkoutForm.reset();
    this.checkoutForm.patchValue({ paymentMethod: 'COD' });
  }

  onSubmit() {
    if (this.checkoutForm.invalid) {
      this.checkoutForm.markAllAsTouched();
      this.toastr.warning('Vui lòng điền đầy đủ thông tin giao hàng');
      return;
    }

    const formValue = this.checkoutForm.value;

    const itemsPayload = this.cartService.cartItems().map(item => ({
      productId: item.product.id,
      quantity: item.quantity
    }));

    if (itemsPayload.length === 0) {
      this.toastr.error('Giỏ hàng trống!');
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

    // app/features/checkout/checkout.ts

    this.checkoutService.checkout(requestData).subscribe({
      next: (response: any) => {
        console.log('Backend trả về:', response);

        let orderListStr = '';

        // Case 1: Nếu Backend sửa lại trả về mảng trực tiếp [1, 2]
        if (Array.isArray(response)) {
          orderListStr = response.join(', #');
        }
        // Case 2: Nếu Backend trả về Object { "orderId": [1, 2] } <-- ĐÂY LÀ CASE CỦA BẠN
        else if (response && typeof response === 'object') {
          if (response.orderId && Array.isArray(response.orderId)) {
            // Hứng đúng key "orderId"
            orderListStr = response.orderId.join(', #');
          }
          else if (response.id) {
            orderListStr = response.id.toString();
          }
        }

        this.toastr.success(`Đặt hàng thành công! Mã đơn: #${orderListStr}`);
        this.cartService.loadCart();
        this.router.navigate(['/profile']);
      },
      error: (err) => {
        const msg = err.error?.message || 'Có lỗi xảy ra khi đặt hàng!';
        this.toastr.error(msg);
      }
    });
  }
}