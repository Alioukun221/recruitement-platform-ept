import {Component, OnInit, inject, PLATFORM_ID, ChangeDetectorRef} from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { DomSanitizer, SafeResourceUrl } from '@angular/platform-browser';
import { Api } from '../../api/api';
import {evaluateApplication, viewCv, getApplicationDetails, getApplicationDetails1} from '../../api/functions';
import { CreateEvaluationDto } from '../../api/models/create-evaluation-dto';
import { ApplicationDetailDto } from '../../api/models/application-detail-dto';
import { FormsModule } from '@angular/forms';
import { CommonModule, isPlatformBrowser } from '@angular/common';

@Component({
  selector: 'app-evaluate-application',
  imports: [
    FormsModule,
    CommonModule,
  ],
  templateUrl: './evaluate-application-component.html',
  styleUrl: './evaluate-application-component.css',
})
export class EvaluateApplicationComponent implements OnInit {
  private platformId = inject(PLATFORM_ID);

  application: ApplicationDetailDto | null = null;
  isLoading: boolean = true;
  isLoadingCV: boolean = true;
  isSubmitting: boolean = false;
  errorMessage: string | null = null;
  successMessage: string | null = null;
  cvError: string | null = null;
  cvUrl: SafeResourceUrl | null = null;

  commissionId: number = 0;
  applicationId: number = 0;

  evaluation: CreateEvaluationDto = {
    diplomaScore: 0,
    experienceScore: 0,
    competenceScore: 0,
    motivationScore: 0,
    softSkillsScore: 0,
    comment: ''
  };

  formErrors: { [key: string]: string } = {};

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
          this.loadApplicationDetails();
          this.loadCV();
        }
      });
    }
  }

  async loadApplicationDetails(): Promise<void> {
    this.isLoading = true;
    this.errorMessage = null;

    try {
      const response = await this.api.invoke(getApplicationDetails1, {
        applicationId: this.applicationId,
        commissionId: this.commissionId,
      });

      this.application = response;
      this.isLoading = false;
      this.cdr.detectChanges();
      console.log('✅ Candidature chargée', response);
      console.log('📊 État actuel - isLoading:', this.isLoading, 'application:', !!this.application);

    } catch (error: any) {
      console.error('❌ Erreur chargement candidature', error);
      this.errorMessage = 'Impossible de charger les détails de la candidature.';
      this.isLoading = false;
      this.cdr.detectChanges();
    }
  }

  async loadCV(): Promise<void> {
    this.isLoadingCV = true;
    this.cvError = null;

    try {
      const response = await this.api.invoke(viewCv, {
        commissionId: this.commissionId,
        applicationId: this.applicationId
      });

      // Créer un Blob URL pour le PDF
      const blob = new Blob([response as any], { type: 'application/pdf' });
      const url = URL.createObjectURL(blob);
      this.cvUrl = this.sanitizer.bypassSecurityTrustResourceUrl(url);
      console.log(this.cvUrl);

      this.isLoadingCV = false;
      console.log('✅ CV chargé');

    } catch (error: any) {
      console.error('❌ Erreur chargement CV', error);
      this.cvError = 'Impossible de charger le CV.';
      this.isLoadingCV = false;
    }
  }

  validateForm(): boolean {
    this.formErrors = {};
    let isValid = true;

    // Valider que tous les scores sont entre 0 et 20
    const scores = [
      { key: 'diplomaScore', value: this.evaluation.diplomaScore },
      { key: 'experienceScore', value: this.evaluation.experienceScore },
      { key: 'competenceScore', value: this.evaluation.competenceScore },
      { key: 'motivationScore', value: this.evaluation.motivationScore },
      { key: 'softSkillsScore', value: this.evaluation.softSkillsScore }
    ];

    scores.forEach(score => {
      if (score.value < 0 || score.value > 20) {
        this.formErrors[score.key] = 'Le score doit être entre 0 et 20';
        isValid = false;
      }
    });

    return isValid;
  }

  async submitEvaluation(): Promise<void> {
    if (!this.validateForm()) {
      this.errorMessage = 'Veuillez corriger les erreurs dans le formulaire.';
      return;
    }

    const confirmSubmit = confirm(
      `Êtes-vous sûr de vouloir soumettre cette évaluation ?\n\nScore total: ${this.getTotalScore()}/100\n\nCette action est définitive.`
    );

    if (!confirmSubmit) {
      return;
    }

    this.isSubmitting = true;
    this.errorMessage = null;
    this.successMessage = null;

    try {
      const params = {
        commissionId: this.commissionId,
        applicationId: this.applicationId,
        body: this.evaluation
      };

      const response = await this.api.invoke(evaluateApplication, params);

      console.log('✅ Évaluation soumise avec succès', response);
      this.successMessage = 'Évaluation soumise avec succès !';

      // Rediriger vers la page de détails après 2 secondes

        await this.router.navigate([
          '/commissions',
          this.commissionId,
          'applications',
          this.applicationId,
          'evaluation'
        ]);

    } catch (error: any) {
      console.error('❌ Erreur soumission évaluation', error);
      this.errorMessage = 'Une erreur est survenue lors de la soumission de l\'évaluation.';
      this.isSubmitting = false;
    }
  }

  getTotalScore(): number {
    return (
      this.evaluation.diplomaScore +
      this.evaluation.experienceScore +
      this.evaluation.competenceScore +
      this.evaluation.motivationScore +
      this.evaluation.softSkillsScore
    );
  }

  getTotalScoreClass(): string {
    const total = this.getTotalScore();
    if (total >= 80) return 'excellent';
    if (total >= 60) return 'good';
    if (total >= 40) return 'average';
    return 'low';
  }

  getScoreClass(score?: number): string {
    if (!score) return '';
    if (score >= 80) return 'score-excellent';
    if (score >= 60) return 'score-good';
    if (score >= 40) return 'score-average';
    return 'score-low';
  }

  downloadCV(): void {
    if (this.cvUrl) {
      // Extraire l'URL du SafeResourceUrl
      const url = (this.cvUrl as any).changingThisBreaksApplicationSecurity;
      const link = document.createElement('a');
      link.href = url;
      link.download = `CV_${this.application?.firstName || 'candidat'}.pdf`;
      link.click();
    }
  }

  goBack(): void {
    this.router.navigate(['/commission-member/commissions', this.commissionId, 'applications']);
  }
}
