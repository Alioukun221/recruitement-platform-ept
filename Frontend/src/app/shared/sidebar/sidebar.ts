import { Component, OnInit, OnDestroy } from '@angular/core';
import { RouterLink, RouterLinkActive } from '@angular/router';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { Subject, takeUntil } from 'rxjs';
import {AuthService} from '../../auth/auth-service';

interface MenuItem {
  label: string;
  route: string;
  icon: string;
  roles?: string[]; // Si undefined, accessible à tous
  badge?: number;
  submenu?: SubMenuItem[];
  excludeRoles?: string[]; // Rôles à exclure
}

interface SubMenuItem {
  label: string;
  route: string;
}

@Component({
  selector: 'app-sidebar',
  templateUrl: './sidebar.html',
  imports: [RouterLink, CommonModule, RouterLinkActive],
  styleUrls: ['./sidebar.css']
})
export class SidebarComponent implements OnInit, OnDestroy {
  expandedMenubar = false;
  isSidebarOpen = false;
  isAuthenticated = false;
  userRole: string | null = null;

  private destroy$ = new Subject<void>();

  // Menu items avec configuration des rôles
  menuItems: MenuItem[] = [
    {
      label: 'Dashboard RH',
      route: '/dashboard',
      icon: 'dashboard',
      roles: ['RH'], // Uniquement pour les RH
      badge: 0
    },
    {
      label: 'Offres d\'emploi',
      route: '/home',
      icon: 'calendar',
      badge: 119,
      // Accessible uniquement si NON connecté OU si rôle CANDIDATE
      excludeRoles: ['RH', 'COMMISSION_MEMBER'], // Exclure les RH et membres de commission
      // submenu: [
      //   { label: 'PATS', route: '/home/PATS' },
      //   { label: 'PER', route: '/home/PER' },
      //   { label: 'CDI', route: '/home/CDI' },
      //   { label: 'CDD', route: '/home/CDD' }
      // ]
    },
    {
      label: 'Mes candidatures',
      route: '/candidate/my-applications',
      icon: 'file',
      roles: ['CANDIDATE'] // Uniquement pour les candidats
    },
    {
      label: 'Mes commissions',
      route: '/commission/my-commissions',
      icon: 'users',
      roles: ['COMMISSION_MEMBER'] // Uniquement pour les membres de commission
    },
    {
      label: 'Gestion des offres',
      route: '/rh/job-offers',
      icon: 'briefcase',
      roles: ['RH'] // Uniquement pour les RH
    },
    {
      label: 'Gestion des commissions',
      route: '/rh/commissions',
      icon: 'briefcase',
      roles: ['RH'] // Uniquement pour les RH
    },
    {
      label: 'Notifications',
      route: '#',
      icon: 'bell',
      badge: 5
      // Accessible à tous
    }
  ];

  toolsMenuItems: MenuItem[] = [
    {
      label: 'Paramètres',
      route: '#',
      icon: 'settings'
    },
    {
      label: 'Aide',
      route: '#',
      icon: 'help'
    }
  ];

  constructor(
    private router: Router,
    private authService: AuthService
  ) {}

  ngOnInit(): void {
    // S'abonner aux changements d'authentification
    this.authService.isAuthenticated$
      .pipe(takeUntil(this.destroy$))
      .subscribe(isAuth => {
        this.isAuthenticated = isAuth;
        this.userRole = this.authService.getUserRole();
        console.log('🔐 Auth status:', isAuth, 'Role:', this.userRole);
      });
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  /**
   * Vérifie si un menu item doit être affiché selon le rôle de l'utilisateur
   */
  shouldShowMenuItem(item: MenuItem): boolean {
    // Vérifier d'abord les rôles exclus
    if (item.excludeRoles && item.excludeRoles.length > 0) {
      if (this.isAuthenticated && this.authService.hasAnyRole(item.excludeRoles)) {
        return false; // L'utilisateur a un rôle exclu, ne pas afficher
      }
    }

    // Si pas de rôles spécifiés, accessible à tous (sauf si exclu ci-dessus)
    if (!item.roles || item.roles.length === 0) {
      return true;
    }

    // Si l'utilisateur n'est pas authentifié, ne pas afficher les items protégés
    if (!this.isAuthenticated) {
      return false;
    }

    // Vérifier si l'utilisateur a un des rôles requis
    return this.authService.hasAnyRole(item.roles);
  }

  /**
   * Récupère les menu items filtrés selon les permissions
   */
  getFilteredMenuItems(): MenuItem[] {
    return this.menuItems.filter(item => this.shouldShowMenuItem(item));
  }

  /**
   * Vérifie si l'utilisateur est RH
   */
  isRH(): boolean {
    return this.isAuthenticated && this.userRole === 'RH';
  }

  /**
   * Navigation vers la création d'offre d'emploi
   */
  navigateToCreateOffer(): void {
    this.router.navigate(['/rh/job-offers/create']);
  }

  goToOffresEmploi(): void {
    this.expandedMenubar = !this.expandedMenubar;
    if (!this.expandedMenubar) {
      this.router.navigate(['/home']);
    }
  }

  toggleSidebar(): void {
    this.isSidebarOpen = !this.isSidebarOpen;
  }

  /**
   * Retourne l'icône SVG selon le type
   */
  getIconPath(iconType: string): string {
    const icons: { [key: string]: string } = {
      'dashboard': 'M3 13h8V3H3v10zm0 8h8v-6H3v6zm10 0h8V11h-8v10zm0-18v6h8V3h-8z',
      'calendar': 'M3 4h18v18H3z M16 2v4 M8 2v4 M3 10h18',
      'file': 'M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z M14 2v6h6',
      'briefcase': 'M20 7H4a2 2 0 0 0-2 2v10a2 2 0 0 0 2 2h16a2 2 0 0 0 2-2V9a2 2 0 0 0-2-2z M16 21V5a2 2 0 0 0-2-2h-4a2 2 0 0 0-2 2v16',
      'bell': 'M18 8A6 6 0 0 0 6 8c0 7-3 9-3 9h18s-3-2-3-9 M13.73 21a2 2 0 0 1-3.46 0',
      'settings': 'M12 1v6m0 6v6m5.656-13.656l-4.242 4.242m0 6l-4.242 4.242M23 12h-6m-6 0H1m17.656 5.656l-4.242-4.242m0-6l-4.242-4.242',
      'help': 'M12 22C17.5228 22 22 17.5228 22 12C22 6.47715 17.5228 2 12 2C6.47715 2 2 6.47715 2 12C2 17.5228 6.47715 22 12 22Z M9.09 9C9.3251 8.33167 9.78915 7.76811 10.4 7.40913C11.0108 7.05016 11.7289 6.91894 12.4272 7.03871C13.1255 7.15849 13.7588 7.52152 14.2151 8.06353C14.6713 8.60553 14.9211 9.29152 14.92 10C14.92 12 11.92 13 11.92 13 M12 17H12.01',
      'users': 'M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2 M23 21v-2a4 4 0 0 0-3-3.87 M16 3.13a4 4 0 0 1 0 7.75 M9 11a4 4 0 1 0 0-8 4 4 0 0 0 0 8z'
    };
    return icons[iconType] || icons['file'];
  }
}
