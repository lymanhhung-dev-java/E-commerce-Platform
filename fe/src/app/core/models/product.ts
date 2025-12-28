export interface Product {
  id: number;
  name: string;
  price: number;
  imageUrl: string;
  stockQuantity: number;
  description?: string;
  categoryName?: string;
  shopName?: string;
  categoryId?: number;
  shopId?: number;
  shopAvatar?: string;
  createdAt?: string; 
  updatedAt?: string;
  rating?: number;
  productImages?: string[];
}

export interface ProductResponse {
  content: Product[];
  totalPages: number;
  totalElements: number;
  size: number;
  number: number; 
}