export interface MerchantProductResponse {
  id: number;
  name: string;
  price: number;
  stock: number;
  description: string;
  imageUrl: string;
  categoryId: number;
  categoryName: string;
  active: boolean; 
}