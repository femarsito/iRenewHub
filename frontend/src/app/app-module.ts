// ---- MÓDULO RAÍZ DE LA APLICACIÓN ----
// AppModule es el punto de registro de toda la aplicación Angular.

import { NgModule, LOCALE_ID } from '@angular/core';
import { registerLocaleData } from '@angular/common';
import localeEs from '@angular/common/locales/es';

import { AppRoutingModule } from './app-routing-module';
import { BrowserModule } from '@angular/platform-browser';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { provideHttpClient, withInterceptorsFromDi, HTTP_INTERCEPTORS } from '@angular/common/http';

// ---- COMPONENTES ----
import { App }          from './app';
import { Home }         from './components/home/home';
import { Navbar }       from './components/navbar/navbar';
import { ProductList }  from './components/product-list/product-list';
import { ProductDetail }from './components/product-detail/product-detail';
import { ContactForm }  from './components/contact-form/contact-form';
import { Cart }         from './components/cart/cart';
import { Checkout }     from './components/checkout/checkout';
import { Login }        from './components/login/login';
import { AdminPanel }   from './components/admin-panel/admin-panel';
import { MisDatos }     from './components/mis-datos/mis-datos';
import { MisCompras }   from './components/mis-compras/mis-compras';
import { Cupones }        from './components/cupones/cupones';
import { ResetPassword }  from './components/reset-password/reset-password';

// ---- SERVICIOS E INTERCEPTOR ----
import { ProductService } from './services/product';
import { CartService }    from './services/cart';
import { AuthService }    from './services/auth';
import { JwtInterceptor } from './interceptors/jwt.interceptor';

registerLocaleData(localeEs);

@NgModule({
  declarations: [
    App,
    Home,
    Navbar,
    ProductList,
    ProductDetail,
    ContactForm,
    Cart,
    Checkout,
    Login,
    AdminPanel,
    MisDatos,
    MisCompras,
    Cupones,
    ResetPassword
  ],
  imports: [
    BrowserModule,
    AppRoutingModule,
    FormsModule,
    ReactiveFormsModule
  ],
  providers: [
    ProductService,
    CartService,
    AuthService,
    { provide: LOCALE_ID, useValue: 'es' },
    provideHttpClient(withInterceptorsFromDi()),
    { provide: HTTP_INTERCEPTORS, useClass: JwtInterceptor, multi: true }
  ],
  bootstrap: [App]
})
export class AppModule { }
