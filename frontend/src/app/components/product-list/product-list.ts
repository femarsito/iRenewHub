// ---- COMPONENTE PRODUCT LIST ----
// Muestra el catalogo de productos con dos filtros combinables:
//   1. Categoria  → botones de categoria (Todos, Baterias, Pantallas...)
//   2. Busqueda   → barra de texto que filtra por nombre
//
// applyFilters() combina los dos filtros sobre this.products y actualiza
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

  selectedCategory: string = 'Todos';
  searchQuery: string = '';

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

        if (categoriaInicial) {
          this.selectedCategory = categoriaInicial;
        }

        this.applyFilters();
        this.isLoading = false;
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

  // Aplica los dos filtros a la vez
  applyFilters(): void {
    let resultado = this.products;

    if (this.selectedCategory !== 'Todos') {
      resultado = resultado.filter(p => p.category === this.selectedCategory);
    }

    if (this.searchQuery.trim() !== '') {
      // normalize('NFD') descompone los caracteres acentuados en letra + acento
      // replace(/[̀-ͯ]/g, '') elimina los acentos resultantes
      // Así "camara" encuentra "Cámara" y "bateria" encuentra "Batería"
      const normalizar = (texto: string) =>
        texto.toLowerCase().normalize('NFD').replace(/[̀-ͯ]/g, '');

      const textoBuscado = normalizar(this.searchQuery);
      resultado = resultado.filter(p => normalizar(p.name).includes(textoBuscado));
    }

    this.filteredProducts = resultado;
  }

  viewDetail(productId: number): void {
    this.router.navigate(['/product', productId]);
  }
}
