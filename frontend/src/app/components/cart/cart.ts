import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { HttpClient } from '@angular/common/http';
import { CartItem } from '../../models/cart-item.model';
import { CartService } from '../../services/cart';

@Component({
  selector: 'app-cart',
  standalone: false,
  templateUrl: './cart.html',
  styleUrl: './cart.css'
})
export class Cart implements OnInit {

  readonly API = 'http://localhost:8080/api';

  cartItems: CartItem[] = [];
  totalPrice: number = 0;
  totalItems: number = 0;

  // ---- CUPÓN ----
  couponInput = '';
  couponError = '';
  couponSuccess = '';
  isValidatingCoupon = false;

  constructor(
    private cartService: CartService,
    private router: Router,
    private http: HttpClient
  ) { }

  ngOnInit(): void {
    this.cartService.cartItems$.subscribe((items: CartItem[]) => {
      this.cartItems = items;
      this.totalPrice = this.cartService.getTotalPrice();
      this.totalItems = this.cartService.getTotalItems();
    });
  }

  get appliedCoupon() { return this.cartService.appliedCoupon; }

  get discountAmount(): number {
    if (!this.appliedCoupon) return 0;
    const base = this.totalPrice + (this.totalPrice >= 50 ? 0 : 4.99);
    return base * this.appliedCoupon.discountPercent / 100;
  }

  get finalTotal(): number {
    const base = this.totalPrice + (this.totalPrice >= 50 ? 0 : 4.99);
    return base - this.discountAmount;
  }

  applyCoupon(): void {
    const code = this.couponInput.trim().toUpperCase();
    if (!code) return;
    this.couponError = '';
    this.couponSuccess = '';
    this.isValidatingCoupon = true;

    this.http.get<any>(`${this.API}/coupons/validate?code=${code}`).subscribe({
      next: (res) => {
        this.isValidatingCoupon = false;
        if (res.valid) {
          this.cartService.appliedCoupon = { code, discountPercent: res.discountPercent };
          this.couponSuccess = `Cupón aplicado: ${res.discountPercent}% de descuento`;
          this.couponInput = '';
        } else {
          this.couponError = res.message || 'Cupón no válido.';
        }
      },
      error: () => {
        this.isValidatingCoupon = false;
        this.couponError = 'Error al validar el cupón.';
      }
    });
  }

  removeCoupon(): void {
    this.cartService.appliedCoupon = null;
    this.couponSuccess = '';
    this.couponError = '';
  }

  increaseQuantity(productId: number): void { this.cartService.increaseQuantity(productId); }
  decreaseQuantity(productId: number): void { this.cartService.decreaseQuantity(productId); }
  removeItem(productId: number): void { this.cartService.removeFromCart(productId); }
  clearCart(): void { this.cartService.clearCart(); }
  goToProducts(): void { this.router.navigate(['/products']); }
  goToCheckout(): void { this.router.navigate(['/checkout']); }
}