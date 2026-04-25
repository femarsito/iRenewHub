// ─── MÓDULO DE RUTAS ─────────────────────────────────────────────────────────
// Define qué componente se muestra para cada URL y qué guards la protegen.
//
// AuthGuard  → requiere estar autenticado (cualquier rol)
// AdminGuard → requiere rol ADMIN

import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';

import { Home }        from './components/home/home';
import { ProductList } from './components/product-list/product-list';
import { ProductDetail }from './components/product-detail/product-detail';
import { ContactForm } from './components/contact-form/contact-form';
import { Cart }        from './components/cart/cart';
import { Checkout }    from './components/checkout/checkout';
import { Login }       from './components/login/login';
import { AdminPanel }  from './components/admin-panel/admin-panel';
import { MisDatos }    from './components/mis-datos/mis-datos';
import { MisCompras }  from './components/mis-compras/mis-compras';
import { Cupones }     from './components/cupones/cupones';

import { AuthGuard, AdminGuard } from './guards/auth.guard';

const routes: Routes = [
  // Raíz → redirige a /home; AuthGuard la protege y manda al login si no hay sesión
  { path: '', redirectTo: '/home', pathMatch: 'full' },

  // ─── PÚBLICA ──────────────────────────────────────────────────────────────
  { path: 'login', component: Login },

  // ─── RUTAS PROTEGIDAS (requieren login) ───────────────────────────────────
  { path: 'home',        component: Home,        canActivate: [AuthGuard] },
  { path: 'products',    component: ProductList, canActivate: [AuthGuard] },
  { path: 'product/:id', component: ProductDetail, canActivate: [AuthGuard] },
  { path: 'contact',     component: ContactForm, canActivate: [AuthGuard] },
  { path: 'cart',        component: Cart,        canActivate: [AuthGuard] },
  { path: 'checkout',    component: Checkout,    canActivate: [AuthGuard] },
  { path: 'perfil',      component: MisDatos,    canActivate: [AuthGuard] },
  { path: 'mis-compras', component: MisCompras,  canActivate: [AuthGuard] },
  { path: 'cupones',     component: Cupones,     canActivate: [AuthGuard] },

  // ─── RUTA EXCLUSIVA ADMIN ─────────────────────────────────────────────────
  { path: 'admin', component: AdminPanel, canActivate: [AuthGuard, AdminGuard] },
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }
