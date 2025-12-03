
import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { Subject, takeUntil } from 'rxjs';
import {AuthService} from '../../auth/auth-service';
@Component({
  selector: 'app-navbar',
  imports: [CommonModule],
  templateUrl: './navbar.html',
  styleUrl: './navbar.css',
})


export class Navbar implements OnInit, OnDestroy {
  isAuthenticated = false;
  userInitials = 'U';
  userName = 'Utilisateur';
  userRole = 'Invité';
  showUserMenu = false;

  private destroy$ = new Subject<void>();

  constructor(
    private authService: AuthService,
    private router: Router
  ) {}

  ngOnInit(): void {
    // S'abonner aux changements d'authentification
    this.authService.isAuthenticated$
      .pipe(takeUntil(this.destroy$))
      .subscribe(isAuth => {
        this.isAuthenticated = isAuth;
        this.updateUserInfo();
      });

    // S'abonner aux changements d'infos utilisateur
    this.authService.userInfo$
      .pipe(takeUntil(this.destroy$))
      .subscribe(() => {
        this.updateUserInfo();
      });
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  /**
   * Met à jour les informations de l'utilisateur affichées
   */
  private updateUserInfo(): void {
    if (this.isAuthenticated) {
      this.userInitials = this.authService.getUserInitials();
      this.userName = this.authService.getUserDisplayName();
      this.userRole = this.authService.getRoleLabel();
    } else {
      this.userInitials = 'U';
      this.userName = 'Invité';
      this.userRole = 'Non connecté';
    }
  }

  /**
   * Toggle le menu utilisateur
   */
  toggleUserMenu(): void {
    this.showUserMenu = !this.showUserMenu;
  }

  /**
   * Ferme le menu utilisateur
   */
  closeUserMenu(): void {
    this.showUserMenu = false;
  }

  /**
   * Navigue vers la page de connexion
   */
  goToLogin(): void {
    this.closeUserMenu();
    this.router.navigate(['/auth/login']);
  }

  /**
   * Navigue vers la page d'inscription
   */
  goToSignup(): void {
    this.closeUserMenu();
    this.router.navigate(['/auth/signup']);
  }

  /**
   * Navigue vers le profil utilisateur
   */
  goToProfile(): void {
    this.closeUserMenu();
    // TODO: Implémenter la route du profil
    console.log('Navigation vers le profil');
  }

  /**
   * Navigue vers les paramètres
   */
  goToSettings(): void {
    this.closeUserMenu();
    // TODO: Implémenter la route des paramètres
    console.log('Navigation vers les paramètres');
  }

  /**
   * Déconnecte l'utilisateur
   */
  logout(): void {
    this.closeUserMenu();

    if (confirm('Êtes-vous sûr de vouloir vous déconnecter ?')) {
      this.authService.logout();
    }
  }
}
