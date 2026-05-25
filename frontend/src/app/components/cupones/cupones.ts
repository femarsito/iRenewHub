import { Component, OnInit } from '@angular/core';
import { HttpClient } from '@angular/common/http';

@Component({
  selector: 'app-cupones',
  standalone: false,
  templateUrl: './cupones.html',
  styleUrl: './cupones.css'
})
export class Cupones implements OnInit {

  readonly API = 'http://localhost:8080/api';

  cupones: any[] = [];
  isLoading = true;

  constructor(private http: HttpClient) {}

  ngOnInit(): void {
    this.http.get<any[]>(`${this.API}/coupons/my`).subscribe({
      next: (data) => { this.cupones = data; this.isLoading = false; },
      error: ()    => { this.isLoading = false; }
    });
  }
}
