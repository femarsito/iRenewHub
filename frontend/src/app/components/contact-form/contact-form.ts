import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';

@Component({
  selector: 'app-contact-form',
  standalone: false,
  templateUrl: './contact-form.html',
  styleUrl: './contact-form.css'
})
export class ContactForm implements OnInit {

  contactForm!: FormGroup;
  isSubmitting: boolean = false;
  submitSuccess: boolean = false;
  submitError: boolean = false;

  constructor(private formBuilder: FormBuilder) { }

  ngOnInit(): void {
    this.initializeForm();
  }

  initializeForm(): void {
    // FormBuilder amplifica la creacion de formularios reactivos.
    // FormGroup representa el formulario completo
    this.contactForm = this.formBuilder.group({
      name: ['', [Validators.required, Validators.minLength(3)]], // Valida que min 3 caracteres
      email: ['', [Validators.required, Validators.email]], // Valida que sea un email
      phone: ['', [Validators.required, Validators.pattern(/^[0-9]{9}$/)]], //Patron personalizado de 9 caracteres
      subject: ['', [Validators.required, Validators.minLength(5)]],
      message: ['', [Validators.required, Validators.minLength(20), Validators.maxLength(500)]] // Max 500 caracteres
    });
  }

  // métodos auxiliares para acceder a los campos del formulario
  get name() {
    return this.contactForm.get('name');
  }

  get email() {
    return this.contactForm.get('email');
  }

  get phone() {
    return this.contactForm.get('phone');
  }

  get subject() {
    return this.contactForm.get('subject');
  }

  get message() {
    return this.contactForm.get('message');
  }

  onSubmit(): void {
    // Validacion antes de enviar:
    // Marca todos los campos como tocados para mostrar errores
    if (this.contactForm.invalid) {
      Object.keys(this.contactForm.controls).forEach(key => {
        this.contactForm.get(key)?.markAsTouched();
      });
      return;
    }

    // simula envío del formulario
    this.isSubmitting = true;
    this.submitSuccess = false;
    this.submitError = false;

    setTimeout(() => {
      // simular éxito
      if (Math.random() > 0.1) {
        this.submitSuccess = true;
        console.log('Formulario enviado:', this.contactForm.value);
        
        // resetea el formulario después del éxito
        this.contactForm.reset();
        
        // oculta el mensaje de éxito después de 5 segundos
        setTimeout(() => {
          this.submitSuccess = false;
        }, 5000);
      } else {
        this.submitError = true;
        
        setTimeout(() => {
          this.submitError = false;
        }, 5000);
      }
      
      this.isSubmitting = false;
    }, 2000);
  }

  resetForm(): void {
    this.contactForm.reset();
    this.submitSuccess = false;
    this.submitError = false;
  }
}