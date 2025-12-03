import { Component } from '@angular/core';

import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { Api } from '../../api/api';
import { LoginRequest } from '../../api/models/login-request';
import { AuthResponse } from '../../api/models/auth-response';
import {authenticate} from '../../api/functions';
import {AuthService} from '../auth-service';

@Component({
  imports: [CommonModule, ReactiveFormsModule, RouterModule],
  selector: 'app-login-component',
  templateUrl: './login-component.html',
  styleUrl: './login-component.css',
})
export class LoginComponent {
  loginForm: FormGroup;
  isSubmitting = false;
  showPassword = false;

  constructor(
    private fb: FormBuilder,
    private api: Api,
    private authService: AuthService, // Ajoutez ceci
    private router: Router
  ) {
    this.loginForm = this.fb.group({
      email: ['', [Validators.required, Validators.email]],
      password: ['', [Validators.required]],
      rememberMe: [false]
    });
  }

  isFieldInvalid(fieldName: string): boolean {
    const field = this.loginForm.get(fieldName);
    return !!(field && field.invalid && field.touched);
  }

  getErrorMessage(fieldName: string): string {
    const field = this.loginForm.get(fieldName);

    if (!field) return '';

    if (field.hasError('required')) {
      return 'This field is required';
    }

    if (field.hasError('email')) {
      return 'Please enter a valid email address';
    }

    return '';
  }
  async onSubmit(): Promise<void> {
    // Marquer tous les champs comme touchés pour afficher les erreurs
    if (this.loginForm.invalid) {
      Object.keys(this.loginForm.controls).forEach(key => {
        this.loginForm.get(key)?.markAsTouched();
      });
      return;
    }

    this.isSubmitting = true;

    // Préparer les données pour l'API
    const loginData: LoginRequest = {
      email: this.loginForm.value.email,
      password: this.loginForm.value.password
    };

    console.log('📤 Attempting login...', { email: loginData.email });

    try {
      const response: AuthResponse = await this.api.invoke(authenticate, { body: loginData });

      console.log('✅ Login successful', response);

      // Utiliser le AuthService pour stocker les données
      if (response.token && response.role) {
        this.authService.setAuthData(
          response.token,
          response.role,
          loginData.email // Passer l'email aussi
        );

        // Redirection selon le rôle
        let redirectUrl = '/home'; // Par défaut pour CANDIDATE

        switch (response.role) {
          case 'RH':
            redirectUrl = '/dashboard';
            break;
          case 'COMMISSION_MEMBER':
            redirectUrl = '/commission/my-commissions';
            break;
          case 'CANDIDATE':
            redirectUrl = '/home';
            break;
          default:
            redirectUrl = '/home';
        }

        console.log('🔄 Redirecting to:', redirectUrl);
        await this.router.navigate([redirectUrl]);
      }

    } catch (error: any) {
      console.error('❌ Login error', error);

      let errorMessage = 'An error occurred during login.';

      if (error.status) {
        switch (error.status) {
          case 400:
            errorMessage = 'Invalid credentials. Please check your email and password.';
            break;
          case 401:
            errorMessage = 'Incorrect email or password.';
            break;
          case 403:
            errorMessage = 'Account is disabled or not verified.';
            break;
          case 404:
            errorMessage = 'Account not found. Please sign up first.';
            break;
          case 429:
            errorMessage = 'Too many login attempts. Please try again later.';
            break;
          case 500:
            errorMessage = 'Server error. Please try again later.';
            break;
          default:
            errorMessage = `Error ${error.status}: ${error.message || 'Unknown error'}`;
        }
      }
      this.isSubmitting = false;
    }
  }

  togglePasswordVisibility(): void {
    this.showPassword = !this.showPassword;
  }
}

/**
 * NOTE: AuthResponse interface (example)
 *
 * export interface AuthResponse {
 *   token?: string;
 *   email?: string;
 *   refreshToken?: string;
 *   expiresIn?: number;
 * }
 *
 * Ajustez selon votre modèle AuthResponse généré par OpenAPI
 */
