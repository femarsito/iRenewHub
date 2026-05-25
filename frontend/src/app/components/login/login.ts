// ---- COMPONENTE LOGIN ----
// Página de autenticación con tres modos, controlados por la variable "modo":
//   'login'    → formulario de inicio de sesión
//   'register' → formulario de registro de nueva cuenta
//   'forgot'   → recuperación de contraseña por email
//
// Flujo de recuperación de contraseña:
//   1. El usuario introduce su email y hace clic en "Enviar enlace"
//   2. El backend genera un token UUID, lo guarda en la BD y envía un email
//   3. El email contiene un link: /reset-password?token=xxx
//   4. El componente ResetPassword gestiona el cambio de contraseña

import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../../services/auth';
import { CartService } from '../../services/cart';

@Component({
  selector: 'app-login',
  standalone: false,
  templateUrl: './login.html',
  styleUrl: './login.css'
})
export class Login implements OnInit {

  // Controla qué formulario se muestra: 'login' | 'register' | 'forgot'
  modo: string = 'login';

  // En el flujo de "olvidé contraseña": true cuando el email ya fue enviado
  emailEnviado: boolean = false;

  loginForm!: FormGroup;
  registerForm!: FormGroup;
  forgotForm!: FormGroup;   // Solo pide el email

  isLoading: boolean = false;
  errorMessage: string = '';
  successMessage: string = '';

  constructor(
    private formBuilder: FormBuilder,
    private authService: AuthService,
    private cartService: CartService,
    private router: Router
  ) { }

  ngOnInit(): void {
    // Si ya hay sesión activa, redirigir según el rol
    if (this.authService.isLoggedIn()) {
      this.router.navigate(this.authService.isAdmin() ? ['/admin'] : ['/home']);
      return;
    }

    this.loginForm = this.formBuilder.group({
      email:    ['', [Validators.required, Validators.email]],
      password: ['', [Validators.required, Validators.minLength(6)]]
    });

    this.registerForm = this.formBuilder.group({
      firstName: ['', [Validators.required, Validators.minLength(2)]],
      lastName:  ['', [Validators.required, Validators.minLength(2)]],
      email:     ['', [Validators.required, Validators.email]],
      password:  ['', [Validators.required, Validators.minLength(6)]]
    });

    this.forgotForm = this.formBuilder.group({
      email: ['', [Validators.required, Validators.email]]
    });
  }

  cambiarModo(nuevoModo: string): void {
    this.modo = nuevoModo;
    this.errorMessage = '';
    this.successMessage = '';
    this.emailEnviado = false;
  }

  // ---- GETTERS ----
  get loginEmail()    { return this.loginForm.get('email'); }
  get loginPassword() { return this.loginForm.get('password'); }
  get regFirstName()  { return this.registerForm.get('firstName'); }
  get regLastName()   { return this.registerForm.get('lastName'); }
  get regEmail()      { return this.registerForm.get('email'); }
  get regPassword()   { return this.registerForm.get('password'); }
  get forgotEmail()   { return this.forgotForm.get('email'); }

  // ---- LOGIN ----
  onLogin(): void {
    if (this.loginForm.invalid) {
      Object.keys(this.loginForm.controls).forEach(k => this.loginForm.get(k)?.markAsTouched());
      return;
    }
    this.isLoading = true;
    this.errorMessage = '';
    const { email, password } = this.loginForm.value;
    this.authService.login(email, password).subscribe({
      // tap() ya guardó el rol en sessionStorage — ahora cargamos el carrito de este usuario
      next: () => {
        this.cartService.recargarParaUsuario();
        this.router.navigate(this.authService.isAdmin() ? ['/admin'] : ['/home']);
      },
      error: (err) => {
        this.isLoading = false;
        this.errorMessage = err.status === 401
          ? 'Email o contraseña incorrectos.'
          : 'Error al iniciar sesión. Inténtalo de nuevo.';
      }
    });
  }

  // ---- REGISTRO ----
  onRegister(): void {
    if (this.registerForm.invalid) {
      Object.keys(this.registerForm.controls).forEach(k => this.registerForm.get(k)?.markAsTouched());
      return;
    }
    this.isLoading = true;
    this.errorMessage = '';
    const { firstName, lastName, email, password } = this.registerForm.value;
    this.authService.register(firstName, lastName, email, password).subscribe({
      next: () => {
        this.cartService.recargarParaUsuario();
        this.router.navigate(['/home']);
      },
      error: (err) => {
        this.isLoading = false;
        this.errorMessage = err.status === 409
          ? 'Ya existe una cuenta con ese email.'
          : 'Error al registrarse. Inténtalo de nuevo.';
      }
    });
  }

  // ---- RECUPERAR CONTRASEÑA ----
  // Envía el email con el enlace. El cambio de contraseña lo gestiona
  // el componente ResetPassword en la ruta /reset-password?token=xxx
  onForgotPassword(): void {
    if (this.forgotForm.invalid) {
      this.forgotForm.get('email')?.markAsTouched();
      return;
    }
    this.isLoading = true;
    this.errorMessage = '';
    const { email } = this.forgotForm.value;
    this.authService.forgotPassword(email).subscribe({
      next: () => {
        this.isLoading = false;
        this.emailEnviado = true;  // Mostrar confirmación "Revisa tu bandeja"
      },
      error: (err) => {
        this.isLoading = false;
        this.errorMessage = err.error?.message || err.message || 'Error al enviar el email. Inténtalo de nuevo.';
      }
    });
  }
}
