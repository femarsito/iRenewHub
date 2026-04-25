// ─── PANEL DE ADMINISTRACIÓN ──────────────────────────────────────────────────
// Gestiona las 4 secciones del panel: Productos, Pedidos, Categorías y Usuarios.
// Cada sección tiene sus propias peticiones HTTP y estado local.
// Los comentarios explican el WHY de cada decisión, no el WHAT.

import { Component, OnInit } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { AuthService } from '../../services/auth';

@Component({
  selector: 'app-admin-panel',
  standalone: false,
  templateUrl: './admin-panel.html',
  styleUrl: './admin-panel.css'
})
export class AdminPanel implements OnInit {

  readonly API = 'http://localhost:8080/api';

  tabActivo: string = 'productos';
  nombreAdmin: string = '';

  // ─── PRODUCTOS ──────────────────────────────────────────────────────────────
  productos: any[] = [];
  // null = formulario cerrado; objeto = nuevo/editar producto
  productoForm: any = null;
  msgProducto: string = '';
  errorProducto: string = '';

  // ─── PEDIDOS ────────────────────────────────────────────────────────────────
  pedidos: any[] = [];
  msgPedido: string = '';

  // Traduce el enum de estado del backend a español legible
  estadosEspanol: Record<string, string> = {
    PENDING:   'Pendiente',
    CONFIRMED: 'Confirmado',
    SHIPPED:   'Enviado',
    DELIVERED: 'Entregado',
    CANCELLED: 'Cancelado'
  };
  todosLosEstados = ['PENDING', 'CONFIRMED', 'SHIPPED', 'DELIVERED', 'CANCELLED'];

  // ─── CATEGORÍAS ─────────────────────────────────────────────────────────────
  categorias: string[] = [];
  categoriaRenombrando: string | null = null;  // categoría que está siendo renombrada
  nuevoNombreCategoria: string = '';
  msgCategoria: string = '';
  errorCategoria: string = '';

  // ─── USUARIOS ───────────────────────────────────────────────────────────────
  usuarios: any[] = [];
  msgUsuario: string = '';

  // ─── ESTADÍSTICAS (tarjetas superiores) ─────────────────────────────────────
  totalProductos: number = 0;
  totalPedidos: number = 0;
  totalCategorias: number = 0;
  totalUsuarios: number | null = null;

  constructor(
    private http: HttpClient,
    public authService: AuthService
  ) {}

  ngOnInit(): void {
    this.nombreAdmin = this.authService.getFirstName();
    // Cargar estadísticas y la pestaña inicial al arrancar
    this.cargarEstadisticas();
    this.activarTab('productos');
  }

  // Carga los contadores para las tarjetas de estadísticas
  cargarEstadisticas(): void {
    this.http.get<any[]>(`${this.API}/products`).subscribe(p => {
      this.totalProductos = p.length;
      this.totalCategorias = new Set(p.map((x: any) => x.category)).size;
    });
    this.http.get<any>(`${this.API}/admin/users/count`).subscribe(d => {
      this.totalUsuarios = d.count;
    });
    this.http.get<any[]>(`${this.API}/admin/orders`).subscribe(o => {
      this.totalPedidos = o.length;
    });
  }

  // Cambia de pestaña y carga los datos correspondientes
  activarTab(tab: string): void {
    this.tabActivo = tab;
    this.limpiarMensajes();
    if (tab === 'productos')   this.cargarProductos();
    if (tab === 'pedidos')     this.cargarPedidos();
    if (tab === 'categorias')  this.cargarCategorias();
    if (tab === 'usuarios')    this.cargarUsuarios();
  }

  limpiarMensajes(): void {
    this.msgProducto = '';   this.errorProducto = '';
    this.msgPedido = '';
    this.msgCategoria = '';  this.errorCategoria = '';
    this.msgUsuario = '';
  }

  // ═══════════════════════════════════════════════════════════════════════════
  //  PRODUCTOS
  // ═══════════════════════════════════════════════════════════════════════════

  cargarProductos(): void {
    this.http.get<any[]>(`${this.API}/products`).subscribe(p => {
      this.productos = p;
      this.totalProductos = p.length;
    });
  }

  // Abre el formulario. Si 'producto' tiene datos → edición; si no → nuevo.
  abrirFormProducto(producto?: any): void {
    if (producto) {
      // Clonar para no modificar la lista mientras editamos
      this.productoForm = {
        ...producto,
        // compatibility es List<String> en backend; lo mostramos como texto CSV para editar
        compatibilityText: (producto.compatibility || []).join(', ')
      };
    } else {
      this.productoForm = {
        name: '', category: '', price: '', originalPrice: '',
        description: '', imageUrl: '', stock: 1, isOem: false,
        compatibilityText: '', warranty: '', condition: 'Nuevo', manufacturer: ''
      };
    }
    this.errorProducto = '';
  }

  cerrarFormProducto(): void {
    this.productoForm = null;
  }

  guardarProducto(): void {
    if (!this.productoForm.name || !this.productoForm.price || !this.productoForm.category) {
      this.errorProducto = 'Nombre, categoría y precio son obligatorios.';
      return;
    }

    // Construir el body que espera el backend (campos planos del Product entity)
    const body = {
      name:          this.productoForm.name,
      category:      this.productoForm.category,
      price:         parseFloat(this.productoForm.price),
      originalPrice: this.productoForm.originalPrice ? parseFloat(this.productoForm.originalPrice) : null,
      description:   this.productoForm.description,
      imageUrl:      this.productoForm.imageUrl,
      stock:         parseInt(this.productoForm.stock) || 0,
      isOem:         this.productoForm.isOem,
      // Convertir "iPhone 13, iPhone 14" → ["iPhone 13", "iPhone 14"]
      compatibility: this.productoForm.compatibilityText
                       .split(',').map((s: string) => s.trim()).filter((s: string) => s),
      warranty:      this.productoForm.warranty,
      condition:     this.productoForm.condition,
      manufacturer:  this.productoForm.manufacturer
    };

    const esEdicion = !!this.productoForm.id;
    const peticion = esEdicion
      ? this.http.put(`${this.API}/products/${this.productoForm.id}`, body)
      : this.http.post(`${this.API}/products`, body);

    peticion.subscribe({
      next: () => {
        this.msgProducto = esEdicion ? 'Producto actualizado.' : 'Producto creado.';
        this.cerrarFormProducto();
        this.cargarProductos();
        this.cargarEstadisticas();
      },
      error: () => { this.errorProducto = 'Error al guardar el producto.'; }
    });
  }

  eliminarProducto(id: number): void {
    if (!confirm('¿Eliminar este producto? Esta acción no se puede deshacer.')) return;
    this.http.delete(`${this.API}/products/${id}`).subscribe({
      next: () => { this.msgProducto = 'Producto eliminado.'; this.cargarProductos(); this.cargarEstadisticas(); },
      error: () => { this.errorProducto = 'Error al eliminar el producto.'; }
    });
  }

  // ═══════════════════════════════════════════════════════════════════════════
  //  PEDIDOS
  // ═══════════════════════════════════════════════════════════════════════════

  cargarPedidos(): void {
    this.http.get<any[]>(`${this.API}/admin/orders`).subscribe(p => {
      this.pedidos = p;
      this.totalPedidos = p.length;
    });
  }

  cambiarEstadoPedido(pedido: any, nuevoEstado: string): void {
    this.http.put(`${this.API}/admin/orders/${pedido.id}/status`, { status: nuevoEstado })
      .subscribe({
        next: (actualizado: any) => {
          // Actualizar solo el pedido afectado en la lista (sin recargar todo)
          pedido.status = actualizado.status;
          this.msgPedido = `Pedido ${pedido.orderNumber} actualizado a "${this.estadosEspanol[actualizado.status]}".`;
        },
        error: () => { this.msgPedido = 'Error al cambiar el estado.'; }
      });
  }

  eliminarPedido(id: number, orderNumber: string): void {
    if (!confirm(`¿Eliminar el pedido ${orderNumber}? Esta acción no se puede deshacer.`)) return;
    this.http.delete(`${this.API}/admin/orders/${id}`).subscribe({
      next: () => {
        this.pedidos = this.pedidos.filter(p => p.id !== id);
        this.msgPedido = `Pedido ${orderNumber} eliminado.`;
        this.cargarEstadisticas();
      },
      error: (err) => { this.msgPedido = err.error?.message || 'Error al eliminar el pedido.'; }
    });
  }

  // Un pedido solo se puede eliminar cuando está entregado o cancelado
  puedeEliminar(pedido: any): boolean {
    return pedido.status === 'DELIVERED' || pedido.status === 'CANCELLED';
  }

  // Clase CSS según el estado del pedido
  clasePorEstado(status: string): string {
    const mapa: Record<string, string> = {
      PENDING:   'estado-pendiente',
      CONFIRMED: 'estado-confirmado',
      SHIPPED:   'estado-enviado',
      DELIVERED: 'estado-entregado',
      CANCELLED: 'estado-cancelado'
    };
    return mapa[status] || '';
  }

  // ═══════════════════════════════════════════════════════════════════════════
  //  CATEGORÍAS
  // ═══════════════════════════════════════════════════════════════════════════

  cargarCategorias(): void {
    this.http.get<string[]>(`${this.API}/admin/categories`).subscribe(c => {
      this.categorias = c;
      this.totalCategorias = c.length;
    });
  }

  iniciarRenombrar(nombre: string): void {
    this.categoriaRenombrando = nombre;
    this.nuevoNombreCategoria = nombre;
    this.msgCategoria = '';
  }

  cancelarRenombrar(): void {
    this.categoriaRenombrando = null;
    this.nuevoNombreCategoria = '';
  }

  confirmarRenombrar(oldName: string): void {
    if (!this.nuevoNombreCategoria.trim()) return;
    this.http.put(`${this.API}/admin/categories/rename`,
      { oldName, newName: this.nuevoNombreCategoria.trim() }
    ).subscribe({
      next: () => {
        this.msgCategoria = `Categoría renombrada de "${oldName}" a "${this.nuevoNombreCategoria}".`;
        this.cancelarRenombrar();
        this.cargarCategorias();
      },
      error: () => { this.errorCategoria = 'Error al renombrar la categoría.'; }
    });
  }

  eliminarCategoria(nombre: string): void {
    if (!confirm(`¿Eliminar la categoría "${nombre}"? Solo se puede si no tiene productos asignados.`)) return;
    this.http.delete(`${this.API}/admin/categories/${encodeURIComponent(nombre)}`).subscribe({
      next: () => { this.msgCategoria = `Categoría "${nombre}" eliminada.`; this.cargarCategorias(); this.cargarEstadisticas(); },
      error: (err) => { this.errorCategoria = err.error?.error || 'Error al eliminar la categoría.'; }
    });
  }

  // ═══════════════════════════════════════════════════════════════════════════
  //  USUARIOS
  // ═══════════════════════════════════════════════════════════════════════════

  cargarUsuarios(): void {
    this.http.get<any[]>(`${this.API}/admin/users`).subscribe(u => {
      this.usuarios = u;
      this.totalUsuarios = u.length;
    });
  }

  eliminarUsuario(id: number, nombre: string): void {
    if (!confirm(`¿Eliminar al usuario "${nombre}"? Esta acción no se puede deshacer.`)) return;
    this.http.delete(`${this.API}/admin/users/${id}`).subscribe({
      next: () => { this.msgUsuario = `Usuario "${nombre}" eliminado.`; this.cargarUsuarios(); this.cargarEstadisticas(); },
      error: () => { this.msgUsuario = 'Error al eliminar el usuario.'; }
    });
  }
}
