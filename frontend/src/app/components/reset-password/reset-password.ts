// ---- COMPONENTE RESET PASSWORD ----
// El usuario llega aquí desde el link del email:
//   http://localhost:4200/reset-password?token=abc123...
//
// Este componente:
//   1. Lee el token de la URL (ActivatedRoute)
//   2. Muestra el formulario de nueva contraseña
//   3. Llama al backend POST /api/auth/reset-password
//   4. Redirige al login si todo va bien

import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { HttpClient } from '@angular/common/http';

@Component({
  selector: 'app-reset-password',
  standalone: false,
  templateUrl: './reset-password.html',
  styleUrl:    './reset-password.css'
})
export class ResetPassword implements OnInit {

  readonly API = 'http://localhost:8080/api';

  form!: FormGroup;
  token: string = '';

  isLoading   = false;
  errorMsg    = '';
  successMsg  = '';
  tokenInvalid = false;  // true si el token no está en la URL

  constructor(
    private fb:    FormBuilder,
    private route: ActivatedRoute,
    private router: Router,
    private http:  HttpClient
  ) {}

  ngOnInit(): void {
    // Leer el token del parámetro de la URL: ?token=xxx
    this.token = this.route.snapshot.queryParamMap.get('token') ?? '';

    if (!this.token) {
      this.tokenInvalid = true;
      return;
    }

    this.form = this.fb.group({
      newPassword:     ['', [Validators.required, Validators.minLength(6)]],
      confirmPassword: ['', [Validators.required]]
    }, { validators: this.passwordsCoinciden });
  }

  // Validador personalizado: comprueba que las dos contraseñas son iguales
  passwordsCoinciden(group: FormGroup) {
    const pass    = group.get('newPassword')?.value;
    const confirm = group.get('confirmPassword')?.value;
    return pass === confirm ? null : { noCoinciden: true };
  }

  get newPassword()     { return this.form.get('newPassword'); }
  get confirmPassword() { return this.form.get('confirmPassword'); }

  onSubmit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.isLoading = true;
    this.errorMsg  = '';

    this.http.post<any>(`${this.API}/auth/reset-password`, {
      token:       this.token,
      newPassword: this.form.value.newPassword
    }).subscribe({
      next: () => {
        this.isLoading  = false;
        this.successMsg = '¡Contraseña actualizada! Redirigiendo al inicio de sesión...';
        setTimeout(() => this.router.navigate(['/login']), 2500);
      },
      error: (err) => {
        this.isLoading = false;
        this.errorMsg  = err.error?.message || 'El enlace no es válido o ha expirado. Solicita uno nuevo.';
      }
    });
  }
}
