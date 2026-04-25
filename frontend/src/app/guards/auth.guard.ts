// ─── GUARDS DE AUTENTICACIÓN ──────────────────────────────────────────────────
// Los guards se ejecutan ANTES de que Angular muestre la ruta.
// Si el guard devuelve false, la navegación se cancela y se redirige.
//
// AuthGuard  → cualquier ruta privada: redirige a /login si no hay sesión.
// AdminGuard → rutas exclusivas de admin: redirige a /products si no es ADMIN.

import { Injectable } from '@angular/core';
import { CanActivate, Router } from '@angular/router';
import { AuthService } from '../services/auth';

// ─── AUTH GUARD ───────────────────────────────────────────────────────────────
@Injectable({ providedIn: 'root' })
export class AuthGuard implements CanActivate {

  constructor(private authService: AuthService, private router: Router) {}

  canActivate(): boolean {
    if (this.authService.isLoggedIn()) return true;
    // Sin JWT → al login
    this.router.navigate(['/login']);
    return false;
  }
}

// ─── ADMIN GUARD ──────────────────────────────────────────────────────────────
@Injectable({ providedIn: 'root' })
export class AdminGuard implements CanActivate {

  constructor(private authService: AuthService, private router: Router) {}

  canActivate(): boolean {
    if (this.authService.isAdmin()) return true;
    // No es admin → al catálogo de usuario normal
    this.router.navigate(['/products']);
    return false;
  }
}
