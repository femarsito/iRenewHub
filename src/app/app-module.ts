// 

import { NgModule, LOCALE_ID } from '@angular/core';              
import { registerLocaleData } from '@angular/common';              
import localeEs from '@angular/common/locales/es';                 

import { AppRoutingModule } from './app-routing-module';
import { BrowserModule } from '@angular/platform-browser';
import { ReactiveFormsModule } from '@angular/forms'; // Para formularios reactivos
import { HttpClientModule } from '@angular/common/http';
import { HttpClientInMemoryWebApiModule } from 'angular-in-memory-web-api';

import { App } from './app';
import { Home } from './components/home/home';
import { Navbar } from './components/navbar/navbar';
import { ProductList } from './components/product-list/product-list';
import { ProductDetail } from './components/product-detail/product-detail';
import { ContactForm } from './components/contact-form/contact-form';
import { Cart } from './components/cart/cart';
import { Checkout } from './components/checkout/checkout';

import { ProductService } from './services/product';
import { CartService } from './services/cart';
import { InMemoryDataService } from './services/in-memory-data.service';

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
    Checkout
  ],
  imports: [
    BrowserModule,
    AppRoutingModule,
    ReactiveFormsModule,
    HttpClientModule,
    HttpClientInMemoryWebApiModule.forRoot(
      InMemoryDataService,
      {
        dataEncapsulation: false,
        passThruUnknownUrl: true
      }
    )
  ],
  providers: [
    ProductService,
    CartService,
    { provide: LOCALE_ID, useValue: 'es' }                       
  ],
  bootstrap: [App]
})
export class AppModule { }