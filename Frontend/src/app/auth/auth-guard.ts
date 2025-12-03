import { Injectable, PLATFORM_ID, inject } from '@angular/core';
import { isPlatformBrowser } from '@angular/common';
import { CanActivate, Router, ActivatedRouteSnapshot, RouterStateSnapshot } from '@angular/router';

@Injectable({
  providedIn: 'root'
})
export class AuthGuard implements CanActivate {
  private platformId = inject(PLATFORM_ID);
  private router = inject(Router);

  canActivate(route: ActivatedRouteSnapshot, state: RouterStateSnapshot): boolean {
    if (!isPlatformBrowser(this.platformId)) {
      return true; // Sur le serveur, autoriser (ou false selon votre besoin)
    }

    const token = localStorage.getItem('auth_token');
    const role = localStorage.getItem('role');

    // Si pas authentifié → redirection
    if (!token) {
      console.warn('🚫 Accès refusé - Non authentifié');
      this.router.navigate(['/auth/login'], { queryParams: { returnUrl: state.url } });
      return false;
    }

    // Vérification des rôles si la route l'exige
    const requiredRoles = route.data['roles'] as Array<string>;

    if (requiredRoles && requiredRoles.length > 0) {
      if (!role || !requiredRoles.includes(role)) {
        console.warn(`🚫 Accès refusé - Rôle insuffisant. Requis: ${requiredRoles}, Actuel: ${role}`);
        alert('Vous n\'avez pas les permissions nécessaires pour accéder à cette page.');
        this.router.navigate(['/home']);
        return false;
      }
    }

    console.log('✅ Accès autorisé');
    return true;
  }
}
