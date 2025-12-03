import { ChangeDetectorRef, Component, inject, OnInit, PLATFORM_ID } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { Api } from '../../api/api';
import { getApplicationDetails } from '../../api/functions';
import { ApplicationDetailDto } from '../../api/models/application-detail-dto';
import { isPlatformBrowser, NgClass, NgIf } from '@angular/common';

@Component({
  selector: 'app-application-detail',
  imports: [
    NgClass,
    NgIf
  ],
  templateUrl: './application-detail-component.html',
  styleUrl: './application-detail-component.css',
})
export class ApplicationDetailComponent implements OnInit {
  private platformId = inject(PLATFORM_ID);
  application: ApplicationDetailDto | null = null;
  isLoading: boolean = true;
  errorMessage: string | null = null;
  applicationId: number | null = null;
  offerId: number | null = null;

  constructor(
    private api: Api,
    private cdr: ChangeDetectorRef,
    private router: Router,
    private route: ActivatedRoute
  ) {}

  ngOnInit(): void {
    if (isPlatformBrowser(this.platformId)) {
      this.route.params.subscribe(params => {
        this.applicationId = +params['applicationId'];
        this.offerId = +params['offerId'];
        if (this.applicationId) {
          setTimeout(() => {
            this.loadApplicationDetails();
          }, 100);
        }
      });
    }
  }

  async loadApplicationDetails(): Promise<void> {
    if (!this.applicationId) return;

    this.isLoading = true;
    this.errorMessage = null;

    try {
      const response = await this.api.invoke(getApplicationDetails, {
        applicationId: this.applicationId
      });

      this.application = response || null;
      this.isLoading = false;
      console.log('✅ Détails de la candidature chargés', this.application);

      this.cdr.detectChanges();

    } catch (error) {
      console.error('❌ Erreur chargement détails candidature', error);
      this.errorMessage = 'Impossible de charger les détails de la candidature.';
      this.isLoading = false;
      this.cdr.detectChanges();
    }
  }

  getFullName(): string {
    if (!this.application) return '—';
    return `${this.application.firstName || ''} ${this.application.lastName || ''}`.trim() || '—';
  }

  getInitials(): string {
    if (!this.application) return '?';
    const first = this.application.firstName?.charAt(0) || '';
    const last = this.application.lastName?.charAt(0) || '';
    return (first + last).toUpperCase() || '?';
  }

  getStatusLabel(status?: string): string {
    const statusMap: { [key: string]: string } = {
      'DRAFT': 'Brouillon',
      'SUBMITTED': 'Soumise',
      'UNDER_REVIEW': 'En révision',
      'AI_SCORED': 'Évaluée par IA',
      'SHORTLISTED': 'Présélectionnée',
      'INTERVIEW_SCHEDULED': 'Entretien prévu',
      'INTERVIEW_COMPLETED': 'Entretien terminé',
      'ACCEPTED': 'Acceptée',
      'REJECTED': 'Rejetée',
      'WITHDRAWN': 'Retirée'
    };
    return status ? statusMap[status] || status : '—';
  }

  getStatusClass(status?: string): string {
    const classMap: { [key: string]: string } = {
      'DRAFT': 'status-draft',
      'SUBMITTED': 'status-submitted',
      'UNDER_REVIEW': 'status-under-review',
      'AI_SCORED': 'status-ai-scored',
      'SHORTLISTED': 'status-shortlisted',
      'INTERVIEW_SCHEDULED': 'status-interview-scheduled',
      'INTERVIEW_COMPLETED': 'status-interview-completed',
      'ACCEPTED': 'status-accepted',
      'REJECTED': 'status-rejected',
      'WITHDRAWN': 'status-withdrawn'
    };
    return status ? classMap[status] || '' : '';
  }

  getScoreColor(): string {
    const score = this.application?.scoreIA || 0;
    if (score >= 80) return 'score-excellent';
    if (score >= 60) return 'score-good';
    if (score >= 40) return 'score-average';
    return 'score-low';
  }

  getScoreLabel(): string {
    const score = this.application?.scoreIA || 0;
    if (score >= 80) return 'Excellent';
    if (score >= 60) return 'Bon';
    if (score >= 40) return 'Moyen';
    return 'Faible';
  }

  formatDate(date?: string): string {
    if (!date) return '—';
    const d = new Date(date);
    return d.toLocaleDateString('fr-FR', {
      day: '2-digit',
      month: 'long',
      year: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    });
  }

  getRelativeDate(date?: string): string {
    if (!date) return '';

    const now = new Date();
    const targetDate = new Date(date);
    const diffTime = now.getTime() - targetDate.getTime();
    const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24));

    if (diffDays < 1) {
      return 'Aujourd\'hui';
    } else if (diffDays === 1) {
      return 'Hier';
    } else if (diffDays < 7) {
      return `Il y a ${diffDays} jour(s)`;
    } else if (diffDays < 30) {
      return `Il y a ${Math.floor(diffDays / 7)} semaine(s)`;
    } else if (diffDays < 365) {
      return `Il y a ${Math.floor(diffDays / 30)} mois`;
    } else {
      return `Il y a ${Math.floor(diffDays / 365)} an(s)`;
    }
  }

  generateAutoSummary(): string {
    if (!this.application) return '';

    const name = this.getFullName();
    const degree = this.application.highestDegree || 'diplôme non spécifié';
    const field = this.application.majorField || 'domaine non spécifié';
    const score = this.application.scoreIA || 0;
    const jobTitle = this.application.jobOfferTitle || 'ce poste';

    let summary = `${name} est candidat(e) pour le poste de ${jobTitle}. `;
    summary += `Titulaire d'un ${degree} en ${field}, `;

    if (score >= 80) {
      summary += `ce profil présente une excellente adéquation avec les exigences du poste. `;
    } else if (score >= 60) {
      summary += `ce profil présente une bonne correspondance avec les exigences du poste. `;
    } else if (score >= 40) {
      summary += `ce profil présente une correspondance moyenne avec les exigences du poste. `;
    } else {
      summary += `ce profil nécessite une analyse approfondie pour évaluer son adéquation au poste. `;
    }

    const compMatch = this.application.matchingCompetences || 0;
    const dipMatch = this.application.matchingDiploma || 0;
    const expMatch = this.application.matchingExperience || 0;

    if (compMatch >= 70 && dipMatch >= 70) {
      summary += `Les compétences et le parcours académique correspondent très bien aux attentes. `;
    }

    if (expMatch >= 70) {
      summary += `L'expérience professionnelle est particulièrement adaptée. `;
    } else if (expMatch < 50) {
      summary += `L'expérience professionnelle pourrait nécessiter un accompagnement initial. `;
    }

    return summary;
  }

  downloadCV(): void {
    if (this.application?.cvUrl) {
      window.open(this.application.cvUrl, '_blank');
    }
  }

  scheduleInterview(): void {
    console.log('Programmer un entretien pour la candidature', this.applicationId);
    // TODO: Implémenter la navigation vers le formulaire de planification d'entretien
    alert('Fonctionnalité à implémenter : Planification d\'entretien');
  }

  addEvaluation(): void {
    console.log('Ajouter une évaluation pour la candidature', this.applicationId);
    // TODO: Implémenter la navigation vers le formulaire d'évaluation
    alert('Fonctionnalité à implémenter : Ajout d\'évaluation');
  }

  acceptApplication(): void {
    const confirmAccept = confirm(
      `Êtes-vous sûr de vouloir accepter la candidature de ${this.getFullName()} ?`
    );

    if (confirmAccept) {
      console.log('Accepter la candidature', this.applicationId);
      // TODO: Implémenter l'appel au service pour accepter
      alert('Candidature acceptée avec succès !');
    }
  }

  rejectApplication(): void {
    const reason = prompt('Raison du rejet (optionnel) :');

    if (reason !== null) {
      console.log('Rejeter la candidature', this.applicationId, 'Raison:', reason);
      // TODO: Implémenter l'appel au service pour rejeter
      alert('Candidature rejetée.');
    }
  }

  backToApplications(): void {
    if (this.offerId) {
      this.router.navigate(['/rh/job-offers', this.offerId, 'applications']);
    } else {
      this.router.navigate(['/rh/applications']);
    }
  }

  editApplication(): void {
    if (this.applicationId) {
      this.router.navigate(['/rh/applications', this.applicationId, 'edit']);
    }
  }

  viewJobOffer(): void {
    if (this.application?.jobOfferId) {
      this.router.navigate(['/rh/job-offers', this.application.jobOfferId]);
    }
  }

  viewCandidateProfile(): void {
    if (this.application?.candidateId) {
      this.router.navigate(['/rh/candidates', this.application.candidateId]);
    }
  }

  getMatchingLevel(value?: number): string {
    if (!value) return 'Faible';
    if (value >= 80) return 'Excellent';
    if (value >= 60) return 'Bon';
    if (value >= 40) return 'Moyen';
    return 'Faible';
  }

  getMatchingClass(value?: number): string {
    if (!value) return 'matching-low';
    if (value >= 80) return 'matching-excellent';
    if (value >= 60) return 'matching-good';
    if (value >= 40) return 'matching-average';
    return 'matching-low';
  }

  printDocument(): void {
    window.print();
  }

  shareApplication(): void {
    const url = window.location.href;
    if (navigator.share) {
      navigator.share({
        title: `Candidature de ${this.getFullName()}`,
        text: `Candidature pour ${this.application?.jobOfferTitle || 'le poste'}`,
        url: url,
      });
    } else {
      // Fallback: copier l'URL
      navigator.clipboard.writeText(url);
      alert('Lien copié dans le presse-papier !');
    }
  }
}
