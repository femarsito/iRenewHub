import { Injectable } from '@angular/core';
import { Observable, of, delay, throwError } from 'rxjs';

export interface ContactMessage {
  name: string;
  email: string;
  phone: string;
  subject: string;
  message: string;
  date: Date;
}

@Injectable({
  providedIn: 'root'
})
export class ContactService {

  private messages: ContactMessage[] = [];

  constructor() { }

  // simula el envío de un mensaje de contacto
  sendMessage(message: ContactMessage): Observable<{ success: boolean; message: string }> {
    
    return of({
      success: true,
      message: 'Tu mensaje ha sido enviado correctamente. Te contactaremos pronto.'
    }).pipe(delay(1500));

  }

  // obtiene todos los mensajes 
  getMessages(): ContactMessage[] {
    return this.messages;
  }
}