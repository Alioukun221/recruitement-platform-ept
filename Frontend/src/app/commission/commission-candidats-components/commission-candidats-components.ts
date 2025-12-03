import {ChangeDetectorRef, Component, inject, OnInit, PLATFORM_ID} from '@angular/core';
import {CommonModule, isPlatformBrowser} from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import {CommissionApplicationListDto} from '../../api/models/commission-application-list-dto';
import {Api} from '../../api/api';
import {getShortlistedApplications1} from '../../api/functions';
@Component({
  selector: 'app-commission-candidats-components',
  imports: [CommonModule, FormsModule],
  templateUrl: './commission-candidats-components.html',
  styleUrl: './commission-candidats-components.css',
})
export class CandidatesListComponent implements OnInit {
  private platformId = inject(PLATFORM_ID);
  candidates: CommissionApplicationListDto[] = [];
  filteredCandidates: CommissionApplicationListDto[] = [];

  isLoading = false;
  error: string | null = null;

  // Filtres et recherche
  searchTerm = '';
  statusFilter = 'all';
  sortBy = 'scoreIA';

  // Informations de la commission
  commissionId: number | null = null;
  commissionTitle = '';

  constructor(
    private api: Api,
    private route: ActivatedRoute,
    private router: Router,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit() {
    if (isPlatformBrowser(this.platformId)) {
      // Récupérer l'ID de la commission depuis l'URL
      this.route.params.subscribe(params => {
        this.commissionId = +params['commissionId'];
        if (this.commissionId) {
          this.loadCandidates();
        } else {
          this.error = "ID de commission invalide";
        }
      });
    }
  }

  async loadCandidates(): Promise<void> {
    if (!this.commissionId) return;
    this.isLoading = true;
    this.error = null;

    try {
      const response = await this.api.invoke(getShortlistedApplications1, { commissionId: this.commissionId });
      console.log('Candidats reçus :', response);
      this.candidates = response;
      this.filteredCandidates = response;
      this.applySorting();
      this.isLoading = false;
      this.cdr.detectChanges();

    } catch (error: any) {
      console.error('Erreur:', error);
      this.error = "Impossible de charger les candidats";
      this.isLoading = false;
      this.cdr.detectChanges();
    }
  }

  applyFilters() {
    this.filteredCandidates = this.candidates.filter(candidate => {
      // Filtre de recherche
      const matchesSearch =
        candidate.candidateName?.toLowerCase().includes(this.searchTerm.toLowerCase()) ||
        candidate.specialization?.toLowerCase().includes(this.searchTerm.toLowerCase()) ||
        candidate.highestDegree?.toLowerCase().includes(this.searchTerm.toLowerCase());

      // Filtre de statut
      let matchesStatus = true;
      if (this.statusFilter === 'evaluated') {
        matchesStatus = candidate.alreadyEvaluatedByMe === true;
      } else if (this.statusFilter === 'pending') {
        matchesStatus = candidate.alreadyEvaluatedByMe === false;
      }

      return matchesSearch && matchesStatus;
    });

    this.applySorting();
  }

  applySorting() {
    this.filteredCandidates.sort((a, b) => {
      if (this.sortBy === 'scoreIA') {
        return (b.scoreIA || 0) - (a.scoreIA || 0);
      } else if (this.sortBy === 'avgScore') {
        return (b.averageCommissionScore || 0) - (a.averageCommissionScore || 0);
      } else if (this.sortBy === 'date') {
        const dateA = a.submitDate ? new Date(a.submitDate).getTime() : 0;
        const dateB = b.submitDate ? new Date(b.submitDate).getTime() : 0;
        return dateB - dateA;
      }
      return 0;
    });
  }

  // Utilitaires pour l'affichage
  getInitials(name?: string): string {
    if (!name) return '??';
    return name
      .split(' ')
      .map(n => n[0])
      .join('')
      .toUpperCase()
      .slice(0, 2);
  }

  formatDate(dateString?: string): string {
    if (!dateString) return 'Date inconnue';

    const date = new Date(dateString);
    return date.toLocaleDateString('fr-FR', {
      day: '2-digit',
      month: 'short',
      year: 'numeric'
    });
  }

  getEvaluationStatusLabel(candidate: CommissionApplicationListDto): string {
    if (candidate.alreadyEvaluatedByMe) {
      return 'Évalué par moi';
    }
    if (candidate.evaluationCount && candidate.evaluationCount > 0) {
      return 'En cours';
    }
    return 'Non évalué';
  }

  getEvaluationStatusClass(candidate: CommissionApplicationListDto): string {
    if (candidate.alreadyEvaluatedByMe) {
      return 'evaluated';
    }
    if (candidate.evaluationCount && candidate.evaluationCount > 0) {
      return 'in-progress';
    }
    return 'not-evaluated';
  }

  // Actions sur les candidats
  viewCandidate(applicationId?: number) {
    if (applicationId && this.commissionId) {
      this.router.navigate(['/commissions', this.commissionId, 'list-candidatures', applicationId]);
    }
  }

  evaluateCandidate(applicationId?: number) {
    if (applicationId && this.commissionId) {
      this.router.navigate(['/commissions', this.commissionId, 'applications', applicationId, 'evaluer']);
    }
  }

  openComments(applicationId?: number) {
    if (applicationId && this.commissionId) {
      this.router.navigate(['/commissions', this.commissionId, 'candidats', applicationId, 'commentaires']);
    }
  }

  sendConvocation(applicationId?: number) {
    if (applicationId) {
      // Logique pour envoyer une convocation
      console.log('Envoyer convocation au candidat:', applicationId);
      // TODO: Implémenter l'appel API pour envoyer la convocation
      alert(`Convocation envoyée au candidat ${applicationId}`);
    }
  }
}
