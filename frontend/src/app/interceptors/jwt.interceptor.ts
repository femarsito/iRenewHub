// ─── INTERCEPTOR JWT ─────────────────────────────────────────────────────────
// Se ejecuta automáticamente en CADA petición HTTP que hace la aplicación.
// Su único trabajo: si hay un JWT en localStorage, añadirlo como cabecera
// "Authorization: Bearer <token>" para que el backend pueda identificar al usuario.
//
// Está registrado en AppModule con HTTP_INTERCEPTORS, multi: true.
// Angular lo llama de forma transparente; los servicios y componentes
// no tienen que preocuparse de añadir el token manualmente.

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

    // Clonar la petición añadiendo el header de autorización
    // (las peticiones HTTP son inmutables en Angular, hay que clonarlas para modificarlas)
    const peticionConToken = req.clone({
      setHeaders: { Authorization: `Bearer ${token}` }
    });

    return next.handle(peticionConToken);
  }
}
