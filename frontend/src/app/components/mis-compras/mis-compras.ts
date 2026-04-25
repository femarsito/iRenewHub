// ─── COMPONENTE MIS COMPRAS ───────────────────────────────────────────────────
// Muestra el historial de pedidos del usuario autenticado.
// Llama al endpoint GET /api/orders del backend.

import { Component, OnInit } from '@angular/core';
import { HttpClient } from '@angular/common/http';

@Component({
  selector: 'app-mis-compras',
  standalone: false,
  templateUrl: './mis-compras.html',
  styleUrl: './mis-compras.css'
})
export class MisCompras implements OnInit {

  pedidos: any[] = [];
  isLoading: boolean = false;

  constructor(private http: HttpClient) {}

  ngOnInit(): void {
    this.isLoading = true;
    // El JWT se adjunta automáticamente por el JwtInterceptor
    this.http.get<any[]>('http://localhost:8080/api/orders/my-orders').subscribe({
      next: (data) => {
        this.pedidos = data;
        this.isLoading = false;
      },
      error: () => {
        // Si el endpoint no devuelve datos, mostramos estado vacío
        this.pedidos = [];
        this.isLoading = false;
      }
    });
  }
}
