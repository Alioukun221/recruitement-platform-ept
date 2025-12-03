import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { Api } from '../../api/api';
import { RegistrationRequest } from '../../api/models/registration-request';
import { register } from "../../api/fn/authentification/register";

@Component({
  selector: 'app-signup',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterModule],
  templateUrl: 'signup-component.html',
  styleUrls: ['signup-component.css']
})
export class SignupComponent {
  signupForm: FormGroup;
  isSubmitting = false;
  showPassword = false;

  constructor(
    private fb: FormBuilder,
    private api: Api,
    private router: Router
  ) {
    this.signupForm = this.fb.group({
      firstName: ['', [Validators.required, Validators.minLength(2)]],
      lastName: ['', [Validators.required, Validators.minLength(2)]],
      email: ['', [Validators.required, Validators.email]],
      phoneNumber: [''],
      adress: [''], // Note: L'API utilise "adress" avec un seul "d"
      password: ['', [Validators.required, Validators.minLength(6)]],
      agreedToTerms: [false]
    });
  }

  isFieldInvalid(fieldName: string): boolean {
    const field = this.signupForm.get(fieldName);
    return !!(field && field.invalid && field.touched);
  }

  getErrorMessage(fieldName: string): string {
    const field = this.signupForm.get(fieldName);

    if (!field) return '';

    if (field.hasError('required')) {
      return 'This field is required';
    }

    if (field.hasError('email')) {
      return 'Please enter a valid email address';
    }

    if (field.hasError('minlength')) {
      const minLength = field.errors?.['minlength'].requiredLength;
      return `Minimum ${minLength} characters required`;
    }

    return '';
  }

  async onSubmit(): Promise<void> {
    // Marquer tous les champs comme touchés pour afficher les erreurs
    if (this.signupForm.invalid) {
      Object.keys(this.signupForm.controls).forEach(key => {
        this.signupForm.get(key)?.markAsTouched();
      });
      return;
    }

    // Vérifier l'acceptation des conditions
    if (!this.signupForm.value.agreedToTerms) {
      alert('Please agree to the terms and conditions to continue');
      return;
    }

    this.isSubmitting = true;

    // Préparer les données pour l'API
    const registrationData: RegistrationRequest = {
      firstName: this.signupForm.value.firstName,
      lastName: this.signupForm.value.lastName,
      email: this.signupForm.value.email,
      password: this.signupForm.value.password,
      phoneNumber: this.signupForm.value.phoneNumber || undefined,
      adress: this.signupForm.value.adress || undefined
    };

    console.log('📤 Submitting registration...', { ...registrationData, password: '***' });

    try {
      // Appel à l'API de registration
      const response = await this.api.invoke(register, { body: registrationData });

      console.log('✅ Registration successful', response);

      alert('🎉 Account created successfully! Please sign in.');

      // Rediriger vers la page de connexion
      this.router.navigate(['/auth/login']);

    } catch (error: any) {
      console.error('❌ Registration error', error);

      let errorMessage = 'An error occurred during registration.';

      if (error.status) {
        switch (error.status) {
          case 400:
            errorMessage = 'Invalid data. Please check your information.';
            if (error.error?.message) {
              errorMessage = error.error.message;
            }
            break;
          case 409:
            errorMessage = 'This email is already registered. Please sign in.';
            break;
          case 422:
            errorMessage = 'Validation error. Please check all fields.';
            break;
          case 500:
            errorMessage = 'Server error. Please try again later.';
            break;
          default:
            errorMessage = `Error ${error.status}: ${error.message || 'Unknown error'}`;
        }
      }

      alert(errorMessage);

      // Afficher les détails de l'erreur en console
      if (error.error) {
        console.error('Error details:', error.error);
      }

    } finally {
      this.isSubmitting = false;
    }
  }

  togglePasswordVisibility(): void {
    this.showPassword = !this.showPassword;
  }
}
