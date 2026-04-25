// ─── COMPONENTE MIS DATOS ─────────────────────────────────────────────────────
// Muestra y permite editar los datos del usuario autenticado.
// Al guardar, llama a PUT /api/users/me y actualiza localStorage con los nuevos valores.

import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { HttpClient } from '@angular/common/http';
import { AuthService } from '../../services/auth';

@Component({
  selector: 'app-mis-datos',
  standalone: false,
  templateUrl: './mis-datos.html',
  styleUrl: './mis-datos.css'
})
export class MisDatos implements OnInit {

  firstName: string = '';
  lastName:  string = '';
  email:     string = '';
  rol:       string = '';

  editForm!: FormGroup;
  modoEdicion: boolean = false;
  isLoading:   boolean = false;
  successMessage: string = '';
  errorMessage:   string = '';

  constructor(
    private fb: FormBuilder,
    private http: HttpClient,
    private authService: AuthService
  ) {}

  ngOnInit(): void {
    this.firstName = this.authService.getFirstName();
    this.lastName  = this.authService.getLastName();
    this.email     = this.authService.getEmail();
    this.rol       = this.authService.getRole() === 'ADMIN' ? 'Administrador' : 'Usuario';

    // Inicializar el formulario con los datos actuales
    this.editForm = this.fb.group({
      firstName: [this.firstName, [Validators.required, Validators.minLength(2)]],
      lastName:  [this.lastName,  [Validators.required, Validators.minLength(2)]]
    });
  }

  get inicial(): string {
    return this.firstName.charAt(0).toUpperCase() || '?';
  }

  activarEdicion(): void {
    // Resetear el formulario con los valores actuales al abrir el modo edición
    this.editForm.patchValue({ firstName: this.firstName, lastName: this.lastName });
    this.modoEdicion = true;
    this.successMessage = '';
    this.errorMessage = '';
  }

  cancelarEdicion(): void {
    this.modoEdicion = false;
    this.errorMessage = '';
  }

  // Llama al backend para actualizar el perfil y actualiza localStorage
  guardarCambios(): void {
    if (this.editForm.invalid) {
      Object.keys(this.editForm.controls).forEach(k => this.editForm.get(k)?.markAsTouched());
      return;
    }

    this.isLoading = true;
    this.errorMessage = '';

    const { firstName, lastName } = this.editForm.value;

    this.http.put<any>('http://localhost:8080/api/users/me', { firstName, lastName }).subscribe({
      next: (respuesta) => {
        // Actualizar datos locales y sessionStorage con los valores devueltos por el backend
        this.firstName = respuesta.firstName;
        this.lastName  = respuesta.lastName;
        sessionStorage.setItem('firstName', respuesta.firstName);
        sessionStorage.setItem('lastName',  respuesta.lastName);

        this.isLoading = false;
        this.modoEdicion = false;
        this.successMessage = '¡Datos actualizados correctamente!';
      },
      error: () => {
        this.isLoading = false;
        this.errorMessage = 'Error al guardar. Inténtalo de nuevo.';
      }
    });
  }
}
