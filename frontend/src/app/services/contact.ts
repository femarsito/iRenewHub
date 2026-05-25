import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface ContactRequest {
  name: string;
  email: string;
  phone: string;
  subject: string;
  message: string;
}

@Injectable({
  providedIn: 'root'
})
export class ContactService {

  private readonly API = 'http://localhost:8080/api/contact';

  constructor(private http: HttpClient) {}

  sendMessage(data: ContactRequest): Observable<{ message: string }> {
    return this.http.post<{ message: string }>(this.API, data);
  }
}