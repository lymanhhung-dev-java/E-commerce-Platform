import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { MerchantStatisticService } from '../../../core/services/merchant-statistic.service';
import { MerchantWalletService } from '../../../core/services/merchant-wallet.service';

@Component({
  selector: 'app-merchant-wallet',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './merchant-wallet.html',
  styleUrls: ['./merchant-wallet.css'] 
})
export class MerchantWalletComponent implements OnInit {
  walletData: any = null; 
  periodRevenue: number = 0; 
  
  showWithdrawModal = false;
  withdrawForm: FormGroup;

  constructor(
    private walletService: MerchantWalletService, 
    private statisticService: MerchantStatisticService, 
    private fb: FormBuilder
  ) {
    this.withdrawForm = this.fb.group({
      amount: [0, [Validators.required, Validators.min(50000)]], 
      bankName: ['MBBank', Validators.required], 
      accountNumber: ['', Validators.required],
      accountName: ['', [Validators.required, Validators.minLength(3)]]
    });
  }

  ngOnInit() {
    this.loadWalletInfo();
    this.loadPeriodRevenue('MONTH'); 
  }

  loadWalletInfo() {
    this.walletService.getWalletOverview().subscribe(res => {
      this.walletData = res;
    });
  }

  onChangePeriod(event: any) {
    const type = event.target.value; 
    this.loadPeriodRevenue(type);
  }

  loadPeriodRevenue(type: string) {
    this.statisticService.getRevenueStats(type as any).subscribe(data => {
      this.periodRevenue = data.reduce((sum: number, item: any) => sum + item.value, 0);
    });
  }

  fillMaxAmount() {
    if (this.walletData?.currentBalance) {
      this.withdrawForm.patchValue({ amount: this.walletData.currentBalance });
    }
  }

  onWithdraw() {
    if (this.withdrawForm.invalid) {
      this.withdrawForm.markAllAsTouched();
      return;
    }
    
    const requestAmount = this.withdrawForm.value.amount;
    const currentBalance = this.walletData?.currentBalance || 0;

    if (requestAmount > currentBalance) {
      alert('Số dư không đủ để thực hiện giao dịch!');
      return;
    }

    this.walletService.requestWithdraw(this.withdrawForm.value).subscribe({
      next: () => {
        alert('Yêu cầu rút tiền thành công!');
        this.showWithdrawModal = false;
        this.loadWalletInfo(); 
      },
      error: (err) => alert('Lỗi: ' + (err.error || err.message))
    });
  }
  
  openWithdrawModal() {
     this.showWithdrawModal = true;
     this.withdrawForm.reset({ 
       amount: 0, 
       bankName: 'MBBank', 
       accountNumber: '', 
       accountName: '' 
     });
  }
}