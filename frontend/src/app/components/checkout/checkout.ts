// ---- COMPONENTE CHECKOUT ----
// Proceso de compra en 3 pasos:
//   Paso 1: Datos de envío
//   Paso 2: Pago con Stripe (modo test — no se cobra dinero real)
//   Paso 3: Revisar y confirmar
//
// Integración Stripe:
//   1. Al entrar al paso 2, el backend crea un PaymentIntent y devuelve un clientSecret
//   2. Stripe.js muestra el formulario seguro de tarjeta (Card Element)
//   3. Al confirmar, stripe.confirmCardPayment() valida la tarjeta con Stripe
//   4. Si Stripe lo aprueba, se crea el pedido en nuestro backend
//
// Tarjeta de test: 4242 4242 4242 4242 | Cualquier fecha futura | Cualquier CVV

import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { HttpClient } from '@angular/common/http';
import { CartItem } from '../../models/cart-item.model';
import { CartService } from '../../services/cart';
import { loadStripe, Stripe, StripeCardElement } from '@stripe/stripe-js';
import jsPDF from 'jspdf';

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

  readonly API = 'http://localhost:8080/api';

  // ---- PERSISTENCIA DEL CHECKOUT ----
  // Claves en sessionStorage para que recargar la página no haga perder el progreso.
  // sessionStorage = se borra al cerrar la pestaña; perfecto para un checkout temporal.
  private readonly STEP_KEY = 'irenewhub_checkout_step';
  private readonly FORM_KEY = 'irenewhub_checkout_form';

  currentStep: number = 1;
  totalSteps: number = 3;

  shippingForm!: FormGroup;

  cartItems: CartItem[] = [];
  subtotal: number = 0;
  shippingCost: number = 0;
  total: number = 0;

  orderSummary: OrderSummaryItem[] = [];
  totalSnapshot: number = 0;
  shippingCostSnapshot: number = 0;
  couponSnapshot: { code: string; discountPercent: number } | null = null;
  discountSnapshot: number = 0;

  isProcessing: boolean = false;
  orderCompleted: boolean = false;
  orderNumber: string = '';
  errorMessage: string = '';

  // ---- STRIPE ----
  private stripe: Stripe | null = null;
  private cardElement: StripeCardElement | null = null;
  private clientSecret: string = '';
  private paymentMethodId: string = '';  // se guarda antes de salir del paso 2
  stripeError: string = '';
  stripeReady: boolean = false;
  isSavingCard: boolean = false;  // spinner mientras se valida la tarjeta

  get appliedCoupon() { return this.cartService.appliedCoupon; }

  get discountAmount(): number {
    if (!this.appliedCoupon) return 0;
    const base = this.subtotal + this.shippingCost;
    return Math.round(base * this.appliedCoupon.discountPercent) / 100;
  }

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
      const base = this.subtotal + this.shippingCost;
      const coupon = this.cartService.appliedCoupon;
      this.total = coupon ? base - (base * coupon.discountPercent / 100) : base;
    });

    if (this.cartItems.length === 0) {
      this.router.navigate(['/cart']);
      return;
    }

    this.initShippingForm();

    // ---- RESTAURAR ESTADO DESDE SESSIONSTORAGE ----
    // Si el usuario recarga la página, recuperamos los datos del formulario y
    // el paso en el que estaba para no obligarle a empezar de cero.
    this.restaurarEstado();

    // Cada vez que cambia el formulario, lo guardamos para sobrevivir a recargas
    this.shippingForm.valueChanges.subscribe(valores => {
      sessionStorage.setItem(this.FORM_KEY, JSON.stringify(valores));
    });
  }

  // ---- RESTAURAR ESTADO TRAS RECARGA ----
  // 1. Recuperamos los valores del formulario de envío (paso 1)
  // 2. Recuperamos el paso en el que estaba el usuario
  // 3. Si estaba en paso 2 o 3, lo dejamos en paso 2 (porque al recargar Stripe
  //    pierde su estado y hay que volver a montar el formulario de tarjeta).
  private restaurarEstado(): void {
    // Restaurar valores del formulario
    const formGuardado = sessionStorage.getItem(this.FORM_KEY);
    if (formGuardado) {
      try {
        this.shippingForm.patchValue(JSON.parse(formGuardado));
      } catch {
        // Si el JSON está corrupto, ignoramos y empezamos limpio
      }
    }

    // Restaurar paso (solo si el formulario es válido — si no, queda en paso 1)
    const pasoGuardado = sessionStorage.getItem(this.STEP_KEY);
    if (pasoGuardado && this.shippingForm.valid) {
      const paso = parseInt(pasoGuardado, 10);
      if (paso === 2 || paso === 3) {
        // Volvemos al paso 2 incluso si estaba en el 3: Stripe necesita
        // re-montar el card element porque su estado vive solo en memoria.
        this.currentStep = 2;
        setTimeout(() => this.iniciarStripe(), 200);
      }
    }
  }

  initShippingForm(): void {
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

  // ---- NAVEGACIÓN ENTRE PASOS ----
  nextStep(): void {
    if (this.currentStep === 1) {
      if (this.shippingForm.invalid) {
        Object.keys(this.shippingForm.controls).forEach(k =>
          this.shippingForm.get(k)?.markAsTouched()
        );
        return;
      }
      this.currentStep = 2;
      sessionStorage.setItem(this.STEP_KEY, '2');
      // Al entrar al paso 2: obtener clave Stripe + crear PaymentIntent
      setTimeout(() => this.iniciarStripe(), 100);

    } else if (this.currentStep === 2) {
      // Antes de salir del paso 2, guardamos la tarjeta como PaymentMethod ID
      // (el card element se destruye cuando Angular oculta el paso 2 con *ngIf)
      this.guardarTarjeta();
      return;  // guardarTarjeta() avanza a paso 3 internamente si todo va bien

    } else if (this.currentStep === 3) {
      this.confirmarPago();
    }
    window.scrollTo(0, 0);
  }

  prevStep(): void {
    if (this.currentStep > 1) {
      this.currentStep--;
      sessionStorage.setItem(this.STEP_KEY, this.currentStep.toString());
      window.scrollTo(0, 0);
    }
  }

  // ---- GUARDAR TARJETA (paso 2 → 3) ----
  // Stripe convierte los datos de la tarjeta en un PaymentMethod ID (token).
  // Así podemos destruir el Card Element del DOM sin perder los datos del pago.

  async guardarTarjeta(): Promise<void> {
    if (!this.stripe || !this.cardElement) return;
    this.stripeError = '';
    this.isSavingCard = true;

    const s = this.shippingForm.value;
    const { paymentMethod, error } = await this.stripe.createPaymentMethod({
      type: 'card',
      card: this.cardElement,
      billing_details: {
        name:  `${s.firstName} ${s.lastName}`,
        email: s.email,
        phone: s.phone
      }
    });

    this.isSavingCard = false;

    if (error) {
      this.stripeError = error.message || 'Datos de tarjeta no válidos.';
      return;
    }

    this.paymentMethodId = paymentMethod!.id;
    this.currentStep = 3;
    sessionStorage.setItem(this.STEP_KEY, '3');
    window.scrollTo(0, 0);
  }

  // ---- INICIAR STRIPE ----
  // 1. Obtiene la clave pública del backend
  // 2. Crea un PaymentIntent en Stripe (devuelve clientSecret)
  // 3. Monta el Card Element en el div #card-element

  async iniciarStripe(): Promise<void> {
    this.stripeError = '';
    this.stripeReady = false;

    try {
      // Paso A: obtener la clave pública desde el backend
      const config = await this.http
        .get<{ publishableKey: string }>(`${this.API}/payment/config`)
        .toPromise();

      this.stripe = await loadStripe(config!.publishableKey);
      if (!this.stripe) throw new Error('No se pudo cargar Stripe');

      // Paso B: crear un PaymentIntent en el backend
      const intentRes = await this.http
        .post<{ clientSecret: string }>(`${this.API}/payment/create-intent`, {
          amount: this.total.toFixed(2)
        })
        .toPromise();

      this.clientSecret = intentRes!.clientSecret;

      // Paso C: montar el formulario de tarjeta de Stripe
      // disableLink: true → desactiva Stripe Link para que no pida datos reales
      const elements = this.stripe.elements({ locale: 'es' });
      this.cardElement = elements.create('card', {
        disableLink: true,
        hidePostalCode: true,
        style: {
          base: {
            fontSize: '16px',
            fontFamily: '-apple-system, BlinkMacSystemFont, "Segoe UI", sans-serif',
            color: '#1d1d1f',
            '::placeholder': { color: '#86868b' }
          },
          invalid: { color: '#ff3b30' }
        }
      });

      this.cardElement.mount('#card-element');
      this.cardElement.on('ready', () => { this.stripeReady = true; });

    } catch (err: any) {
      this.stripeError = 'Error al conectar con el sistema de pago. Inténtalo de nuevo.';
      console.error('Stripe init error:', err);
    }
  }

  // ---- CONFIRMAR PAGO ----
  // 1. Stripe valida la tarjeta con su servidor
  // 2. Si OK → creamos el pedido en nuestro backend
  // 3. Si KO → mostramos el error de Stripe

  async confirmarPago(): Promise<void> {
    if (!this.stripe || !this.paymentMethodId) {
      this.errorMessage = 'Error: vuelve al paso anterior e introduce los datos de la tarjeta.';
      return;
    }

    this.isProcessing = true;
    this.errorMessage = '';

    const shipping = this.shippingForm.value;

    // Snapshot antes de vaciar el carrito
    this.orderSummary = this.cartItems.map(item => ({
      productName: item.product.name,
      quantity:    item.quantity,
      subtotal:    item.product.price * item.quantity
    }));
    this.totalSnapshot        = this.total;
    this.shippingCostSnapshot = this.shippingCost;
    this.couponSnapshot       = this.cartService.appliedCoupon
      ? { ...this.cartService.appliedCoupon } : null;
    this.discountSnapshot     = this.discountAmount;

    // Paso 1: confirmar el pago con Stripe usando el PaymentMethod ID guardado
    // (el card element ya no está en el DOM, pero el ID tiene los datos tokenizados)
    const { error } = await this.stripe.confirmCardPayment(this.clientSecret, {
      payment_method: this.paymentMethodId
    });

    if (error) {
      this.isProcessing = false;
      this.errorMessage = error.message || 'Error en el pago. Comprueba los datos de tu tarjeta.';
      return;
    }

    // Paso 2: Stripe aprobó el pago → crear el pedido en nuestro backend
    const requestBody: any = {
      firstName:  shipping.firstName,
      lastName:   shipping.lastName,
      email:      shipping.email,
      phone:      shipping.phone,
      address:    shipping.address,
      city:       shipping.city,
      postalCode: shipping.postalCode,
      province:   shipping.province,
      items: this.cartItems.map(item => ({
        productId: item.product.id,
        quantity:  item.quantity
      }))
    };

    if (this.cartService.appliedCoupon) {
      requestBody.couponCode = this.cartService.appliedCoupon.code;
    }

    this.http.post<any>(`${this.API}/orders`, requestBody).subscribe({
      next: (pedido) => {
        this.isProcessing   = false;
        this.orderCompleted = true;
        this.orderNumber    = pedido.orderNumber;
        this.cartService.clearCart();
        // Limpiar estado persistido — el checkout ha terminado correctamente
        sessionStorage.removeItem(this.STEP_KEY);
        sessionStorage.removeItem(this.FORM_KEY);
      },
      error: (err) => {
        this.isProcessing = false;
        this.errorMessage = err.error?.message || 'Error al registrar el pedido.';
      }
    });
  }

  // ---- GENERAR TICKET PDF ----
  // Carga el logo como base64 usando canvas, que es el formato que acepta jsPDF
  private cargarLogo(): Promise<string> {
    return new Promise((resolve) => {
      const img = new Image();
      img.onload = () => {
        const canvas = document.createElement('canvas');
        canvas.width = img.width;
        canvas.height = img.height;
        canvas.getContext('2d')!.drawImage(img, 0, 0);
        resolve(canvas.toDataURL('image/png'));
      };
      img.onerror = () => resolve(''); // Si no carga el logo, el PDF sigue sin él
      img.src = 'assets/irenewhub_logo.png';
    });
  }

  async descargarTicketPDF(): Promise<void> {
    const logo = await this.cargarLogo();
    const doc = new jsPDF({ unit: 'mm', format: 'a4' });
    const pageW = 210;
    const margin = 20;
    const contentW = pageW - margin * 2;
    let y = 0;

    const fmt = (n: number) =>
      n.toLocaleString('es-ES', { minimumFractionDigits: 2, maximumFractionDigits: 2 }) + ' €';

    doc.setFillColor(0, 113, 227);
    doc.rect(0, 0, pageW, 40, 'F');
    // Logo al lado del nombre si se cargó correctamente
    if (logo) doc.addImage(logo, 'PNG', margin, 6, 22, 22);
    doc.setTextColor(255, 255, 255);
    doc.setFontSize(24);
    doc.setFont('helvetica', 'bold');
    doc.text('iRenewHub', margin + 26, 18);
    doc.setFontSize(11);
    doc.setFont('helvetica', 'normal');
    doc.text('Repuestos originales para iPhone', margin + 26, 27);
    doc.setFontSize(13);
    doc.setFont('helvetica', 'bold');
    doc.text('TICKET DE COMPRA', pageW - margin, 22, { align: 'right' });

    y = 50;
    doc.setTextColor(30, 30, 30);
    doc.setFontSize(13);
    doc.setFont('helvetica', 'bold');
    doc.text(`Pedido: ${this.orderNumber}`, margin, y);
    const fecha = new Date().toLocaleDateString('es-ES', { day: '2-digit', month: 'long', year: 'numeric' });
    doc.setFontSize(10);
    doc.setFont('helvetica', 'normal');
    doc.setTextColor(100, 100, 100);
    doc.text(fecha, pageW - margin, y, { align: 'right' });

    y += 8;
    doc.setDrawColor(220, 220, 220);
    doc.setLineWidth(0.4);
    doc.line(margin, y, pageW - margin, y);

    y += 10;
    const s = this.shippingForm.value;
    doc.setTextColor(0, 113, 227);
    doc.setFontSize(10);
    doc.setFont('helvetica', 'bold');
    doc.text('DATOS DE ENVÍO', margin, y);
    y += 7;
    doc.setTextColor(50, 50, 50);
    doc.setFont('helvetica', 'normal');
    doc.setFontSize(10);
    doc.text(`${s.firstName} ${s.lastName}`, margin, y);         y += 6;
    doc.text(s.address, margin, y);                               y += 6;
    doc.text(`${s.postalCode} ${s.city}, ${s.province}`, margin, y); y += 6;
    doc.text(s.email, margin, y);                                 y += 6;
    doc.text(s.phone, margin, y);

    y += 12;
    doc.setDrawColor(220, 220, 220);
    doc.line(margin, y, pageW - margin, y);
    y += 8;

    doc.setFillColor(245, 245, 247);
    doc.rect(margin, y - 5, contentW, 9, 'F');
    doc.setTextColor(80, 80, 80);
    doc.setFontSize(9);
    doc.setFont('helvetica', 'bold');
    doc.text('PRODUCTO', margin + 2, y);
    doc.text('CANT.', margin + 100, y, { align: 'center' });
    doc.text('SUBTOTAL', pageW - margin - 2, y, { align: 'right' });

    y += 7;
    doc.setDrawColor(200, 200, 200);
    doc.line(margin, y, pageW - margin, y);

    doc.setFont('helvetica', 'normal');
    doc.setFontSize(10);
    for (const item of this.orderSummary) {
      y += 8;
      doc.setTextColor(30, 30, 30);
      const nombre = item.productName.length > 45 ? item.productName.substring(0, 42) + '...' : item.productName;
      doc.text(nombre, margin + 2, y);
      doc.text(String(item.quantity), margin + 100, y, { align: 'center' });
      doc.setFont('helvetica', 'bold');
      doc.text(fmt(item.subtotal), pageW - margin - 2, y, { align: 'right' });
      doc.setFont('helvetica', 'normal');
      doc.setDrawColor(235, 235, 235);
      doc.line(margin, y + 3, pageW - margin, y + 3);
      y += 3;
    }

    y += 10;
    const totalColX = pageW - margin - 60;

    const lineaTotales = (label: string, valor: string, bold = false) => {
      doc.setFont('helvetica', bold ? 'bold' : 'normal');
      doc.setFontSize(bold ? 11 : 10);
      doc.setTextColor(bold ? 30 : 90, bold ? 30 : 90, bold ? 30 : 90);
      doc.text(label, totalColX, y);
      doc.text(valor, pageW - margin - 2, y, { align: 'right' });
      y += 7;
    };

    lineaTotales('Subtotal:', fmt(this.orderSummary.reduce((a, i) => a + i.subtotal, 0)));
    lineaTotales('Envío:', this.shippingCostSnapshot === 0 ? 'GRATIS' : fmt(this.shippingCostSnapshot));

    if (this.couponSnapshot) {
      doc.setTextColor(52, 199, 89);
      doc.setFont('helvetica', 'normal');
      doc.setFontSize(10);
      doc.text(`Cupón ${this.couponSnapshot.code} (${this.couponSnapshot.discountPercent}%):`, totalColX, y);
      doc.text(`-${fmt(this.discountSnapshot)}`, pageW - margin - 2, y, { align: 'right' });
      y += 7;
    }

    doc.setDrawColor(180, 180, 180);
    doc.setLineWidth(0.5);
    doc.line(totalColX, y, pageW - margin, y);
    y += 6;

    doc.setFillColor(0, 113, 227);
    doc.roundedRect(totalColX - 2, y - 5, pageW - margin - totalColX + 4, 10, 2, 2, 'F');
    doc.setTextColor(255, 255, 255);
    doc.setFont('helvetica', 'bold');
    doc.setFontSize(12);
    doc.text('TOTAL:', totalColX + 2, y + 1);
    doc.text(fmt(this.totalSnapshot), pageW - margin - 2, y + 1, { align: 'right' });

    y += 20;
    doc.setDrawColor(220, 220, 220);
    doc.setLineWidth(0.3);
    doc.line(margin, y, pageW - margin, y);
    y += 8;
    doc.setTextColor(130, 130, 130);
    doc.setFont('helvetica', 'normal');
    doc.setFontSize(9);
    doc.text('¡Gracias por tu compra en iRenewHub!', pageW / 2, y, { align: 'center' });
    y += 6;
    doc.text('Para cualquier consulta: soporte@irenewhub.com', pageW / 2, y, { align: 'center' });
    y += 6;
    doc.text('Pago procesado de forma segura por Stripe · Devoluciones gratuitas en 30 días', pageW / 2, y, { align: 'center' });

    doc.save(`ticket-${this.orderNumber}.pdf`);
  }

  goToHome():     void { this.router.navigate(['/home']); }
  goToProducts(): void { this.router.navigate(['/products']); }
  goToCart():     void { this.router.navigate(['/cart']); }
}
