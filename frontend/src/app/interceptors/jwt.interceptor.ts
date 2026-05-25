// Se ejecuta en cada petición HTTP. Si hay JWT en sessionStorage lo añade como
// cabecera Authorization: Bearer <token>. Registrado en AppModule con HTTP_INTERCEPTORS.

import { Injectable } from '@angular/core';
import { HttpInterceptor, HttpRequest, HttpHandler } from '@angular/common/http';
import { AuthService } from '../services/auth';

@Injectable()
export class JwtInterceptor implements HttpInterceptor {

  constructor(private authService: AuthService) { }

  intercept(req: HttpRequest<any>, next: HttpHandler) {
    const token = this.authService.getToken();

    // Si no hay token (usuario no autenticado), la petición pasa sin modificar
    if (!token) {
      return next.handle(req);
    }

    // Las peticiones HTTP son inmutables en Angular, hay que clonarlas para modificarlas
    const peticionConToken = req.clone({
      setHeaders: { Authorization: `Bearer ${token}` }
    });

    return next.handle(peticionConToken);
  }
}
