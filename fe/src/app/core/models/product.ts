export interface Product {
  id: number;
  name: string;
  price: number;
  imageUrl: string;      // Khớp với Java: private String imageUrl;
  categoryName: string;  // Khớp với Java
  shopName: string;      // Khớp với Java
  rating: number;        // Khớp với Java
}

// (Tùy chọn) Interface cho Response trả về từ Spring Page
export interface ProductResponse {
  content: Product[];
  totalPages: number;
  totalElements: number;
  size: number;
  number: number; // trang hiện tại
}