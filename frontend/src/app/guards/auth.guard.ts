// Los guards se ejecutan antes de mostrar la ruta. Si devuelven false, redirigen.
// AuthGuard: redirige a /login si no hay sesión. AdminGuard: redirige a /products si no es ADMIN.

import { Injectable } from '@angular/core';
import { CanActivate, Router } from '@angular/router';
import { AuthService } from '../services/auth';

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
