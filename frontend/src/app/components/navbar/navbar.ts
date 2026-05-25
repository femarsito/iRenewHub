// ---- COMPONENTE NAVBAR ----
// Barra de navegación fija con:
//   - Círculo de perfil estilo YouTube (inicial del nombre + menú desplegable)
//   - Badge ADMIN si el usuario tiene ese rol
//   - Contador del carrito en tiempo real
//
// El menú desplegable se cierra al hacer clic fuera gracias a @HostListener.

import { Component, OnInit, HostListener } from '@angular/core';
import { Router } from '@angular/router';
import { CartService } from '../../services/cart';
import { AuthService } from '../../services/auth';

@Component({
  selector: 'app-navbar',
  standalone: false,
  templateUrl: './navbar.html',
  styleUrl: './navbar.css'
})
export class Navbar implements OnInit {

  cartItemCount: number = 0;
  menuAbierto: boolean = false;  // Controla si el dropdown de perfil está visible

  constructor(
    private cartService: CartService,
    public authService: AuthService,
    private router: Router
  ) { }

  ngOnInit(): void {
    this.cartService.cartItems$.subscribe(() => {
      this.cartItemCount = this.cartService.getTotalItems();
    });
  }

  // Abre o cierra el menú de perfil
  toggleMenu(): void {
    this.menuAbierto = !this.menuAbierto;
  }

  // Cierra el menú (usado al seleccionar una opción)
  cerrarMenu(): void {
    this.menuAbierto = false;
  }

  // Cierra el menú si el clic ocurre fuera del contenedor de perfil
  @HostListener('document:click', ['$event'])
  onDocumentClick(event: MouseEvent): void {
    const target = event.target as HTMLElement;
    if (!target.closest('.perfil-container')) {
      this.menuAbierto = false;
    }
  }

  // Inicial del nombre para el círculo de avatar (ej: "F" para "Fernando")
  get inicialNombre(): string {
    return this.authService.getFirstName().charAt(0).toUpperCase() || '?';
  }

  // Nombre completo para el encabezado del menú
  get nombreCompleto(): string {
    return `${this.authService.getFirstName()} ${this.authService.getLastName()}`.trim();
  }

  logout(): void {
    this.cartService.clearCart();   // Borra el carrito del usuario actual antes de cerrar sesión
    this.authService.logout();
    this.menuAbierto = false;
    this.router.navigate(['/login']);
  }
}
