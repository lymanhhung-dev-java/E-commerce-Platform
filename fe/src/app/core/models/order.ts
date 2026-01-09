export interface OrderItem {
  productId: number;
  productName: string;
  productImageUrl: string;
  quantity: number;
  price: number;
}

export type OrderStatus = 'PENDING' | 'PROCESSING' | 'SHIPPED' | 'DELIVERED' | 'CANCELED' | 'RETURNED';

export interface Order {
  id: number;
  customerName: string;
  shippingAddress: string;
  shippingPhone: string;
  note?: string; 
  paymentMethod: string;
  status: OrderStatus;
  totalAmount: number;
  createdAt: string; 
  items: OrderItem[];
}