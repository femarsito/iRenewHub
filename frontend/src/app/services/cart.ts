// ─── SERVICIO DEL CARRITO ─────────────────────────────────────────────────────
// Gestiona los items del carrito con un BehaviorSubject (patrón observable).
//
// PERSISTENCIA: el carrito se guarda en localStorage en cada cambio.
// Así sobrevive a recargas de página (F5) y al restablecimiento de sesión.
// Solo se borra al llamar a clearCart() (cuando se completa el pedido).

import { Injectable } from '@angular/core';
import { BehaviorSubject } from 'rxjs';
import { CartItem } from '../models/cart-item.model';
import { Product } from '../models/product.model';

const CART_KEY = 'irenewhub_cart';

@Injectable({
  providedIn: 'root'
})
export class CartService {

  // Inicializamos el BehaviorSubject con los datos guardados en localStorage
  private cartItemsSubject = new BehaviorSubject<CartItem[]>(this.cargarDeStorage());
  cartItems$ = this.cartItemsSubject.asObservable();

  constructor() {}

  // ─── OPERACIONES DEL CARRITO ──────────────────────────────────────────────

  getCartItems(): CartItem[] {
    return this.cartItemsSubject.getValue();
  }

  addToCart(product: Product): void {
    const currentItems = this.getCartItems();
    const existingItem = currentItems.find(item => item.product.id === product.id);

    if (existingItem) {
      if (existingItem.quantity < product.stock) {
        const updatedItems = currentItems.map(item =>
          item.product.id === product.id
            ? { ...item, quantity: item.quantity + 1 }
            : item
        );
        this.emitir(updatedItems);
      }
    } else {
      if (product.stock > 0) {
        this.emitir([...currentItems, { product, quantity: 1 }]);
      }
    }
  }

  removeFromCart(productId: number): void {
    this.emitir(this.getCartItems().filter(item => item.product.id !== productId));
  }

  increaseQuantity(productId: number): void {
    const item = this.getCartItems().find(i => i.product.id === productId);
    if (item && item.quantity < item.product.stock) {
      this.emitir(this.getCartItems().map(i =>
        i.product.id === productId ? { ...i, quantity: i.quantity + 1 } : i
      ));
    }
  }

  decreaseQuantity(productId: number): void {
    const item = this.getCartItems().find(i => i.product.id === productId);
    if (!item) return;
    if (item.quantity <= 1) {
      this.removeFromCart(productId);
    } else {
      this.emitir(this.getCartItems().map(i =>
        i.product.id === productId ? { ...i, quantity: i.quantity - 1 } : i
      ));
    }
  }

  getTotalItems(): number {
    return this.getCartItems().reduce((total, item) => total + item.quantity, 0);
  }

  getTotalPrice(): number {
    return this.getCartItems().reduce(
      (total, item) => total + (item.product.price * item.quantity), 0
    );
  }

  clearCart(): void {
    localStorage.removeItem(CART_KEY);
    this.cartItemsSubject.next([]);
  }

  // ─── PERSISTENCIA ─────────────────────────────────────────────────────────

  // Emite la nueva lista y la guarda en localStorage de forma atómica
  private emitir(items: CartItem[]): void {
    this.cartItemsSubject.next(items);
    localStorage.setItem(CART_KEY, JSON.stringify(items));
  }

  // Lee el carrito guardado en localStorage al arrancar el servicio
  private cargarDeStorage(): CartItem[] {
    try {
      const raw = localStorage.getItem(CART_KEY);
      return raw ? JSON.parse(raw) : [];
    } catch {
      return [];  // Si el JSON está corrupto, empezamos con carrito vacío
    }
  }
}
