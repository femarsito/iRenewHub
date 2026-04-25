// ─── SERVICIO DE AUTENTICACIÓN ────────────────────────────────────────────────
// Gestiona el login, el registro, el estado de sesión y la recuperación de contraseña.
//
// ALMACENAMIENTO: se usa sessionStorage (NO localStorage) para que la sesión:
//   ✓ Sobreviva a F5 / recargas de página dentro de la misma pestaña
//   ✓ Se borre automáticamente al cerrar el navegador o la pestaña
//   ✗ No persista entre sesiones de navegador (por seguridad)

import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, tap } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class AuthService {

  private apiUrl = 'http://localhost:8080/api/auth';

  constructor(private http: HttpClient) { }

  // Envía las credenciales al backend y guarda JWT + datos del usuario en sessionStorage.
  login(email: string, password: string): Observable<any> {
    return this.http.post<any>(`${this.apiUrl}/login`, { email, password }).pipe(
      tap(r => {
        sessionStorage.setItem('jwt',       r.token);
        sessionStorage.setItem('role',      r.role);
        sessionStorage.setItem('firstName', r.firstName);
        sessionStorage.setItem('lastName',  r.lastName);
        sessionStorage.setItem('email',     r.email);
      })
    );
  }

  // Registra un nuevo usuario. El backend siempre asigna rol USER.
  register(firstName: string, lastName: string, email: string, password: string): Observable<any> {
    return this.http.post<any>(`${this.apiUrl}/register`, { firstName, lastName, email, password }).pipe(
      tap(r => {
        sessionStorage.setItem('jwt',       r.token);
        sessionStorage.setItem('role',      r.role);
        sessionStorage.setItem('firstName', r.firstName);
        sessionStorage.setItem('lastName',  r.lastName);
        sessionStorage.setItem('email',     r.email);
      })
    );
  }

  // Elimina todos los datos de sesión → cierra la sesión
  logout(): void {
    sessionStorage.removeItem('jwt');
    sessionStorage.removeItem('role');
    sessionStorage.removeItem('firstName');
    sessionStorage.removeItem('lastName');
    sessionStorage.removeItem('email');
  }

  getToken():     string | null { return sessionStorage.getItem('jwt'); }
  getRole():      string | null { return sessionStorage.getItem('role'); }
  getFirstName(): string        { return sessionStorage.getItem('firstName') || ''; }
  getLastName():  string        { return sessionStorage.getItem('lastName')  || ''; }
  getEmail():     string        { return sessionStorage.getItem('email')     || ''; }

  isLoggedIn(): boolean { return !!this.getToken(); }
  isAdmin():    boolean { return this.getRole() === 'ADMIN'; }

  // ─── RECUPERAR CONTRASEÑA (DEMO) ──────────────────────────────────────────
  forgotPassword(email: string): Observable<any> {
    return this.http.post<any>(`${this.apiUrl}/forgot-password`, { email });
  }

  resetPassword(token: string, newPassword: string): Observable<any> {
    return this.http.post<any>(`${this.apiUrl}/reset-password`, { token, newPassword });
  }
}
