import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router } from '@angular/router';
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

  // Control de pasos del proceso de pago
  currentStep: number = 1;
  totalSteps: number = 3;

  // Formularios
  shippingForm!: FormGroup;
  paymentForm!: FormGroup;

  // Estado del carrito
  cartItems: CartItem[] = [];
  subtotal: number = 0;
  shippingCost: number = 0;
  total: number = 0;

  // Snapshot del pedido para mostrar después de completar
  orderSummary: OrderSummaryItem[] = [];      // ← AÑADIR
  totalSnapshot: number = 0;                   // ← AÑADIR
  shippingCostSnapshot: number = 0;            // ← AÑADIR

  // Estado del proceso
  isProcessing: boolean = false;
  orderCompleted: boolean = false;
  orderNumber: string = '';

  constructor(
    private formBuilder: FormBuilder,
    private cartService: CartService,
    private router: Router
  ) { }

  ngOnInit(): void {
    // si el carrito está vacío redirige al carrito
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
    // formulario de envío
    this.shippingForm = this.formBuilder.group({
      firstName: ['', [Validators.required, Validators.minLength(2)]],
      lastName: ['', [Validators.required, Validators.minLength(2)]],
      email: ['', [Validators.required, Validators.email]],
      phone: ['', [Validators.required, Validators.pattern(/^[0-9]{9}$/)]],
      address: ['', [Validators.required, Validators.minLength(10)]],
      city: ['', Validators.required],
      postalCode: ['', [Validators.required, Validators.pattern(/^[0-9]{5}$/)]],
      province: ['', Validators.required]
    });

    // formulario de pago
    this.paymentForm = this.formBuilder.group({
      cardName: ['', [Validators.required, Validators.minLength(3)]],
      cardNumber: ['', [Validators.required, Validators.pattern(/^[0-9]{16}$/)]],
      expiryDate: ['', [Validators.required, Validators.pattern(/^(0[1-9]|1[0-2])\/([0-9]{2})$/)]],
      cvv: ['', [Validators.required, Validators.pattern(/^[0-9]{3,4}$/)]]
    });
  }

  // getters formulario de envío
  get firstName() { return this.shippingForm.get('firstName'); }
  get lastName() { return this.shippingForm.get('lastName'); }
  get email() { return this.shippingForm.get('email'); }
  get phone() { return this.shippingForm.get('phone'); }
  get address() { return this.shippingForm.get('address'); }
  get city() { return this.shippingForm.get('city'); }
  get postalCode() { return this.shippingForm.get('postalCode'); }
  get province() { return this.shippingForm.get('province'); }

  // getters formulario de pago
  get cardName() { return this.paymentForm.get('cardName'); }
  get cardNumber() { return this.paymentForm.get('cardNumber'); }
  get expiryDate() { return this.paymentForm.get('expiryDate'); }
  get cvv() { return this.paymentForm.get('cvv'); }

  // avanza al siguiente paso
  nextStep(): void {
    if (this.currentStep === 1) {
      if (this.shippingForm.invalid) {
        Object.keys(this.shippingForm.controls).forEach(key => {
          this.shippingForm.get(key)?.markAsTouched();
        });
        return;
      }
    }
    if (this.currentStep < this.totalSteps) {
      this.currentStep++;
      window.scrollTo(0, 0);
    }
  }

  // retrocede al paso anterior
  prevStep(): void {
    if (this.currentStep > 1) {
      this.currentStep--;
      window.scrollTo(0, 0);
    }
  }

  // confirmar el pago
  confirmPayment(): void {
    if (this.paymentForm.invalid) {
      Object.keys(this.paymentForm.controls).forEach(key => {
        this.paymentForm.get(key)?.markAsTouched();
      });
      return;
    }

    this.isProcessing = true;

    this.orderSummary = this.cartItems.map(item => ({
      productName: item.product.name,
      quantity: item.quantity,
      subtotal: item.product.price * item.quantity
    }));
    this.totalSnapshot = this.total;
    this.shippingCostSnapshot = this.shippingCost;

    // simula procesamiento del pago durante 3 segundos
    setTimeout(() => {
      this.isProcessing = false;
      this.orderCompleted = true;
      this.orderNumber = this.generateOrderNumber();
      
      // vacia el carrito despues de guardar el snapshot
      this.cartService.clearCart();
    }, 3000);
  }
  // genera un número de pedido aleatorio
  generateOrderNumber(): string {
    const timestamp = Date.now().toString().slice(-6);
    const random = Math.floor(Math.random() * 1000).toString().padStart(3, '0');
    return `IPH-${timestamp}-${random}`;
  }

  // formatea número de tarjeta para mostrar
  formatCardNumber(value: string): string {
    return value.replace(/(.{4})/g, '$1 ').trim();
  }

  goToHome(): void {
    this.router.navigate(['/home']);
  }

  goToProducts(): void {
    this.router.navigate(['/products']);
  }

  goToCart(): void {
    this.router.navigate(['/cart']);
  }
}