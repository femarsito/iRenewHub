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

  products: Product[] = [];
  filteredProducts: Product[] = [];
  categories: string[] = [];
  selectedCategory: string = 'Todos';
  isLoading: boolean = false;

  constructor(
    private productService: ProductService,
    private router: Router,
    private route: ActivatedRoute  
  ) { }

  ngOnInit(): void {
    this.loadProducts();
    
    // lee el parámetro de la URL si existe
    this.route.queryParams.subscribe(params => {
      const categoryParam = params['category'];
      if (categoryParam) {
        // espera a que los productos se carguen
        setTimeout(() => {
          this.filterByCategory(categoryParam);
        }, 100);
      }
    });
  }

  loadProducts(): void {
    this.isLoading = true;
    
    this.productService.getProducts().subscribe({
      next: (data: Product[]) => {
        this.products = data;
        this.filteredProducts = data;
        
        // extrae categorías directamente de los productos cargados
        const uniqueCategories = [...new Set(data.map(p => p.category))];
        this.categories = ['Todos', ...uniqueCategories];
        
        this.isLoading = false;
        console.log('Productos cargados:', data.length);
        console.log('Categorías:', this.categories);
      },
      error: (error: any) => {
        console.error('Error al cargar productos:', error);
        this.isLoading = false;
      }
    });
  }

  filterByCategory(category: string): void {
    this.selectedCategory = category;
    
    if (category === 'Todos') {
      this.filteredProducts = this.products;
    } else {
      this.filteredProducts = this.products.filter(p => p.category === category);
    }
    
    console.log('Filtrando por:', category, '- Productos encontrados:', this.filteredProducts.length);
  }

  viewDetail(productId: number): void {
    this.router.navigate(['/product', productId]);
  }
}