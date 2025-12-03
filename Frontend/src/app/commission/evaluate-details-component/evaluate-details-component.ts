import { Component, OnInit, inject, PLATFORM_ID, ChangeDetectorRef } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { DomSanitizer, SafeResourceUrl } from '@angular/platform-browser';
import { Api } from '../../api/api';
import {
  getMyEvaluation,
  updateMyEvaluation,
  deleteMyEvaluation,
  viewCv, getMyApplicationDetail
} from '../../api/functions';
import { EvaluationResponseDto } from '../../api/models/evaluation-response-dto';
import { CreateEvaluationDto } from '../../api/models/create-evaluation-dto';
import { FormsModule } from '@angular/forms';
import { CommonModule, isPlatformBrowser } from '@angular/common';
import {ApplicationDetailDto} from '../../api/models/application-detail-dto';
import {CandidateApplicationDetailDto} from '../../api/models/candidate-application-detail-dto';

@Component({
  selector: 'app-evaluation-detail',
  imports: [
    FormsModule,
    CommonModule,
  ],
  templateUrl: './evaluate-details-component.html',
  styleUrl: './evaluate-details-component.css',
})
export class EvaluateDetailsComponent implements OnInit {
  private platformId = inject(PLATFORM_ID);

  evaluation: EvaluationResponseDto | null = null;
  isLoading: boolean = true;
  application: CandidateApplicationDetailDto | undefined;
  isLoadingCV: boolean = true;
  isUpdating: boolean = false;
  errorMessage: string | null = null;
  successMessage: string | null = null;
  cvUrl: SafeResourceUrl | null = null;

  commissionId: number = 0;
  applicationId: number = 0;

  // Edit Mode
  isEditMode: boolean = false;
  editData: CreateEvaluationDto = {
    diplomaScore: 0,
    experienceScore: 0,
    competenceScore: 0,
    motivationScore: 0,
    softSkillsScore: 0,
    comment: ''
  };

  constructor(
    private api: Api,
    private route: ActivatedRoute,
    private router: Router,
    private sanitizer: DomSanitizer,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    if (isPlatformBrowser(this.platformId)) {
      this.route.params.subscribe(params => {
        this.commissionId = +params['commissionId'];
        this.applicationId = +params['applicationId'];

        if (this.commissionId && this.applicationId) {
          this.loadEvaluation();
          this.loadApplicationDetails();
          this.loadCV();
        }
      });
    }
  }

  async loadEvaluation(): Promise<void> {
    this.isLoading = true;
    this.errorMessage = null;

    try {
      const response = await this.api.invoke(getMyEvaluation, {
        commissionId: this.commissionId,
        applicationId: this.applicationId
      });

      this.evaluation = response;
      this.isLoading = false;
      console.log('✅ Évaluation chargée', response);
      this.cdr.detectChanges();

    } catch (error: any) {
      console.error('❌ Erreur chargement évaluation', error);
      this.errorMessage = 'Impossible de charger l\'évaluation.';
      this.isLoading = false;
      this.cdr.detectChanges();
    }
  }
  loadApplicationDetails(): void {
    this.isLoading = true;

    getMyApplicationDetail(this.api.http, this.api.rootUrl, {
      applicationId: this.applicationId
    }).subscribe({
      next: (response) => {
        this.application = response.body;
        this.isLoading = false;
        console.log('✅ Détails de la candidature chargés', this.application);
      },
      error: (err) => {
        console.error('❌ Erreur lors du chargement', err);
        this.isLoading = false;

        // Remplacer alert() par des messages d'erreur appropriés
        // if (err.status === 404) {
        //   this.errorMessage = 'Candidature introuvable';
        // } else if (err.status === 403) {
        //   this.errorMessage = 'Vous n\'avez pas accès à cette candidature';
        // } else {
        //   this.errorMessage = 'Erreur lors du chargement des détails';
        // }

        // Rediriger après un court délai pour permettre à l'utilisateur de voir le message
        setTimeout(() => {
          this.router.navigate(['/candidate/my-applications']);
        }, 2000);
      }

    });
  }
  async loadCV(): Promise<void> {
    this.isLoadingCV = true;

    try {
      const response = await this.api.invoke(viewCv, {
        commissionId: this.commissionId,
        applicationId: this.applicationId
      });

      const blob = new Blob([response as any], { type: 'application/pdf' });
      const url = URL.createObjectURL(blob);
      this.cvUrl = this.sanitizer.bypassSecurityTrustResourceUrl(url);

      this.isLoadingCV = false;
      console.log('✅ CV chargé');

    } catch (error: any) {
      console.error('❌ Erreur chargement CV', error);
      this.isLoadingCV = false;
    }
  }

  enableEditMode(): void {
    if (this.evaluation) {
      this.editData = {
        diplomaScore: this.evaluation.diplomaScore || 0,
        experienceScore: this.evaluation.experienceScore || 0,
        competenceScore: this.evaluation.competenceScore || 0,
        motivationScore: this.evaluation.motivationScore || 0,
        softSkillsScore: this.evaluation.softSkillsScore || 0,
        comment: this.evaluation.comment || ''
      };
      this.isEditMode = true;
    }
  }

  cancelEdit(): void {
    this.isEditMode = false;
    this.editData = {
      diplomaScore: 0,
      experienceScore: 0,
      competenceScore: 0,
      motivationScore: 0,
      softSkillsScore: 0,
      comment: ''
    };
  }

  async saveEdit(): Promise<void> {
    const confirmUpdate = confirm(
      'Êtes-vous sûr de vouloir modifier cette évaluation ?'
    );

    if (!confirmUpdate) {
      return;
    }

    this.isUpdating = true;
    this.errorMessage = null;
    this.successMessage = null;

    try {
      const params = {
        commissionId: this.commissionId,
        applicationId: this.applicationId,
        body: this.editData
      };

      const response = await this.api.invoke(updateMyEvaluation, params);

      console.log('✅ Évaluation mise à jour', response);
      this.evaluation = response;
      this.successMessage = 'Évaluation mise à jour avec succès !';
      this.isEditMode = false;
      this.clearSuccessMessage();
      this.cdr.detectChanges();

    } catch (error: any) {
      console.error('❌ Erreur mise à jour évaluation', error);
      this.errorMessage = 'Une erreur est survenue lors de la mise à jour.';
      this.isUpdating = false;
      this.cdr.detectChanges();
    }
  }

  async deleteEvaluation(): Promise<void> {
    const confirmDelete = confirm(
      '⚠️ Êtes-vous sûr de vouloir supprimer cette évaluation ?\n\nCette action est irréversible.'
    );

    if (!confirmDelete) {
      return;
    }

    this.errorMessage = null;
    this.successMessage = null;

    try {
      await this.api.invoke(deleteMyEvaluation, {
        commissionId: this.commissionId,
        applicationId: this.applicationId
      });

      console.log('✅ Évaluation supprimée');
      this.successMessage = 'Évaluation supprimée avec succès !';

      // Rediriger après 1.5 secondes
      setTimeout(() => {
        this.router.navigate([
          '/commission-member/commissions',
          this.commissionId,
          'applications'
        ]);
      }, 1500);

    } catch (error: any) {
      console.error('❌ Erreur suppression évaluation', error);
      this.errorMessage = 'Une erreur est survenue lors de la suppression.';
    }
  }

  getTotalScore(): number {
    if (!this.evaluation) return 0;
    return (
      (this.evaluation.diplomaScore || 0) +
      (this.evaluation.experienceScore || 0) +
      (this.evaluation.competenceScore || 0) +
      (this.evaluation.motivationScore || 0) +
      (this.evaluation.softSkillsScore || 0)
    );
  }

  getTotalScoreClass(): string {
    const total = this.getTotalScore();
    if (total >= 80) return 'excellent';
    if (total >= 60) return 'good';
    if (total >= 40) return 'average';
    return 'low';
  }

  formatDate(dateString?: string): string {
    if (!dateString) return 'N/A';
    const date = new Date(dateString);
    return date.toLocaleDateString('fr-FR', {
      year: 'numeric',
      month: 'long',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    });
  }

  downloadCV(): void {
    if (this.cvUrl) {
      const url = (this.cvUrl as any).changingThisBreaksApplicationSecurity;
      const link = document.createElement('a');
      link.href = url;
      link.download = `CV_${this.application?.firstName || 'candidat'}.pdf`;
      link.click();
    }
  }

  clearSuccessMessage(): void {
    setTimeout(() => {
      this.successMessage = null;
      this.cdr.detectChanges();
    }, 5000);
  }

  goBack(): void {
    this.router.navigate([
      '/commission-member/commissions',
      this.commissionId,
      'applications'
    ]);
  }
}
