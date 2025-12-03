import {Component, OnInit, inject, PLATFORM_ID, ChangeDetectorRef} from '@angular/core';
import { CommonModule, isPlatformBrowser } from '@angular/common';
import { Api } from '../../api/api';
import { getMyApplications } from '../../api/fn/candidats/get-my-applications';
import { CandidateApplicationHistoryDto } from '../../api/models/candidate-application-history-dto';
import { Router, RouterModule } from '@angular/router';
import { formatDate } from '@angular/common';
import {getAvailableJobOffers} from '../../api/fn/candidats/get-available-job-offers';

@Component({
  selector: 'app-candidatures',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './candidatures-component.html',
  styleUrls: ['./candidatures-component.css']
})
export class CandidaturesComponent implements OnInit {
  applications: CandidateApplicationHistoryDto[] = [];
  isLoading = false;
  errorMessage: string | null = null;

  private platformId = inject(PLATFORM_ID);

  constructor(private api: Api, private cdr: ChangeDetectorRef, private router: Router) {}

  ngOnInit(): void {
      this.loadApplications();

  }

  loadApplications(): void {
    this.isLoading = true;
    this.errorMessage = null;

    this.api.invoke(getMyApplications).then(response => {
        this.applications = response || [];
        this.cdr.detectChanges();
        this.isLoading = false;
        console.log('✅ Candidatures chargées', this.applications);
      })
      .catch(error => {
        console.error('❌ Erreur chargement candidatures', error);
        this.errorMessage = 'Impossible de charger vos candidatures pour le moment.';
        this.isLoading = false;
      });
  }

  formatDate(date?: string): string {
    if (!date) return '-';

    try {
      const d = new Date(date);
      if (isNaN(d.getTime())) return '-';

      // Format manuel DD/MM/YYYY
      const day = String(d.getDate()).padStart(2, '0');
      const month = String(d.getMonth() + 1).padStart(2, '0');
      const year = d.getFullYear();

      return `${day}/${month}/${year}`;
    } catch {
      return '-';
    }
  }

  downloadCV(url?: string) {
    if (!url) return;
    // ✅ Vérifier que window existe
    if (typeof window !== 'undefined') {
      window.open(url, '_blank');
    }
  }

  withdrawApplication(app: CandidateApplicationHistoryDto) {
    if (!app.canWithdraw) return;

    // ✅ Remplacer alert() par confirmation UI ou console
    const confirmed = typeof window !== 'undefined'
      ? window.confirm(`Voulez-vous vraiment retirer votre candidature pour "${app.jobTitle}" ?`)
      : false;

    if (confirmed) {
      // TODO: Appeler API pour retirer candidature
      this.applications = this.applications.filter(a => a.id !== app.id);
    }
  }

  getJobTypeLabel(type?: string): string {
    switch (type) {
      case 'PER': return 'Permanent';
      case 'PATS': return 'Part-time / Temps partiel';
      case 'CONTRACTUEL': return 'Contractuel';
      default: return type || '-';
    }
  }

  getContractTypeLabel(type?: string): string {
    switch (type) {
      case 'CDD': return 'CDD';
      case 'CDI': return 'CDI';
      case 'STAGE': return 'Stage';
      default: return type || '-';
    }
  }

  goToDetailAplly(applicationId: number): void {
    this.router.navigate([`candidate/my-applications/${applicationId}`]);
  }
}
