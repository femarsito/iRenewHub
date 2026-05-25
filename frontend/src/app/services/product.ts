// ---- SERVICIO DE PRODUCTOS ----
// Centraliza todas las llamadas HTTP al endpoint de productos del backend.
// Patrón: Componente → ProductService → HTTP → Backend (Spring Boot) → PostgreSQL

import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable, of } from 'rxjs';
import { catchError, map, tap } from 'rxjs/operators';
import { Product } from '../models/product.model';

@Injectable({
  providedIn: 'root'
})
export class ProductService {

  // URL absoluta al backend Spring Boot. NUNCA relativa (causaría que Angular sirva la petición a sí mismo)
  private apiUrl = 'http://localhost:8080/api/products';

  httpOptions = {
    headers: new HttpHeaders({ 'Content-Type': 'application/json' })
  };

  constructor(private http: HttpClient) { }

  // Devuelve todos los productos adaptados al modelo del frontend
  getProducts(): Observable<Product[]> {
    return this.http.get<any[]>(this.apiUrl).pipe(
      map(productos => productos.map(p => this.mapToFrontend(p))),
      tap(_ => console.log('Productos cargados desde el backend')),
      catchError(this.handleError<Product[]>('getProducts', []))
    );
  }

  // Devuelve un único producto por ID
  getProductById(id: number): Observable<Product | undefined> {
    return this.http.get<any>(`${this.apiUrl}/${id}`).pipe(
      map(p => this.mapToFrontend(p)),
      tap(_ => console.log(`Producto cargado: id=${id}`)),
      catchError(this.handleError<Product>(`getProductById id=${id}`))
    );
  }

  // Filtra por categoría usando el parámetro ?category=X del backend
  getProductsByCategory(category: string): Observable<Product[]> {
    return this.http.get<any[]>(`${this.apiUrl}?category=${category}`).pipe(
      map(productos => productos.map(p => this.mapToFrontend(p))),
      catchError(this.handleError<Product[]>('getProductsByCategory', []))
    );
  }

  getCategories(): Observable<string[]> {
    return this.http.get<any[]>(this.apiUrl).pipe(
      map(productos => [...new Set(productos.map((p: any) => p.category))]),
      catchError(this.handleError<string[]>('getCategories', []))
    );
  }

  addProduct(product: Product): Observable<Product> {
    return this.http.post<Product>(this.apiUrl, product, this.httpOptions).pipe(
      tap((p: Product) => console.log(`Producto creado con id=${p.id}`)),
      catchError(this.handleError<Product>('addProduct'))
    );
  }

  updateProduct(product: Product): Observable<any> {
    return this.http.put(`${this.apiUrl}/${product.id}`, product, this.httpOptions).pipe(
      tap(_ => console.log(`Producto actualizado: id=${product.id}`)),
      catchError(this.handleError<any>('updateProduct'))
    );
  }

  deleteProduct(id: number): Observable<Product> {
    return this.http.delete<Product>(`${this.apiUrl}/${id}`, this.httpOptions).pipe(
      tap(_ => console.log(`Producto eliminado: id=${id}`)),
      catchError(this.handleError<Product>('deleteProduct'))
    );
  }

  // ---- ADAPTACIÓN DE MODELOS ----
  // El backend devuelve campos planos (isOem, warranty, condition, manufacturer).
  // El frontend espera isOEM y specifications anidado.
  // Este método es el ÚNICO punto de transformación entre ambos modelos.
  private mapToFrontend(raw: any): Product {
    return {
      id: raw.id,
      name: raw.name,
      category: raw.category,
      price: raw.price,
      originalPrice: raw.originalPrice,
      description: raw.description,
      imageUrl: raw.imageUrl,
      stock: raw.stock,
      isOEM: raw.isOem,           // Backend: isOem → Frontend: isOEM
      specifications: {
        warranty: raw.warranty,
        condition: raw.condition,
        manufacturer: raw.manufacturer
      }
    };
  }

  // Si la petición HTTP falla, devuelve un valor por defecto para que la app no se rompa
  private handleError<T>(operation = 'operation', result?: T) {
    return (error: any): Observable<T> => {
      console.error(`${operation} falló:`, error);
      console.error('Detalles del error:', error.message);
      return of(result as T);
    };
  }
}
