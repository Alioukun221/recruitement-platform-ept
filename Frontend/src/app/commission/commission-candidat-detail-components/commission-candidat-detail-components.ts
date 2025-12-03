import {ChangeDetectorRef, Component, inject, OnInit, PLATFORM_ID} from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import {CommissionApplicationDetailDto} from '../../api/models/commission-application-detail-dto';
import {Api} from '../../api/api';
import {getApplicationDetails1} from '../../api/functions';
import {response} from 'express';
import {FormsModule} from '@angular/forms';
import {isPlatformBrowser, NgClass, NgForOf, NgIf} from '@angular/common';

interface MyEvaluation {
  technicalSkills: number;
  behavioralSkills: number;
  professionalExperience: number;
  motivation: number;
  comment: string;
}

@Component({
  selector: 'app-candidate-detail',
  templateUrl: './commission-candidat-detail-components.html',
  imports: [
    FormsModule,
    NgClass,
    NgIf,
    NgForOf
  ],
  styleUrls: ['./commission-candidat-detail-components.css']
})
export class CommissionCandidatDetailComponents implements OnInit {
  private platformId = inject(PLATFORM_ID);
  candidate: CommissionApplicationDetailDto = {};
  loading: boolean = true;
  error: string | null = null;

  commissionId!: number;
  applicationId!: number;

  myEvaluation: MyEvaluation = {
    technicalSkills: 0,
    behavioralSkills: 0,
    professionalExperience: 0,
    motivation: 0,
    comment: ''
  };

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private apiService: Api,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    if (isPlatformBrowser(this.platformId)) {
      // Récupération des paramètres de route
      this.route.params.subscribe(params => {
        this.commissionId = +params['commissionId'];
        this.applicationId = +params['applicationId'];

        if (this.commissionId && this.applicationId) {
          this.loadCandidateDetails();
        } else {
          this.error = 'Paramètres de navigation invalides';
          this.loading = false;
        }
      });

    }}
  async loadCandidateDetails(): Promise<void> {
    this.loading = true;
    this.error = null;

    try {
      const response = await this.apiService.invoke(getApplicationDetails1, {commissionId: this.commissionId, applicationId: this.applicationId});
      console.log('candidat reçues :', response);
      this.candidate = response;
      this.loading = false;
      this.loading = false;
      this.cdr.detectChanges();

    } catch (error: any) {
      console.error('Erreur:', error);
      this.error = "Impossible de charger le candidat";
      this.loading = false;
      this.cdr.detectChanges();
    }
  }

  goBack(): void {
    this.router.navigate(['/commissions', this.commissionId, 'list-candidatures']);
  }

  getInitials(): string {
    if (!this.candidate) return '';
    const firstInitial = this.candidate.firstName?.charAt(0).toUpperCase() || '';
    const lastInitial = this.candidate.lastName?.charAt(0).toUpperCase() || '';
    return `${firstInitial}${lastInitial}`;
  }

  getEvaluatorInitials(evaluation: any): string {
    const firstInitial = evaluation.evaluatorFirstName?.charAt(0).toUpperCase() || '';
    const lastInitial = evaluation.evaluatorLastName?.charAt(0).toUpperCase() || '';
    return `${firstInitial}${lastInitial}`;
  }

  formatDate(dateString?: string): string {
    if (!dateString) return 'N/A';
    const date = new Date(dateString);
    return date.toLocaleDateString('fr-FR', {
      day: '2-digit',
      month: 'long',
      year: 'numeric'
    });
  }

  getScoreBadgeClass(): string {
    const score = this.candidate?.scoreIA || 0;
    if (score >= 70) return 'high';
    if (score >= 40) return 'medium';
    return 'low';
  }

  getStarsArray(rating?: number): number[] {
    const fullStars = Math.floor(rating || 0);
    return Array(fullStars).fill(0);
  }

  getEmptyStarsArray(rating?: number): number[] {
    const fullStars = Math.floor(rating || 0);
    const emptyStars = 5 - fullStars;
    return Array(emptyStars).fill(0);
  }

  downloadCV(): void {
    if (this.candidate?.cvUrl) {
      window.open(this.candidate.cvUrl, '_blank');
    }
  }

  viewCV(): void {
    if (this.candidate?.cvUrl) {
      window.open(this.candidate.cvUrl, '_blank');
    }
  }

  setRating(criterion: keyof MyEvaluation, value: number): void {
    if (criterion !== 'comment') {
      this.myEvaluation[criterion] = value as never;
    }
  }

  isEvaluationValid(): boolean {
    return (
      this.myEvaluation.technicalSkills > 0 &&
      this.myEvaluation.behavioralSkills > 0 &&
      this.myEvaluation.professionalExperience > 0 &&
      this.myEvaluation.motivation > 0
    );
  }

  submitEvaluation(): void {
    if (!this.isEvaluationValid()) {
      alert('Veuillez compléter tous les critères d\'évaluation');
      return;
    }

    // Appel API pour soumettre l'évaluation
    // this.apiService.submitEvaluation(this.commissionId, this.applicationId, this.myEvaluation)
    //   .subscribe({
    //     next: () => {
    //       alert('Évaluation soumise avec succès');
    //       this.loadCandidateDetails(); // Recharger les données
    //     },
    //     error: (err) => {
    //       console.error('Erreur lors de la soumission:', err);
    //       alert('Erreur lors de la soumission de l\'évaluation');
    //     }
    //   });

    // Pour le moment, simulation
    console.log('Évaluation soumise:', this.myEvaluation);
    alert('Évaluation soumise avec succès (simulation)');
  }
}
