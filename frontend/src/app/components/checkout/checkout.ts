// ─── COMPONENTE CHECKOUT ──────────────────────────────────────────────────────
// Proceso de compra en 3 pasos:
//   Paso 1: Datos de envío (formulario)
//   Paso 2: Datos de pago (formulario — solo simulación, no se cobra realmente)
//   Paso 3: Resumen y confirmación
//
// Al confirmar, llama al backend POST /api/orders que guarda el pedido en base de datos.
// El JwtInterceptor añade automáticamente el token JWT a la petición.

import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { HttpClient } from '@angular/common/http';
import { CartItem } from '../../models/cart-item.model';
import { CartService } from '../../services/cart';

interface OrderSummaryItem {
  productName: string;
  quantity: number;
  subtotal: number;
}

@Component({
  selector: 'app-checkout',
  standalone: false,
  templateUrl: './checkout.html',
  styleUrl: './checkout.css'
})
export class Checkout implements OnInit {

  currentStep: number = 1;
  totalSteps: number = 3;

  shippingForm!: FormGroup;
  paymentForm!: FormGroup;

  cartItems: CartItem[] = [];
  subtotal: number = 0;
  shippingCost: number = 0;
  total: number = 0;

  orderSummary: OrderSummaryItem[] = [];
  totalSnapshot: number = 0;
  shippingCostSnapshot: number = 0;

  isProcessing: boolean = false;
  orderCompleted: boolean = false;
  orderNumber: string = '';
  errorMessage: string = '';

  constructor(
    private formBuilder: FormBuilder,
    private cartService: CartService,
    private http: HttpClient,
    private router: Router
  ) { }

  ngOnInit(): void {
    this.cartService.cartItems$.subscribe((items: CartItem[]) => {
      this.cartItems = items;
      this.subtotal = this.cartService.getTotalPrice();
      this.shippingCost = this.subtotal >= 50 ? 0 : 4.99;
      this.total = this.subtotal + this.shippingCost;
    });

    if (this.cartItems.length === 0) {
      this.router.navigate(['/cart']);
      return;
    }

    this.initForms();
  }

  initForms(): void {
    this.shippingForm = this.formBuilder.group({
      firstName:  ['', [Validators.required, Validators.minLength(2)]],
      lastName:   ['', [Validators.required, Validators.minLength(2)]],
      email:      ['', [Validators.required, Validators.email]],
      phone:      ['', [Validators.required, Validators.pattern(/^[0-9]{9}$/)]],
      address:    ['', [Validators.required, Validators.minLength(10)]],
      city:       ['', Validators.required],
      postalCode: ['', [Validators.required, Validators.pattern(/^[0-9]{5}$/)]],
      province:   ['', Validators.required]
    });

    this.paymentForm = this.formBuilder.group({
      cardName:   ['', [Validators.required, Validators.minLength(3)]],
      cardNumber: ['', [Validators.required, Validators.pattern(/^[0-9]{16}$/)]],
      expiryDate: ['', [Validators.required, Validators.pattern(/^(0[1-9]|1[0-2])\/([0-9]{2})$/)]],
      cvv:        ['', [Validators.required, Validators.pattern(/^[0-9]{3,4}$/)]]
    });
  }

  // Getters formulario de envío
  get firstName()  { return this.shippingForm.get('firstName'); }
  get lastName()   { return this.shippingForm.get('lastName'); }
  get email()      { return this.shippingForm.get('email'); }
  get phone()      { return this.shippingForm.get('phone'); }
  get address()    { return this.shippingForm.get('address'); }
  get city()       { return this.shippingForm.get('city'); }
  get postalCode() { return this.shippingForm.get('postalCode'); }
  get province()   { return this.shippingForm.get('province'); }

  // Getters formulario de pago
  get cardName()   { return this.paymentForm.get('cardName'); }
  get cardNumber() { return this.paymentForm.get('cardNumber'); }
  get expiryDate() { return this.paymentForm.get('expiryDate'); }
  get cvv()        { return this.paymentForm.get('cvv'); }

  nextStep(): void {
    if (this.currentStep === 1) {
      if (this.shippingForm.invalid) {
        Object.keys(this.shippingForm.controls).forEach(k => this.shippingForm.get(k)?.markAsTouched());
        return;
      }
      this.currentStep = 2;
    } else if (this.currentStep === 2) {
      if (this.paymentForm.invalid) {
        Object.keys(this.paymentForm.controls).forEach(k => this.paymentForm.get(k)?.markAsTouched());
        return;
      }
      this.currentStep = 3;
    } else if (this.currentStep === 3) {
      this.confirmPayment();
    }
    window.scrollTo(0, 0);
  }

  prevStep(): void {
    if (this.currentStep > 1) {
      this.currentStep--;
      window.scrollTo(0, 0);
    }
  }

  // ─── CONFIRMAR PAGO ─────────────────────────────────────────────────────────
  // Llama al backend POST /api/orders con los datos del formulario y el carrito.
  // El JwtInterceptor añade el token JWT automáticamente para identificar al usuario.
  confirmPayment(): void {
    this.isProcessing = true;
    this.errorMessage = '';

    // Guardar snapshot visual antes de vaciar el carrito
    this.orderSummary = this.cartItems.map(item => ({
      productName: item.product.name,
      quantity: item.quantity,
      subtotal: item.product.price * item.quantity
    }));
    this.totalSnapshot = this.total;
    this.shippingCostSnapshot = this.shippingCost;

    // Construir el body que espera el backend (CreateOrderRequest)
    const shipping = this.shippingForm.value;
    const requestBody = {
      firstName:  shipping.firstName,
      lastName:   shipping.lastName,
      email:      shipping.email,
      phone:      shipping.phone,
      address:    shipping.address,
      city:       shipping.city,
      postalCode: shipping.postalCode,
      province:   shipping.province,
      // Convertir cada CartItem al formato {productId, quantity} del backend
      items: this.cartItems.map(item => ({
        productId: item.product.id,
        quantity:  item.quantity
      }))
    };

    this.http.post<any>('http://localhost:8080/api/orders', requestBody).subscribe({
      next: (pedido) => {
        this.isProcessing = false;
        this.orderCompleted = true;
        this.orderNumber = pedido.orderNumber;  // Número real generado por el backend
        this.cartService.clearCart();
      },
      error: (err) => {
        this.isProcessing = false;
        this.errorMessage = err.error?.message || 'Error al procesar el pedido. Inténtalo de nuevo.';
      }
    });
  }

  formatCardNumber(value: string): string {
    return value.replace(/(.{4})/g, '$1 ').trim();
  }

  goToHome():     void { this.router.navigate(['/home']); }
  goToProducts(): void { this.router.navigate(['/products']); }
  goToCart():     void { this.router.navigate(['/cart']); }
}
