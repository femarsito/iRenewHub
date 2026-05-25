// ---- COMPONENTE MIS COMPRAS ----
// Muestra el historial de pedidos del usuario autenticado.
// Llama al endpoint GET /api/orders del backend.

import { Component, OnInit } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import jsPDF from 'jspdf';

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

  nombreProductos(items: any[]): string {
    if (!items?.length) return '';
    return items.map(i => i.productName).join(', ');
  }

  estadoTexto(status: string): string {
    const map: Record<string, string> = {
      PENDING:   'Pendiente',
      CONFIRMED: 'Confirmado',
      SHIPPED:   'Enviado',
      DELIVERED: 'Entregado',
      CANCELLED: 'Cancelado'
    };
    return map[status] ?? status;
  }

  ngOnInit(): void {
    this.isLoading = true;
    this.http.get<any[]>('http://localhost:8080/api/orders/my-orders').subscribe({
      next: (data) => {
        this.pedidos = data;
        this.isLoading = false;
      },
      error: () => {
        this.pedidos = [];
        this.isLoading = false;
      }
    });
  }

  // ---- GENERAR PDF DE UN PEDIDO ----
  // Carga el logo como base64 usando canvas, que es el formato que acepta jsPDF
  private cargarLogo(): Promise<string> {
    return new Promise((resolve) => {
      const img = new Image();
      img.onload = () => {
        const canvas = document.createElement('canvas');
        canvas.width = img.width;
        canvas.height = img.height;
        canvas.getContext('2d')!.drawImage(img, 0, 0);
        resolve(canvas.toDataURL('image/png'));
      };
      img.onerror = () => resolve('');
      img.src = 'assets/irenewhub_logo.png';
    });
  }

  async descargarPDF(pedido: any): Promise<void> {
    const logo = await this.cargarLogo();
    const doc = new jsPDF({ unit: 'mm', format: 'a4' });
    const pageW   = 210;
    const margin  = 20;
    const contentW = pageW - margin * 2;
    let y = 20;

    // Cabecera
    doc.setFillColor(0, 113, 227);
    doc.rect(0, 0, pageW, 30, 'F');
    if (logo) doc.addImage(logo, 'PNG', margin, 4, 22, 22);
    doc.setTextColor(255, 255, 255);
    doc.setFontSize(20);
    doc.setFont('helvetica', 'bold');
    doc.text('iRenewHub', margin + 26, 17);
    doc.setFontSize(10);
    doc.setFont('helvetica', 'normal');
    doc.text('Repuestos originales para iPhone', pageW - margin, 17, { align: 'right' });

    y = 42;
    doc.setTextColor(0, 0, 0);
    doc.setFontSize(16);
    doc.setFont('helvetica', 'bold');
    doc.text('TICKET DE COMPRA', margin, y);

    y += 8;
    doc.setFontSize(10);
    doc.setFont('helvetica', 'normal');
    doc.setTextColor(100, 100, 100);
    doc.text(`Pedido: ${pedido.orderNumber}`, margin, y);
    doc.text(`Fecha: ${new Date(pedido.createdAt).toLocaleDateString('es-ES')}`, pageW - margin, y, { align: 'right' });

    y += 5;
    doc.text(`Estado: ${this.estadoTexto(pedido.status)}`, margin, y);

    // Línea separadora
    y += 8;
    doc.setDrawColor(0, 113, 227);
    doc.setLineWidth(0.5);
    doc.line(margin, y, pageW - margin, y);

    // Cabecera tabla
    y += 8;
    doc.setFillColor(245, 245, 245);
    doc.rect(margin, y - 5, contentW, 8, 'F');
    doc.setFont('helvetica', 'bold');
    doc.setTextColor(0, 0, 0);
    doc.setFontSize(9);
    doc.text('PRODUCTO',        margin + 2,          y);
    doc.text('CANT.',           margin + contentW * 0.65, y, { align: 'center' });
    doc.text('PRECIO UNIT.',    margin + contentW * 0.80, y, { align: 'center' });
    doc.text('SUBTOTAL',        margin + contentW - 2, y, { align: 'right' });

    // Filas de productos
    y += 6;
    doc.setFont('helvetica', 'normal');
    for (const item of (pedido.items || [])) {
      doc.text(item.productName,                        margin + 2,              y);
      doc.text(String(item.quantity),                   margin + contentW * 0.65, y, { align: 'center' });
      doc.text(`${Number(item.unitPrice).toFixed(2)} €`, margin + contentW * 0.80, y, { align: 'center' });
      doc.text(`${Number(item.subtotal).toFixed(2)} €`,  margin + contentW - 2,   y, { align: 'right' });
      y += 7;
    }

    // Totales
    y += 3;
    doc.line(margin, y, pageW - margin, y);
    y += 7;
    doc.text('Subtotal:',   margin + contentW * 0.55, y);
    doc.text(`${Number(pedido.subtotal).toFixed(2)} €`, margin + contentW - 2, y, { align: 'right' });
    y += 6;
    doc.text('Envío:',      margin + contentW * 0.55, y);
    doc.text(`${Number(pedido.shippingCost).toFixed(2)} €`, margin + contentW - 2, y, { align: 'right' });
    y += 6;
    doc.setFont('helvetica', 'bold');
    doc.setFontSize(11);
    doc.text('TOTAL:',      margin + contentW * 0.55, y);
    doc.text(`${Number(pedido.total).toFixed(2)} €`, margin + contentW - 2, y, { align: 'right' });

    // Pie
    y += 16;
    doc.setFont('helvetica', 'normal');
    doc.setFontSize(8);
    doc.setTextColor(150, 150, 150);
    doc.text('Pago procesado de forma segura por Stripe', pageW / 2, y, { align: 'center' });

    doc.save(`ticket-${pedido.orderNumber}.pdf`);
  }
}
