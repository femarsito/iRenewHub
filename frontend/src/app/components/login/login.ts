// ─── COMPONENTE LOGIN ─────────────────────────────────────────────────────────
// Página de autenticación con tres modos, controlados por la variable "modo":
//   'login'    → formulario de inicio de sesión
//   'register' → formulario de registro de nueva cuenta
//   'forgot'   → flujo de recuperación de contraseña (2 pasos)
//
// Flujo de recuperación (DEMO):
//   Paso 1: el usuario introduce su email → el backend devuelve un token
//   Paso 2: el usuario introduce ese token y su nueva contraseña

import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../../services/auth';

@Component({
  selector: 'app-login',
  standalone: false,
  templateUrl: './login.html',
  styleUrl: './login.css'
})
export class Login implements OnInit {

  // Controla qué formulario se muestra: 'login' | 'register' | 'forgot'
  modo: string = 'login';

  // En el flujo de "olvidé contraseña", guardamos el token recibido del backend
  tokenRecuperacion: string = '';
  forgotPaso2: boolean = false;  // true = mostrar paso 2 (token + nueva contraseña)

  loginForm!: FormGroup;
  registerForm!: FormGroup;
  forgotForm!: FormGroup;   // Paso 1: solo email
  resetForm!: FormGroup;    // Paso 2: token + nueva contraseña

  isLoading: boolean = false;
  errorMessage: string = '';
  successMessage: string = '';

  constructor(
    private formBuilder: FormBuilder,
    private authService: AuthService,
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

    this.resetForm = this.formBuilder.group({
      token:       ['', [Validators.required]],
      newPassword: ['', [Validators.required, Validators.minLength(6)]]
    });
  }

  cambiarModo(nuevoModo: string): void {
    this.modo = nuevoModo;
    this.errorMessage = '';
    this.successMessage = '';
    this.forgotPaso2 = false;
    this.tokenRecuperacion = '';
  }

  // ─── GETTERS ──────────────────────────────────────────────────────────────
  get loginEmail()    { return this.loginForm.get('email'); }
  get loginPassword() { return this.loginForm.get('password'); }
  get regFirstName()  { return this.registerForm.get('firstName'); }
  get regLastName()   { return this.registerForm.get('lastName'); }
  get regEmail()      { return this.registerForm.get('email'); }
  get regPassword()   { return this.registerForm.get('password'); }
  get forgotEmail()   { return this.forgotForm.get('email'); }
  get resetToken()    { return this.resetForm.get('token'); }
  get resetNewPass()  { return this.resetForm.get('newPassword'); }

  // ─── LOGIN ────────────────────────────────────────────────────────────────
  onLogin(): void {
    if (this.loginForm.invalid) {
      Object.keys(this.loginForm.controls).forEach(k => this.loginForm.get(k)?.markAsTouched());
      return;
    }
    this.isLoading = true;
    this.errorMessage = '';
    const { email, password } = this.loginForm.value;
    this.authService.login(email, password).subscribe({
      // tap() ya guardó el rol en localStorage antes de llegar aquí
      next: () => this.router.navigate(this.authService.isAdmin() ? ['/admin'] : ['/home']),
      error: (err) => {
        this.isLoading = false;
        this.errorMessage = err.status === 401
          ? 'Email o contraseña incorrectos.'
          : 'Error al iniciar sesión. Inténtalo de nuevo.';
      }
    });
  }

  // ─── REGISTRO ─────────────────────────────────────────────────────────────
  onRegister(): void {
    if (this.registerForm.invalid) {
      Object.keys(this.registerForm.controls).forEach(k => this.registerForm.get(k)?.markAsTouched());
      return;
    }
    this.isLoading = true;
    this.errorMessage = '';
    const { firstName, lastName, email, password } = this.registerForm.value;
    this.authService.register(firstName, lastName, email, password).subscribe({
      next: () => this.router.navigate(['/home']),  // Los nuevos usuarios siempre son USER
      error: (err) => {
        this.isLoading = false;
        this.errorMessage = err.status === 409
          ? 'Ya existe una cuenta con ese email.'
          : 'Error al registrarse. Inténtalo de nuevo.';
      }
    });
  }

  // ─── RECUPERAR CONTRASEÑA: PASO 1 ─────────────────────────────────────────
  onForgotPassword(): void {
    if (this.forgotForm.invalid) {
      this.forgotForm.get('email')?.markAsTouched();
      return;
    }
    this.isLoading = true;
    this.errorMessage = '';
    const { email } = this.forgotForm.value;
    this.authService.forgotPassword(email).subscribe({
      next: (respuesta) => {
        this.isLoading = false;
        this.tokenRecuperacion = respuesta.token;
        this.forgotPaso2 = true;
        this.resetForm.patchValue({ token: respuesta.token });
      },
      error: () => {
        this.isLoading = false;
        this.errorMessage = 'No existe cuenta con ese email.';
      }
    });
  }

  // ─── RECUPERAR CONTRASEÑA: PASO 2 ─────────────────────────────────────────
  onResetPassword(): void {
    if (this.resetForm.invalid) {
      Object.keys(this.resetForm.controls).forEach(k => this.resetForm.get(k)?.markAsTouched());
      return;
    }
    this.isLoading = true;
    this.errorMessage = '';
    const { token, newPassword } = this.resetForm.value;
    this.authService.resetPassword(token, newPassword).subscribe({
      next: () => {
        this.isLoading = false;
        this.successMessage = '¡Contraseña actualizada! Ya puedes iniciar sesión.';
        this.forgotPaso2 = false;
        this.modo = 'login';
      },
      error: (err) => {
        this.isLoading = false;
        this.errorMessage = err.error?.message || 'Token inválido o expirado. Solicita uno nuevo.';
      }
    });
  }
}
