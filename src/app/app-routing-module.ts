import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { Home } from './components/home/home';
import { ProductList } from './components/product-list/product-list';
import { ProductDetail } from './components/product-detail/product-detail';
import { ContactForm } from './components/contact-form/contact-form';
import { Cart } from './components/cart/cart';
import { Checkout } from './components/checkout/checkout';        

const routes: Routes = [
  // Redirige a /home por defecto
  { path: '', redirectTo: '/home', pathMatch: 'full' }, 
  
  // Rutas para los componentes principales
  // Cuando la URL tiene /products muestra ProductList
  { path: 'home', component: Home },
  { path: 'products', component: ProductList },
  { path: 'product/:id', component: ProductDetail },
  { path: 'contact', component: ContactForm },
  { path: 'cart', component: Cart },
  { path: 'checkout', component: Checkout }                      
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }