import { Component, OnInit } from '@angular/core';
// Servicio que nos da acceso a la informacion de la ruta actual
import { ActivatedRoute, Router } from '@angular/router';
import { Product } from '../../models/product.model';
import { ProductService } from '../../services/product';
import { CartService } from '../../services/cart';        

@Component({
  selector: 'app-product-detail',
  standalone: false,
  templateUrl: './product-detail.html',
  styleUrl: './product-detail.css'
})
export class ProductDetail implements OnInit {

  product: Product | undefined;
  isLoading: boolean = false; //Muestra un indicador de que esta cargando
  errorMessage: string = ''; // Guarda mensajes de error si algo falla
  addedToCart: boolean = false;                                     

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private productService: ProductService,
    private cartService: CartService                              
  ) { }

  ngOnInit(): void {
    this.loadProductDetail();
  }

  loadProductDetail(): void {
    this.isLoading = true;

    // Obtiene el ID del producto en el que se encuentra /products/1, /products/2, etc.
    const id = Number(this.route.snapshot.paramMap.get('id'));

    // Esto previene errores sia lguien intenta acceder a /products/abc o /products/ sin un ID válido, mostrando un mensaje de error en lugar de intentar cargar un producto inexistente.
    if (!id) {
      this.errorMessage = 'ID de producto no válido';
      this.isLoading = false;
      return;
    }

    this.productService.getProductById(id).subscribe({

      // Undefined por si se busca un producto que no existe
      next: (data: Product | undefined) => {
        if (data) {
          this.product = data;
        } else {
          this.errorMessage = 'Producto no encontrado';
        }
        this.isLoading = false;
      },
      error: (error: any) => {
        console.error('Error al cargar el producto:', error);
        this.errorMessage = 'Error al cargar el producto';
        this.isLoading = false;
      }
    });
  }

  goBack(): void {
    this.router.navigate(['/products']);
  }

  // metodo para añadir al carrito real
  addToCart(): void {
  if (this.product) {
    const currentCartItem = this.cartService.getCartItems()
      .find(item => item.product.id === this.product?.id);
    
    const currentQuantityInCart = currentCartItem ? currentCartItem.quantity : 0;
    
    // verifica si hay stock disponible
    if (currentQuantityInCart >= this.product.stock) {
      alert(`❌ Ya tienes el máximo disponible en tu carrito (${this.product.stock} unidades)`);
      return; 
    }
    
    this.cartService.addToCart(this.product);
    this.addedToCart = true;

    // muestra confirmación durante 2 segundos
    setTimeout(() => {
      this.addedToCart = false;
    }, 2000);
  }
}

  
}