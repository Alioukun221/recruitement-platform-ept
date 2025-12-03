import {ChangeDetectorRef, Component, inject, OnInit, PLATFORM_ID} from '@angular/core';
import {getJobOfferById} from '../../api/functions';
import {JobOfferResponseDto} from '../../api/models/job-offer-response-dto';
import {ActivatedRoute, Router} from '@angular/router';
import {Api} from '../../api/api';
import {CommonModule, isPlatformBrowser, NgClass} from '@angular/common';

@Component({
  selector: 'app-job-offer-detail-component',
  imports: [
    NgClass,
    CommonModule
  ],
  templateUrl: './job-offer-detail-component.html',
  styleUrl: './job-offer-detail-component.css',
})
export class JobOfferDetailComponent implements OnInit {
  private platformId = inject(PLATFORM_ID);
  jobOffer: JobOfferResponseDto = {};
  offerId: number | null = null;
  requiredSkills: string[] = [];
  isLoading: boolean = true;
  errorMessage: string | null = null;

  constructor(
    private api: Api,
    private route: ActivatedRoute,
    private router: Router,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    this.route.params.subscribe(params => {
      this.offerId = +params['offerId'];

      // ✅ Charger UNIQUEMENT côté client
      if (this.offerId && isPlatformBrowser(this.platformId)) {
        setTimeout(() => {
          this.loadJobOffer();
        }, 100);
      }
    });
  }

  async loadJobOffer(): Promise<void> {
    if (!this.offerId) return;

    this.isLoading = true;
    this.errorMessage = null;

    try {
      const params = { id: this.offerId };

      // ✅ Utiliser await car api.invoke retourne une Promise
      this.jobOffer = await this.api.invoke(getJobOfferById, params);

      console.log('✅ Détails de l\'offre chargés', this.jobOffer);
      this.isLoading = false;
      this.requiredSkills = this.jobOffer.requiredSkills
        ? this.jobOffer.requiredSkills.split(';').map(s => s.trim())
        : [];
      this.cdr.detectChanges();



    } catch (error) {
      console.error('❌ Erreur chargement détails offre', error);
      this.errorMessage = 'Impossible de charger les détails de l\'offre pour le moment.';
      this.isLoading = false;
      this.cdr.detectChanges();
    }
  }

  getStatusLabel(status?: string): string {
    const statusMap: { [key: string]: string } = {
      'DRAFT': 'Brouillon',
      'PUBLISHED': 'Publiée',
      'SUSPENDED': 'Suspendue',
      'ARCHIVED': 'Archivée'
    };
    return status ? statusMap[status] || status : '';
  }

  getJobTypeLabel(type?: string): string {
    const typeMap: { [key: string]: string } = {
      'PER': 'Personnel Enseignant et de Recherche',
      'PATS': 'Personnel Administratif, Technique et de Service',
      'CONTRACTUEL': 'Contractuel'
    };
    return type ? typeMap[type] || type : '';
  }

  getContractTypeLabel(type?: string): string {
    const typeMap: { [key: string]: string } = {
      'CDD': 'Contrat à Durée Déterminée',
      'CDI': 'Contrat à Durée Indéterminée',
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
      month: 'long',
      year: 'numeric'
    });
  }

  getDaysRemaining(dateLimite?: string): number {
    if (!dateLimite) return 0;
    const limite = new Date(dateLimite);
    const today = new Date();
    const diffTime = limite.getTime() - today.getTime();
    const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24));
    return diffDays;
  }

  isExpiringSoon(dateLimite?: string): boolean {
    const days = this.getDaysRemaining(dateLimite);
    return days <= 7 && days >= 0;
  }

  isExpired(dateLimite?: string): boolean {
    const days = this.getDaysRemaining(dateLimite);
    return days < 0;
  }

  viewCandidates(): void {
    if (this.offerId) {
      this.router.navigate(['/rh/job-offers', this.offerId, 'applications']);
    }
  }

  editOffer(): void {
    if (this.offerId) {
      this.router.navigate(['/rh/job-offers', this.offerId, 'edit']);
    }
  }

  archiveOffer(): void {
    const confirmArchive = confirm(
      'Êtes-vous sûr de vouloir archiver cette offre ?\n\nLes candidats ne pourront plus postuler.'
    );

    if (confirmArchive) {
      // TODO: Implémenter l'appel au service d'archivage
      console.log('Archivage de l\'offre', this.offerId);
    }
  }

  republishOffer(): void {
    const confirmRepublish = confirm(
      'Êtes-vous sûr de vouloir republier cette offre ?'
    );

    if (confirmRepublish) {
      // TODO: Implémenter l'appel au service de republication
      console.log('Republication de l\'offre', this.offerId);
    }
  }

  downloadPDF(): void {
    // TODO: Implémenter le téléchargement PDF
    console.log('Téléchargement PDF de l\'offre', this.offerId);
  }

  goBack(): void {
    this.router.navigate(['/rh/job-offers']);
  }

  scrollToSection(sectionId: string): void {
    const element = document.getElementById(sectionId);
    if (element) {
      element.scrollIntoView({ behavior: 'smooth', block: 'start' });
    }
  }
}
