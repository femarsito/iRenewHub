import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { Product } from '../../models/product.model';
import { ProductService } from '../../services/product';

@Component({
  selector: 'app-home',
  standalone: false,
  templateUrl: './home.html',
  styleUrl: './home.css'
})
export class Home implements OnInit {

  featuredProducts: Product[] = [];
  isLoading: boolean = false;

  categories = [
    { name: 'Pantallas', icon: '📱', description: 'OLED y LCD originales' },
    { name: 'Baterías', icon: '🔋', description: 'Mayor duración garantizada' },
    { name: 'Cámaras', icon: '📷', description: 'Calidad fotográfica original' },
    { name: 'Conectores', icon: '🔌', description: 'Carga y conectividad' },
    { name: 'Audio', icon: '🔊', description: 'Altavoces y micrófonos' }
  ];

  advantages = [
    {
      icon: '✅',
      title: 'Piezas Certificadas',
      description: 'Todas nuestras piezas son OEM originales o genéricas certificadas por Apple'
    },
    {
      icon: '🛡️',
      title: 'Garantía Incluida',
      description: 'Hasta 12 meses de garantía en todas las piezas OEM originales'
    },
    {
      icon: '🚚',
      title: 'Envío Rápido',
      description: 'Entrega en 24-48 horas en toda la península'
    },
    {
      icon: '🔧',
      title: 'Soporte Técnico',
      description: 'Equipo especializado disponible para resolver todas tus dudas'
    }
  ];

  constructor(
    private router: Router,
    private productService: ProductService
  ) { }

  ngOnInit(): void {
    this.loadFeaturedProducts();
  }

  loadFeaturedProducts(): void {
    this.isLoading = true;
    this.productService.getProducts().subscribe({
      next: (products: Product[]) => {
        // muestra solo los 3 primeros como destacados
        this.featuredProducts = products.slice(0, 3);
        this.isLoading = false;
      },
      error: (error: any) => {
        console.error('Error al cargar productos destacados:', error);
        this.isLoading = false;
      }
    });
  }

  goToProducts(): void {
    this.router.navigate(['/products']);
  }

  goToCategory(category: string): void {
    this.router.navigate(['/products'], { queryParams: { category } });
  }

  goToDetail(id: number): void {
    this.router.navigate(['/product', id]);
  }

  goToContact(): void {
    this.router.navigate(['/contact']);
  }
}