import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable, of } from 'rxjs';
import { catchError, map, tap } from 'rxjs/operators';
import { Product } from '../models/product.model';

@Injectable({
  providedIn: 'root'
})
export class ProductService {

  private apiUrl = 'http://localhost:8080/api/products';

  httpOptions = {
    headers: new HttpHeaders({ 'Content-Type': 'application/json' })
  };

  constructor(private http: HttpClient) { }

  getProducts(): Observable<Product[]> {
    return this.http.get<Product[]>(this.apiUrl)
      .pipe(
        tap(_ => console.log('Productos obtenidos del backend real')),
        map(products => products.map(p => this.mapToFrontend(p))),
        catchError(this.handleError<Product[]>('getProducts', []))
      );
  }

  getProductById(id: number): Observable<Product | undefined> {
    const url = `${this.apiUrl}/${id}`;
    return this.http.get<Product>(url)
      .pipe(
        tap(_ => console.log(`Producto obtenido: id=${id}`)),
        map(p => this.mapToFrontend(p)),
        catchError(this.handleError<Product>(`getProductById id=${id}`))
      );
  }

  getProductsByCategory(category: string): Observable<Product[]> {
    return this.http.get<Product[]>(`${this.apiUrl}?category=${category}`)
      .pipe(
        tap(_ => console.log(`Productos filtrados por categoría: ${category}`)),
        map(products => products.map(p => this.mapToFrontend(p))),
        catchError(this.handleError<Product[]>('getProductsByCategory', []))
      );
  }

  getCategories(): Observable<string[]> {
    return this.http.get<Product[]>(this.apiUrl)
      .pipe(
        map(products => [...new Set(products.map(p => p.category))]),
        catchError(this.handleError<string[]>('getCategories', []))
      );
  }

  addProduct(product: Product): Observable<Product> {
    return this.http.post<Product>(this.apiUrl, product, this.httpOptions)
      .pipe(
        tap((p: Product) => console.log(`Producto creado: id=${p.id}`)),
        catchError(this.handleError<Product>('addProduct'))
      );
  }

  updateProduct(product: Product): Observable<any> {
    return this.http.put(`${this.apiUrl}/${product.id}`, product, this.httpOptions)
      .pipe(
        tap(_ => console.log(`Producto actualizado: id=${product.id}`)),
        catchError(this.handleError<any>('updateProduct'))
      );
  }

  deleteProduct(id: number): Observable<Product> {
    return this.http.delete<Product>(`${this.apiUrl}/${id}`, this.httpOptions)
      .pipe(
        tap(_ => console.log(`Producto eliminado: id=${id}`)),
        catchError(this.handleError<Product>('deleteProduct'))
      );
  }

  // El backend devuelve isOem (minúscula) y campos planos
  // El frontend espera isOEM (mayúscula) y specifications anidado
  private mapToFrontend(p: any): Product {
    return {
      id: p.id,
      name: p.name,
      category: p.category,
      price: p.price,
      originalPrice: p.originalPrice,
      description: p.description,
      imageUrl: p.imageUrl,
      stock: p.stock,
      isOEM: p.isOem,
      compatibility: p.compatibility || [],
      specifications: {
        warranty: p.warranty,
        condition: p.condition,
        manufacturer: p.manufacturer
      }
    };
  }

  private handleError<T>(operation = 'operation', result?: T) {
    return (error: any): Observable<T> => {
      console.error(`${operation} falló:`, error);
      return of(result as T);
    };
  }
}