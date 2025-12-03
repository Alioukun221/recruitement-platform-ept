import { Injectable, PLATFORM_ID, inject } from '@angular/core';
import { isPlatformBrowser } from '@angular/common';
import { Router } from '@angular/router';
import { BehaviorSubject, Observable } from 'rxjs';

export interface UserInfo {
  email?: string;
  role?: string;
  token?: string;
}

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private platformId = inject(PLATFORM_ID);
  private router = inject(Router);

  // Observable pour suivre l'état de connexion
  private isAuthenticatedSubject = new BehaviorSubject<boolean>(this.checkAuthentication());
  public isAuthenticated$: Observable<boolean> = this.isAuthenticatedSubject.asObservable();

  // Observable pour suivre les infos utilisateur
  private userInfoSubject = new BehaviorSubject<UserInfo | null>(this.getUserInfo());
  public userInfo$: Observable<UserInfo | null> = this.userInfoSubject.asObservable();

  constructor() {
    // Vérifier l'authentification au démarrage
    this.checkAuthentication();
  }

  /**
   * Vérifie si l'utilisateur est authentifié
   */
  private checkAuthentication(): boolean {
    if (!isPlatformBrowser(this.platformId)) {
      return false;
    }
    const token = localStorage.getItem('auth_token');
    return !!token;
  }

  /**
   * Récupère les informations de l'utilisateur depuis le localStorage
   */
  getUserInfo(): UserInfo | null {
    if (!isPlatformBrowser(this.platformId)) {
      return null;
    }

    const token = localStorage.getItem('auth_token');
    const role = localStorage.getItem('role');
    const email = localStorage.getItem('email');

    if (!token) {
      return null;
    }

    return { token, role: role || undefined, email: email || undefined };
  }

  /**
   * Récupère le rôle de l'utilisateur
   */
  getUserRole(): string | null {
    if (!isPlatformBrowser(this.platformId)) {
      return null;
    }
    return localStorage.getItem('role');
  }

  /**
   * Récupère le token
   */
  getToken(): string | null {
    if (!isPlatformBrowser(this.platformId)) {
      return null;
    }
    return localStorage.getItem('auth_token');
  }

  /**
   * Vérifie si l'utilisateur a un rôle spécifique
   */
  hasRole(role: string): boolean {
    const userRole = this.getUserRole();
    return userRole === role;
  }

  /**
   * Vérifie si l'utilisateur a l'un des rôles spécifiés
   */
  hasAnyRole(roles: string[]): boolean {
    const userRole = this.getUserRole();
    return userRole !== null && roles.includes(userRole);
  }

  /**
   * Vérifie si l'utilisateur est authentifié
   */
  isAuthenticated(): boolean {
    return this.checkAuthentication();
  }

  /**
   * Stocke les informations d'authentification
   */
  setAuthData(token: string, role: string, email?: string): void {
    if (!isPlatformBrowser(this.platformId)) {
      return;
    }

    localStorage.setItem('auth_token', token);
    localStorage.setItem('role', role);
    if (email) {
      localStorage.setItem('email', email);
    }

    // Mettre à jour les observables
    this.isAuthenticatedSubject.next(true);
    this.userInfoSubject.next({ token, role, email });
  }

  /**
   * Déconnexion de l'utilisateur
   */
  logout(): void {
    if (!isPlatformBrowser(this.platformId)) {
      return;
    }

    console.log('🚪 Déconnexion en cours...');

    // Supprimer toutes les données d'authentification
    localStorage.removeItem('auth_token');
    localStorage.removeItem('role');
    localStorage.removeItem('email');
    localStorage.removeItem('remember_me');

    // Mettre à jour les observables
    this.isAuthenticatedSubject.next(false);
    this.userInfoSubject.next(null);

    console.log('✅ Déconnexion réussie');

    // Rediriger vers la page de connexion
    this.router.navigate(['/auth/login']);
  }

  /**
   * Récupère les initiales de l'utilisateur pour l'avatar
   */
  getUserInitials(): string {
    const userInfo = this.getUserInfo();
    if (userInfo?.email) {
      const emailParts = userInfo.email.split('@')[0].split('.');
      if (emailParts.length >= 2) {
        return (emailParts[0][0] + emailParts[1][0]).toUpperCase();
      }
      return userInfo.email.substring(0, 2).toUpperCase();
    }
    return 'U';
  }

  /**
   * Récupère le nom d'affichage de l'utilisateur
   */
  getUserDisplayName(): string {
    const userInfo = this.getUserInfo();
    if (userInfo?.email) {
      return userInfo.email.split('@')[0];
    }
    return 'Utilisateur';
  }

  /**
   * Récupère le label du rôle pour l'affichage
   */
  getRoleLabel(): string {
    const role = this.getUserRole();
    const roleLabels: { [key: string]: string } = {
      'CANDIDATE': 'Candidat',
      'COMMISSION_MEMBER': 'Membre de commission',
      'RH': 'Ressources Humaines',
      'ADMIN': 'Administrateur'
    };
    return role ? roleLabels[role] || role : 'Invité';
  }
}
