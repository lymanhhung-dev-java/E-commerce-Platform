import { Product } from './product';

export interface CartItem {
  selected: unknown;
  product: Product;
  quantity: number;
}