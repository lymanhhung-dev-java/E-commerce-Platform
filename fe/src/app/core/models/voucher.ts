export interface Voucher {
  id: number;
  code: string;
  discountValue: number;
  discountType: 'PERCENT' | 'FIXED';
  ownerType: 'SYSTEM' | 'SHOP';
  shopId: number | null;
  minOrderValue: number;
  maxDiscount: number;
  startDate: string;
  endDate: string;
  createdAt: string;
  updatedAt: string;
}

export interface VoucherRequest {
  code: string;
  discountValue: number;
  discountType: 'PERCENT' | 'FIXED';
  minOrderValue: number;
  maxDiscount: number;
  startDate: string;
  endDate: string;
}
