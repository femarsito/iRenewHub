// Define que propiedades tiene un producto en la aplicación

export interface Product {
  id: number;
  name: string;
  category: string;
  price: number;
  originalPrice?: number;
  description: string;
  imageUrl: string;
  stock: number;
  isOEM: boolean; // true = Original (OEM), false = Generica
  specifications: {
    warranty: string;
    condition: string;
    manufacturer: string;
  };
}