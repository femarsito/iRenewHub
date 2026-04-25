// ─── COMPONENTE PRODUCT LIST ──────────────────────────────────────────────────
// Muestra el catálogo de productos con tres filtros combinables:
//   1. Categoría  → botones de categoría (Todos, Baterías, Pantallas...)
//   2. Búsqueda   → barra de texto que filtra por nombre
//   3. Modelo     → desplegable con los modelos de iPhone compatibles
//
// applyFilters() combina los tres filtros sobre this.products y actualiza
// this.filteredProducts (la lista que se muestra en pantalla).

import { Component, OnInit } from '@angular/core';
import { Router, ActivatedRoute } from '@angular/router';
import { Product } from '../../models/product.model';
import { ProductService } from '../../services/product';

@Component({
  selector: 'app-product-list',
  standalone: false,
  templateUrl: './product-list.html',
  styleUrl: './product-list.css'
})
export class ProductList implements OnInit {

  products: Product[] = [];          // Lista completa (no se modifica al filtrar)
  filteredProducts: Product[] = [];  // Lista filtrada que se muestra en pantalla
  categories: string[] = [];
  availableModels: string[] = [];    // Modelos de iPhone únicos extraídos de los productos

  selectedCategory: string = 'Todos';
  searchQuery: string = '';
  selectedModel: string = 'Todos';

  isLoading: boolean = false;

  constructor(
    private productService: ProductService,
    private router: Router,
    private route: ActivatedRoute
  ) { }

  ngOnInit(): void {
    this.route.queryParams.subscribe(params => {
      const categoriaDeUrl = params['category'];
      this.cargarProductos(categoriaDeUrl);
    });
  }

  cargarProductos(categoriaInicial?: string): void {
    this.isLoading = true;

    this.productService.getProducts().subscribe({
      next: (productos: Product[]) => {
        this.products = productos;

        const categoriasUnicas = [...new Set(productos.map(p => p.category))];
        this.categories = ['Todos', ...categoriasUnicas];

        // flatMap aplana el array de arrays de compatibilidad en uno solo
        const todosLosModelos = productos.flatMap(p => p.compatibility ?? []);
        const modelosUnicos = [...new Set(todosLosModelos)].sort();
        this.availableModels = ['Todos', ...modelosUnicos];

        if (categoriaInicial) {
          this.selectedCategory = categoriaInicial;
        }

        this.applyFilters();
        this.isLoading = false;

        console.log('Productos cargados:', productos.length);
        console.log('Categorías:', this.categories);
      },
      error: () => {
        this.isLoading = false;
      }
    });
  }

  filterByCategory(category: string): void {
    this.selectedCategory = category;
    this.applyFilters();
  }

  onSearchChange(query: string): void {
    this.searchQuery = query;
    this.applyFilters();
  }

  onModelChange(model: string): void {
    this.selectedModel = model;
    this.applyFilters();
  }

  // Aplica los tres filtros a la vez. El orden no importa, cada uno reduce el resultado.
  applyFilters(): void {
    let resultado = this.products;

    if (this.selectedCategory !== 'Todos') {
      resultado = resultado.filter(p => p.category === this.selectedCategory);
    }

    if (this.searchQuery.trim() !== '') {
      const texto = this.searchQuery.toLowerCase();
      resultado = resultado.filter(p => p.name.toLowerCase().includes(texto));
    }

    if (this.selectedModel !== 'Todos') {
      resultado = resultado.filter(p =>
        p.compatibility?.some(m => m.toLowerCase().includes(this.selectedModel.toLowerCase()))
      );
    }

    this.filteredProducts = resultado;
  }

  viewDetail(productId: number): void {
    this.router.navigate(['/product', productId]);
  }
}
