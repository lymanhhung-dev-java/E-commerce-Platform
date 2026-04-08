export interface MerchantProductResponse {
  id: number;
  name: string;
  price: number;
  stockQuantity: number;
  description: string;
  imageUrl: string;
  categoryId: number;
  categoryName: string;
  active: boolean;
  status?: boolean; // Backup field
  editPrice?: number; // Used for bulk update UI
  editStock?: number; // Used for bulk update UI
}