import {afterNextRender, ChangeDetectorRef, Component, inject, NgZone, OnInit, PLATFORM_ID} from '@angular/core';
import {JobOfferListDto} from '../../api/models/job-offer-list-dto';
import {Router} from '@angular/router';
import {Api} from '../../api/api';
import {getAllJobOffers} from '../../api/functions';
import {FormsModule} from '@angular/forms';
import {isPlatformBrowser, NgClass, NgForOf, NgIf} from '@angular/common';

@Component({
  selector: 'app-job-offers-list-component',
  imports: [
    FormsModule,
    NgClass,
    NgIf,
    NgForOf
  ],
  templateUrl: './job-offers-list-component.html',
  styleUrl: './job-offers-list-component.css',
})
export class JobOffersListComponent implements OnInit {
  private platformId = inject(PLATFORM_ID);
  jobOffers: JobOfferListDto[] = [];
  filteredJobOffers: JobOfferListDto[] = [];
  isLoading: boolean = true;
  errorMessage: string | null = null;
  selectedOffers: number[] = [];

// Tri
  sortColumn: string = '';
  sortDirection: 'asc' | 'desc' = 'asc';

  // Filtres
  searchQuery: string = '';
  selectedStatus: string = '';
  selectedJobType: string = '';
  selectedContractType: string = '';

  // Options pour les filtres
  statusOptions = [
    { value: '', label: 'Tous les statuts' },
    { value: 'DRAFT', label: 'Brouillon' },
    { value: 'PUBLISHED', label: 'Publié' },
    { value: 'SUSPENDED', label: 'Suspendu' },
    { value: 'ARCHIVED', label: 'Archivé' }
  ];

  jobTypeOptions = [
    { value: '', label: 'Tous les types' },
    { value: 'PER', label: 'PER' },
    { value: 'PATS', label: 'PATS' },
    { value: 'CONTRACTUEL', label: 'Contractuel' }
  ];

  contractTypeOptions = [
    { value: '', label: 'Tous les contrats' },
    { value: 'CDD', label: 'CDD' },
    { value: 'CDI', label: 'CDI' },
    { value: 'STAGE', label: 'Stage' }
  ];

  constructor(
    private api: Api, // Remplacer par votre service API
    private cdr: ChangeDetectorRef,
    private router: Router,
    private ngZone: NgZone
  ) {
  }
  ngOnInit(): void {
    // ✅ Charger UNIQUEMENT côté client
    if (isPlatformBrowser(this.platformId)) {
      // Petit délai pour laisser l'hydratation se terminer
      setTimeout(() => {
        this.loadJobOffers();
      }, 100);
    }
  }

  async loadJobOffers(status?: 'DRAFT' | 'PUBLISHED' | 'SUSPENDED' | 'ARCHIVED'): Promise<void> {
    this.isLoading = true;
    this.errorMessage = null;

    try {
      const params = status ? { status } : {};
      const response = await this.api.invoke(getAllJobOffers, params);

      this.jobOffers = response || [];
      this.filteredJobOffers = [...this.jobOffers];
      this.applyFilters();
      this.isLoading = false;
      console.log('✅ Offres d\'emploi chargées', this.jobOffers);
      console.log('✅ isLoading:', this.isLoading);
      console.log('✅ filteredJobOffers:', this.filteredJobOffers);



      // ✅ Forcer la détection de changement
      this.cdr.detectChanges();

    } catch (error) {
      console.error('❌ Erreur chargement offres', error);
      this.errorMessage = 'Impossible de charger les offres d\'emploi pour le moment.';
      this.isLoading = false;
      this.cdr.detectChanges();
    }
  }
  applyFilters(): void {
    this.filteredJobOffers = this.jobOffers.filter(offer => {
      const matchesSearch = !this.searchQuery ||
        (offer.jobTitle?.toLowerCase().includes(this.searchQuery.toLowerCase()));

      const matchesStatus = !this.selectedStatus ||
        offer.jobStatus === this.selectedStatus;

      const matchesJobType = !this.selectedJobType ||
        offer.jobType === this.selectedJobType;

      const matchesContractType = !this.selectedContractType ||
        offer.typeContrat === this.selectedContractType;

      return matchesSearch && matchesStatus && matchesJobType && matchesContractType;
    });
  }

  onSearchChange(): void {
    this.applyFilters();
  }

  onStatusFilterChange(): void {
    this.applyFilters();
  }

  onJobTypeFilterChange(): void {
    this.applyFilters();
  }

  onContractTypeFilterChange(): void {
    this.applyFilters();
  }

  resetFilters(): void {
    this.searchQuery = '';
    this.selectedStatus = '';
    this.selectedJobType = '';
    this.selectedContractType = '';
    this.applyFilters();
  }

  getStatusLabel(status?: string): string {
    const statusMap: { [key: string]: string } = {
      'DRAFT': 'Brouillon',
      'PUBLISHED': 'Publié',
      'SUSPENDED': 'Suspendu',
      'ARCHIVED': 'Archivé'
    };
    return status ? statusMap[status] || status : '';
  }

  getJobTypeLabel(type?: string): string {
    const typeMap: { [key: string]: string } = {
      'PER': 'PER',
      'PATS': 'PATS',
      'CONTRACTUEL': 'Contractuel'
    };
    return type ? typeMap[type] || type : '';
  }

  getContractTypeLabel(type?: string): string {
    const typeMap: { [key: string]: string } = {
      'CDD': 'CDD',
      'CDI': 'CDI',
      'STAGE': 'Stage'
    };
    return type ? typeMap[type] || type : '';
  }

  getStatusClass(status?: string): string {
    const classMap: { [key: string]: string } = {
      'DRAFT': 'status-draft',
      'PUBLISHED': 'status-published',
      'SUSPENDED': 'status-suspended',
      'ARCHIVED': 'status-archived'
    };
    return status ? classMap[status] || '' : '';
  }

  formatDate(date?: string): string {
    if (!date) return '-';
    const d = new Date(date);
    return d.toLocaleDateString('fr-FR', {
      day: '2-digit',
      month: '2-digit',
      year: 'numeric'
    });
  }

  isExpiringSoon(dateLimite?: string): boolean {
    if (!dateLimite) return false;
    const limite = new Date(dateLimite);
    const today = new Date();
    const diffTime = limite.getTime() - today.getTime();
    const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24));
    return diffDays <= 7 && diffDays >= 0;
  }

  isExpired(dateLimite?: string): boolean {
    if (!dateLimite) return false;
    const limite = new Date(dateLimite);
    const today = new Date();
    return limite < today;
  }

  createNewOffer(): void {
    this.router.navigate(['/rh/job-offers/create']);
  }

  viewOfferDetails(offerId?: number): void {
    if (offerId) {
      this.router.navigate(['/rh/job-offers', offerId]);
    }
  }

  viewCandidates(offerId?: number): void {
    if (offerId) {
      this.router.navigate(['/rh/job-offers', offerId, 'applications']);
    }
  }

  editOffer(offerId?: number): void {
    if (offerId) {
      this.router.navigate(['/rh/job-offers', offerId, 'edit']);
    }
  }

  deleteOffer(offer: JobOfferListDto): void {
    if (!offer.id) return;

    const confirmDelete = confirm(
      `Êtes-vous sûr de vouloir supprimer l'offre "${offer.jobTitle}" ?\n\nCette action est irréversible.`
    );

    if (confirmDelete) {
      // TODO: Implémenter l'appel au service de suppression
      console.log('Suppression de l\'offre', offer.id);
      // this.api.invoke(deleteJobOffer, { id: offer.id })
      //   .then(() => {
      //     this.loadJobOffers();
      //   })
      //   .catch(error => {
      //     console.error('Erreur lors de la suppression', error);
      //   });
    }
  }

  toggleSelect(offerId: number): void {
    const index = this.selectedOffers.indexOf(offerId);
    if (index > -1) {
      this.selectedOffers.splice(index, 1);
    } else {
      this.selectedOffers.push(offerId);
    }
  }

  toggleSelectAll(): void {
    if (this.isAllSelected()) {
      this.selectedOffers = [];
    } else {
      this.selectedOffers = this.filteredJobOffers
        .filter(offer => offer.id !== undefined)
        .map(offer => offer.id!);
    }
  }

  isSelected(offerId?: number): boolean {
    if (!offerId) return false;
    return this.selectedOffers.includes(offerId);
  }

  isAllSelected(): boolean {
    return this.filteredJobOffers.length > 0 &&
      this.selectedOffers.length === this.filteredJobOffers.length;
  }

  isSomeSelected(): boolean {
    return this.selectedOffers.length > 0 &&
      this.selectedOffers.length < this.filteredJobOffers.length;
  }

// ----- Méthodes de tri -----

  sortBy(column: string): void {
    if (this.sortColumn === column) {
      this.sortDirection = this.sortDirection === 'asc' ? 'desc' : 'asc';
    } else {
      this.sortColumn = column;
      this.sortDirection = 'asc';
    }

    this.filteredJobOffers.sort((a, b) => {
      let valueA: any = (a as any)[column];
      let valueB: any = (b as any)[column];

      // Gérer les valeurs nulles/undefined
      if (valueA === undefined || valueA === null) return 1;
      if (valueB === undefined || valueB === null) return -1;

      // Gérer les dates
      if (column === 'datePublication' || column === 'dateLimite') {
        valueA = new Date(valueA).getTime();
        valueB = new Date(valueB).getTime();
      }

      // Gérer les nombres
      if (column === 'applicationCount') {
        valueA = valueA || 0;
        valueB = valueB || 0;
      }

      // Gérer les chaînes
      if (typeof valueA === 'string') {
        valueA = valueA.toLowerCase();
        valueB = valueB.toLowerCase();
      }

      if (valueA < valueB) return this.sortDirection === 'asc' ? -1 : 1;
      if (valueA > valueB) return this.sortDirection === 'asc' ? 1 : -1;
      return 0;
    });
  }

// ----- Actions en masse -----

  bulkChangeStatus(): void {
    if (this.selectedOffers.length === 0) return;

    // TODO: Implémenter une modale pour choisir le nouveau statut
    const newStatus = prompt('Choisir le nouveau statut (DRAFT, PUBLISHED, SUSPENDED, ARCHIVED):');

    if (newStatus) {
      console.log('Changer le statut des offres:', this.selectedOffers, 'vers', newStatus);
      // TODO: Appeler l'API pour mettre à jour en masse
      // this.api.invoke(bulkUpdateStatus, { ids: this.selectedOffers, status: newStatus })
      //   .then(() => {
      //     this.loadJobOffers();
      //     this.selectedOffers = [];
      //   })
      //   .catch(error => console.error('Erreur mise à jour en masse', error));
    }
  }

  bulkDelete(): void {
    if (this.selectedOffers.length === 0) return;

    const confirmDelete = confirm(
      `⚠️ Êtes-vous sûr de vouloir supprimer ${this.selectedOffers.length} offre(s) ?\n\nCette action est irréversible.`
    );

    if (confirmDelete) {
      console.log('Supprimer les offres:', this.selectedOffers);
      // TODO: Appeler l'API pour supprimer en masse
      // this.api.invoke(bulkDeleteJobOffers, { ids: this.selectedOffers })
      //   .then(() => {
      //     this.loadJobOffers();
      //     this.selectedOffers = [];
      //   })
      //   .catch(error => console.error('Erreur suppression en masse', error));
    }
  }

// ----- Méthodes utilitaires -----

  getActiveOffersCount(): number {
    return this.jobOffers.filter(offer => offer.jobStatus === 'PUBLISHED').length;
  }

  hasActiveFilters(): boolean {
    return !!(this.searchQuery || this.selectedStatus ||
      this.selectedJobType || this.selectedContractType);
  }

  clearSearch(): void {
    this.searchQuery = '';
    this.onSearchChange();
  }

  toggleStatusFilter(status: string): void {
    this.selectedStatus = this.selectedStatus === status ? '' : status;
    this.onStatusFilterChange();
  }

  getRelativeDate(date?: string): string {
    if (!date) return '';

    const now = new Date();
    const targetDate = new Date(date);
    const diffTime = targetDate.getTime() - now.getTime();
    const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24));

    if (diffDays < -365) {
      return `Il y a ${Math.abs(Math.floor(diffDays / 365))} an(s)`;
    } else if (diffDays < -30) {
      return `Il y a ${Math.abs(Math.floor(diffDays / 30))} mois`;
    } else if (diffDays < -7) {
      return `Il y a ${Math.abs(Math.floor(diffDays / 7))} semaine(s)`;
    } else if (diffDays < 0) {
      return `Il y a ${Math.abs(diffDays)} jour(s)`;
    } else if (diffDays === 0) {
      return 'Aujourd\'hui';
    } else if (diffDays === 1) {
      return 'Demain';
    } else if (diffDays < 7) {
      return `Dans ${diffDays} jour(s)`;
    } else if (diffDays < 30) {
      return `Dans ${Math.floor(diffDays / 7)} semaine(s)`;
    } else if (diffDays < 365) {
      return `Dans ${Math.floor(diffDays / 30)} mois`;
    } else {
      return `Dans ${Math.floor(diffDays / 365)} an(s)`;
    }
  }

  toggleView(): void {
    // Optionnel: Basculer entre vue tableau et vue cartes
    console.log('Changer de vue (optionnel)');
    // Vous pouvez ajouter une propriété viewMode: 'table' | 'cards'
    // et basculer entre les deux vues dans le template
  }

  exportData(): void {
    // Optionnel: Exporter les données en CSV
    console.log('Exporter les offres');

    // Exemple simple d'export CSV
    const headers = ['Titre', 'Type', 'Contrat', 'Statut', 'Publication', 'Limite', 'Candidatures'];
    const csvData = this.filteredJobOffers.map(offer => [
      offer.jobTitle || '',
      this.getJobTypeLabel(offer.jobType),
      this.getContractTypeLabel(offer.typeContrat),
      this.getStatusLabel(offer.jobStatus),
      this.formatDate(offer.datePublication),
      this.formatDate(offer.dateLimite),
      (offer.applicationCount || 0).toString()
    ]);

    const csvContent = [
      headers.join(','),
      ...csvData.map(row => row.map(cell => `"${cell}"`).join(','))
    ].join('\n');

    const blob = new Blob([csvContent], { type: 'text/csv;charset=utf-8;' });
    const link = document.createElement('a');
    const url = URL.createObjectURL(blob);
    link.setAttribute('href', url);
    link.setAttribute('download', `offres-emploi-${new Date().toISOString().split('T')[0]}.csv`);
    link.style.visibility = 'hidden';
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
  }
}
