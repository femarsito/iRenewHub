import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable } from 'rxjs';
import { CartItem } from '../models/cart-item.model';
import { Product } from '../models/product.model';

@Injectable({
  providedIn: 'root'
})
export class CartService {

  private cartItemsSubject = new BehaviorSubject<CartItem[]>([]);
  cartItems$ = this.cartItemsSubject.asObservable();

  constructor() { }

  // obtener los items actuales del carrit
  getCartItems(): CartItem[] {
    return this.cartItemsSubject.getValue();
  }

  // añade producto al carrito
  addToCart(product: Product): void {
    const currentItems = this.getCartItems();
    const existingItem = currentItems.find(item => item.product.id === product.id);

    if (existingItem) {
      // verifica que no exceda el stock
      if (existingItem.quantity < product.stock) {
        const updatedItems = currentItems.map(item =>
          item.product.id === product.id
            ? { ...item, quantity: item.quantity + 1 }
            : item
        );
        this.cartItemsSubject.next(updatedItems);
      } else {
        console.warn(`No se puede añadir más de ${product.stock} unidades de ${product.name}`);
      }
    } else {
      // si no existe, añadir nuevo item si hay stock
      if (product.stock > 0) {
        const newItem: CartItem = { product, quantity: 1 };
        this.cartItemsSubject.next([...currentItems, newItem]);
      }
    }
  }

  // elimina un producto del carrito completamente
  removeFromCart(productId: number): void {
    const updatedItems = this.getCartItems().filter(
      item => item.product.id !== productId
    );
    this.cartItemsSubject.next(updatedItems);
  }

  // aumenta cantidad de un producto
  increaseQuantity(productId: number): void {
    const currentItems = this.getCartItems();
    const item = currentItems.find(i => i.product.id === productId);
  
  // verifica que no exceda el stock
    if (item && item.quantity < item.product.stock) {
      const updatedItems = currentItems.map(i =>
        i.product.id === productId
          ? { ...i, quantity: i.quantity + 1 }
          : i
      );
      this.cartItemsSubject.next(updatedItems);
    } else if (item) {
      console.warn(`Stock máximo alcanzado para ${item.product.name}: ${item.product.stock} unidades`);
    }
  }

  // disminuye la cantidad de un producto
  decreaseQuantity(productId: number): void {
    const currentItems = this.getCartItems();
    const item = currentItems.find(i => i.product.id === productId);

    if (item && item.quantity <= 1) {
      // si la cantidad es 1, eliminar el producto
      this.removeFromCart(productId);
    } else {
      const updatedItems = currentItems.map(i =>
        i.product.id === productId
          ? { ...i, quantity: i.quantity - 1 }
          : i
      );
      this.cartItemsSubject.next(updatedItems);
    }
  }

  // obtiene el número total de items en el carrito
  getTotalItems(): number {
    return this.getCartItems().reduce((total, item) => total + item.quantity, 0);
  }

  // obtiene el precio total del carrito
  getTotalPrice(): number {
    return this.getCartItems().reduce(
      (total, item) => total + (item.product.price * item.quantity), 0
    );
  }

  // vacia el carrito completamente
  clearCart(): void {
    this.cartItemsSubject.next([]);
  }
}