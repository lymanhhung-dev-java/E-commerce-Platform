export interface OrderItem {
  id: number; // Added from requirement
  productId: number;
  productName: string;
  productImageUrl: string;
  quantity: number;
  price: number;
  isReviewed?: boolean; // Added for review tracking
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

export interface ReviewRequest {
  productId: number;
  rating: number;
  comment: string;
}

export interface Review {
  id: number;
  productId: number;
  productName: string;
  userName: string;
  rating: number;
  comment: string;
  createdAt: string;
}
