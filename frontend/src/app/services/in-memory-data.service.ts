// Implementacion de HttpClient y simular un backend real
// Esto es una libreria que simula un servicio backend en memoria 
// intercepta las peticiones HTTP y las responde como si hubiera un servidor real
import { Injectable } from '@angular/core';
import { InMemoryDbService } from 'angular-in-memory-web-api';
import { Product } from '../models/product.model';

@Injectable({
  providedIn: 'root'
})
export class InMemoryDataService implements InMemoryDbService {

// Creacion de una BBDD en memoria. La API simulada creara 
// automaticamente estos endpoints:

// GET /api/products -> devuelve la lista de productos
// GET /api/products/1 -> devuelve el producto con id 1
// POST /api/products -> agrega un nuevo producto a la lista
// PUT /api/products/1 -> actualiza el producto con id 1
// DELETE /api/products/1 -> elimina el producto con id 1

  createDb() {
    const products: Product[] = [
      {
        id: 1,
        name: 'Pantalla OLED iPhone 14 Pro',
        category: 'Pantallas',
        price: 299.99,
        originalPrice: 399.99,
        description: 'Pantalla OLED original certificada por Apple. Calidad premium con tecnología ProMotion 120Hz.',
        imageUrl: 'assets/products/pant-i14-oem.webp',
        stock: 15,
        isOEM: true,
        specifications: {
          warranty: '12 meses',
          condition: 'Nuevo',
          manufacturer: 'Apple Certified'
        }
      },
      {
        id: 2,
        name: 'Batería iPhone 13',
        category: 'Baterías',
        price: 49.99,
        description: 'Batería de reemplazo con certificación Apple. Capacidad de 3227mAh.',
        imageUrl: 'assets/products/bat-i13-oem.webp',
        stock: 30,
        isOEM: true,
        specifications: {
          warranty: '6 meses',
          condition: 'Nuevo',
          manufacturer: 'Apple Certified'
        }
      },
      {
        id: 3,
        name: 'Cámara Trasera iPhone 15 Pro Max',
        category: 'Cámaras',
        price: 179.99,
        originalPrice: 249.99,
        description: 'Sistema de cámara triple de 48MP. Incluye sensor principal, ultra gran angular y telefoto.',
        imageUrl: 'assets/products/cam-i15promax-oem.jpg',
        stock: 8,
        isOEM: true,
        specifications: {
          warranty: '12 meses',
          condition: 'Nuevo',
          manufacturer: 'Apple OEM'
        }
      },
      {
        id: 4,
        name: 'Pantalla LCD iPhone 11 (Genérica)',
        category: 'Pantallas',
        price: 89.99,
        description: 'Pantalla LCD de alta calidad genérica. Certificada por Apple, excelente relación calidad-precio.',
        imageUrl: 'assets/products/pant-i11-generica.jpg',
        stock: 25,
        isOEM: false,
        specifications: {
          warranty: '6 meses',
          condition: 'Nuevo',
          manufacturer: 'Apple Certified Generic'
        }
      },
      {
        id: 5,
        name: 'Conector de Carga Lightning iPhone 12',
        category: 'Conectores',
        price: 39.99,
        description: 'Puerto de carga Lightning original. Incluye micrófono y cables flex.',
        imageUrl: 'assets/products/puerto-i12-oem.jpg',
        stock: 40,
        isOEM: true,
        specifications: {
          warranty: '6 meses',
          condition: 'Nuevo',
          manufacturer: 'Apple Certified'
        }
      },
      {
        id: 6,
        name: 'Altavoz Auricular iPhone 13 Pro',
        category: 'Audio',
        price: 29.99,
        description: 'Altavoz superior para llamadas. Audio cristalino y compatible con Spatial Audio.',
        imageUrl: 'assets/products/auricular-i13promax-oem.jpg',
        stock: 20,
        isOEM: true,
        specifications: {
          warranty: '3 meses',
          condition: 'Nuevo',
          manufacturer: 'Apple OEM'
        }
      }
    ];

    // la API simulada necesita devolver un objeto con las colecciones
    return { products };
  }

  // metodo para generar IDs automáticamente cuando se crea un nuevo producto
  genId(products: Product[]): number {
    return products.length > 0 ? Math.max(...products.map(p => p.id)) + 1 : 1;
  }
}