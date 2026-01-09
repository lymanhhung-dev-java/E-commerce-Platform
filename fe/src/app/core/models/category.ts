export interface Category {
  id: number;
  name: string;
  children?: Category[]; 
  expanded?: boolean;
  isActive?: boolean;    
}