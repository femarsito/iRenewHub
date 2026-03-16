
// AQUI ES DONDE CENTRALIZAMOS LOS DATOS DE PRODUCTOS.
// Este servicio usara inyección de dependencias 

import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable, of } from 'rxjs';
import { catchError, tap } from 'rxjs/operators'; //Libreria para manejar flujos de datos asincronos y errores
import { Product } from '../models/product.model';

// Este servicio se puede inyectar en cualquier parte de la aplicación 
// Es un singleton, una unica instancia compartida en toda la app 
@Injectable({
  providedIn: 'root'
})

export class ProductService {

  private apiUrl = 'api/products';  // URL del backend simulado

  httpOptions = {
    headers: new HttpHeaders({ 'Content-Type': 'application/json' })
  };

  //HTTPCliente es el servicio de Angular para hacer peticiones
  // GET, POST, PUT, DELETE
  constructor(private http: HttpClient) { }

  // GET: obtiene todos los productos
  getProducts(): Observable<Product[]> { // Observable dice esta funcion devuelve un flujo de datos asincrono que eventualmente emitira un array de productos
    return this.http.get<Product[]>(this.apiUrl)
      .pipe(
        tap(_ => console.log('Productos obtenidos desde la API')),// Permite ejecutar codigo sin modificar el Observable
        catchError(this.handleError<Product[]>('getProducts', []))
      );
  }

  // GET: obtiene un producto por ID
  getProductById(id: number): Observable<Product | undefined> {
    const url = `${this.apiUrl}/${id}`;
    return this.http.get<Product>(url)
      .pipe( //encadena operadores RxJS
        tap(_ => console.log(`Producto obtenido: id=${id}`)),
        catchError(this.handleError<Product>(`getProductById id=${id}`))
      );
  }

  // GET: filtra productos por categoría
  getProductsByCategory(category: string): Observable<Product[]> {
    return this.http.get<Product[]>(this.apiUrl)
      .pipe(
        tap(products => {
          const filtered = products.filter(p => p.category === category);
          console.log(`Productos filtrados por categoría: ${category}`);
          return filtered;
        }),
        catchError(this.handleError<Product[]>('getProductsByCategory', []))
      );
  }

  // POST: crea un nuevo producto
  addProduct(product: Product): Observable<Product> {
    return this.http.post<Product>(this.apiUrl, product, this.httpOptions)
      .pipe(
        tap((newProduct: Product) => console.log(`Producto creado con id=${newProduct.id}`)),
        catchError(this.handleError<Product>('addProduct'))
      );
  }

  // PUT: actualiza un producto existente
  updateProduct(product: Product): Observable<any> {
    return this.http.put(this.apiUrl, product, this.httpOptions)
      .pipe(
        tap(_ => console.log(`Producto actualizado: id=${product.id}`)),
        catchError(this.handleError<any>('updateProduct'))
      );
  }

  // DELETE: elimina un producto
  deleteProduct(id: number): Observable<Product> {
    const url = `${this.apiUrl}/${id}`;
    return this.http.delete<Product>(url, this.httpOptions)
      .pipe(
        tap(_ => console.log(`Producto eliminado: id=${id}`)),
        catchError(this.handleError<Product>('deleteProduct'))
      );
  }

  getCategories(): Observable<string[]> {
    return this.http.get<Product[]>(this.apiUrl)
      .pipe(
        tap(products => console.log('Categorías obtenidas:', [...new Set(products.map(p => p.category))])),
        catchError(this.handleError<Product[]>('getCategories', []))
      ) as Observable<any>;
  }

  // manejo de errores
  private handleError<T>(operation = 'operation', result?: T) {
    return (error: any): Observable<T> => {
      console.error(`${operation} falló:`, error);
      console.error('Detalles del error:', error.message);
      
      // devuelve un resultado vacío para que la app siga funcionando
      return of(result as T);
    };
  }
}