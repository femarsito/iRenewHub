// ─── COMPONENTE CUPONES ───────────────────────────────────────────────────────
// Muestra los cupones de descuento disponibles para el usuario.
// En esta versión del TFG los cupones son estáticos (no vienen del backend).

import { Component } from '@angular/core';

@Component({
  selector: 'app-cupones',
  standalone: false,
  templateUrl: './cupones.html',
  styleUrl: './cupones.css'
})
export class Cupones {

  // Cupones de ejemplo para demostrar la funcionalidad
  cupones = [
    { codigo: 'BIENVENIDO10', descuento: '10%',  descripcion: 'Descuento de bienvenida en tu primera compra', activo: true },
    { codigo: 'VERANO20',     descuento: '20%',  descripcion: 'Promoción de verano en baterías y pantallas',   activo: true },
    { codigo: 'FIDELIDAD5',   descuento: '5%',   descripcion: 'Cupón por fidelidad — más de 3 compras',        activo: false }
  ];
}
