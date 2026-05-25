// Gestiona el carrito con BehaviorSubject. Se persiste en localStorage con clave por usuario
// (irenewhub_cart_email@ejemplo.com) para que cada cuenta tenga su propio carrito.

import { Injectable } from '@angular/core';
import { BehaviorSubject } from 'rxjs';
import { CartItem } from '../models/cart-item.model';
import { Product } from '../models/product.model';
import { AuthService } from './auth';

const CART_KEY_PREFIX = 'irenewhub_cart_';

@Injectable({
  providedIn: 'root'
})
export class CartService {

  private cartItemsSubject = new BehaviorSubject<CartItem[]>([]);
  cartItems$ = this.cartItemsSubject.asObservable();

  appliedCoupon: { code: string; discountPercent: number } | null = null;

  constructor(private authService: AuthService) {
    // Al arrancar el servicio cargamos el carrito del usuario actual (si hay sesión)
    this.recargarParaUsuario();
  }

  // Devuelve la clave de localStorage específica para el usuario logueado.
  // Si no hay sesión activa usa "guest" como fallback.
  private getCartKey(): string {
    const email = this.authService.getEmail();
    return CART_KEY_PREFIX + (email || 'guest');
  }

  // Carga desde localStorage el carrito del usuario actualmente logueado.
  // Se llama al arrancar la app y después de hacer login.
  recargarParaUsuario(): void {
    try {
      const raw = localStorage.getItem(this.getCartKey());
      this.cartItemsSubject.next(raw ? JSON.parse(raw) : []);
    } catch {
      this.cartItemsSubject.next([]);
    }
    this.appliedCoupon = null;
  }

  // ---- OPERACIONES ----

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
    localStorage.removeItem(this.getCartKey());
    this.cartItemsSubject.next([]);
    this.appliedCoupon = null;
  }

  // Emite la nueva lista y la guarda en localStorage con la clave del usuario
  private emitir(items: CartItem[]): void {
    this.cartItemsSubject.next(items);
    localStorage.setItem(this.getCartKey(), JSON.stringify(items));
  }
}
