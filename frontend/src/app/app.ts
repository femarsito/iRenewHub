// ─── COMPONENTE RAÍZ ──────────────────────────────────────────────────────────
// Oculta el navbar en la página de login.
// La sesión se gestiona con sessionStorage (ver AuthService):
//   - F5 / recarga → la sesión se mantiene (sessionStorage persiste en la misma pestaña)
//   - Cerrar el navegador → la sesión se borra automáticamente

import { Component } from '@angular/core';
import { Router } from '@angular/router';

@Component({
  selector: 'app-root',
  standalone: false,
  templateUrl: './app.html',
  styleUrl: './app.css'
})
export class App {
  title = 'iRenewHub';

  constructor(private router: Router) {}

  get mostrarNavbar(): boolean {
    return this.router.url !== '/login';
  }
}
