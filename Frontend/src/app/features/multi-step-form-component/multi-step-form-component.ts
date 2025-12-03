import { Component, OnInit, Input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { Router, ActivatedRoute } from '@angular/router';
import { Api } from '../../api/api';
import { submitApplication } from "../../api/fn/candidats/submit-application";
import {HttpClient} from '@angular/common/http';
import {ApplicationSubmissionResponseDto} from '../../api/models/application-submission-response-dto';

interface Step {
  number: number;
  title: string;
  subtitle: string;
}

interface FileUpload {
  file: File;
  id: string;
  progress: number;
  status: 'uploading' | 'completed' | 'error';
  errorMessage?: string;
}

@Component({
  selector: 'app-multi-step-form',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: 'multi-step-form-component.html',
  styleUrls: ['multi-step-form-component.css']
})
export class MultiStepFormComponent implements OnInit {
  steps: Step[] = [
    { number: 1, title: 'Personal Info', subtitle: 'Basic details' },
    { number: 2, title: 'Education', subtitle: 'Academic background' },
    { number: 3, title: 'Documents', subtitle: 'Upload files' },
    { number: 4, title: 'Motivation', subtitle: 'Your goals' },
    { number: 5, title: 'Confirmation', subtitle: 'Review & submit' }
  ];

  currentStep = 1;
  totalSteps = 5;

  step1Form: FormGroup;
  step2Form: FormGroup;
  step4Form: FormGroup;
  step5Form: FormGroup;

  completedSteps: Set<number> = new Set();
  files: FileUpload[] = [];
  isDragging = false;
  isSubmitting = false;

  // ID de l'offre d'emploi (peut être passé via @Input ou récupéré depuis la route)
  @Input() jobOfferId?: number;

  // Configuration
  readonly MAX_FILE_SIZE = 5 * 1024 * 1024; // 5 Mo
  readonly ALLOWED_EXTENSIONS = ['.pdf', '.docx', '.png'];

  // Options pour les dropdowns
  degreeOptions = [
    'High School Diploma',
    'Associate Degree',
    'Bachelor\'s Degree',
    'Master\'s Degree',
    'Doctorate (PhD)',
    'Professional Degree'
  ];

  yearOptions: number[] = [];

  constructor(
    private fb: FormBuilder,
    private api: Api,
    private http: HttpClient,
    private router: Router,
    private route: ActivatedRoute
  ) {
    // Initialiser les formulaires
    this.step1Form = this.fb.group({
      lastName: ['', Validators.required],
      firstName: ['', Validators.required],
      email: ['', [Validators.required, Validators.email]],
      phoneNumber: ['', Validators.required],
      address: ['', Validators.required],
      nationality: ['', Validators.required]
    });

    this.step2Form = this.fb.group({
      highestDegree: ['', Validators.required],
      majorField: ['', Validators.required],
      educationalInstitution: ['', Validators.required],
      yearOfGraduation: ['', Validators.required]
    });

    this.step4Form = this.fb.group({
      motivationEcole: ['', Validators.required],
      motivationPosition: ['', Validators.required],
      availableImmediately: [true, Validators.required]
    });

    this.step5Form = this.fb.group({
      certifyAccurate: [false, Validators.requiredTrue],
      consentGDPR: [false, Validators.requiredTrue],
      electronicSignature: ['', Validators.required]
    });
  }

  ngOnInit(): void {
    this.generateYearOptions();

    // Récupérer l'ID de l'offre depuis la route si non fourni via @Input
    if (!this.jobOfferId) {
      this.route.params.subscribe(params => {
        this.jobOfferId = +params['id'];
      });
    }
  }

  generateYearOptions(): void {
    const currentYear = new Date().getFullYear();
    for (let year = currentYear + 5; year >= 1970; year--) {
      this.yearOptions.push(year);
    }
  }

  get progressPercentage(): number {
    return (this.currentStep / this.totalSteps) * 100;
  }

  // Vérifier si une étape est complétée
  isStepCompleted(stepNumber: number): boolean {
    return this.completedSteps.has(stepNumber);
  }

  // Navigation vers une étape spécifique (via le stepper header)
  navigateToStep(stepNumber: number): void {
    if (stepNumber < this.currentStep) {
      // On peut toujours revenir en arrière
      this.currentStep = stepNumber;
      window.scrollTo({ top: 0, behavior: 'smooth' });
    } else if (stepNumber === this.currentStep + 1) {
      // Vérifier si l'étape actuelle est valide avant d'avancer
      this.nextStep();
    } else {
      // Pour sauter des étapes, vérifier que toutes les étapes intermédiaires sont complétées
      let canNavigate = true;
      for (let i = this.currentStep; i < stepNumber; i++) {
        if (!this.isStepCompleted(i)) {
          canNavigate = false;
          break;
        }
      }
      if (canNavigate) {
        this.currentStep = stepNumber;
        window.scrollTo({ top: 0, behavior: 'smooth' });
      }
    }
  }

  // Valider l'étape actuelle
  validateCurrentStep(): boolean {
    switch (this.currentStep) {
      case 1:
        if (this.step1Form.valid) {
          this.completedSteps.add(1);
          return true;
        }
        this.markFormGroupTouched(this.step1Form);
        return false;

      case 2:
        if (this.step2Form.valid) {
          this.completedSteps.add(2);
          return true;
        }
        this.markFormGroupTouched(this.step2Form);
        return false;

      case 3:
        // Vérifier qu'au moins le CV est uploadé
        const completedFiles = this.files.filter(f => f.status === 'completed');
        if (completedFiles.length > 0) {
          this.completedSteps.add(3);
          return true;
        }
        alert('Veuillez uploader au moins votre CV avant de continuer.');
        return false;

      case 4:
        if (this.step4Form.valid) {
          this.completedSteps.add(4);
          return true;
        }
        this.markFormGroupTouched(this.step4Form);
        return false;

      case 5:
        if (this.step5Form.valid) {
          this.completedSteps.add(5);
          return true;
        }
        this.markFormGroupTouched(this.step5Form);
        return false;

      default:
        return true;
    }
  }

  markFormGroupTouched(formGroup: FormGroup): void {
    Object.keys(formGroup.controls).forEach(key => {
      formGroup.get(key)?.markAsTouched();
    });
  }

  // Navigation
  nextStep(): void {
    if (this.validateCurrentStep() && this.currentStep < this.totalSteps) {
      this.currentStep++;
      window.scrollTo({ top: 0, behavior: 'smooth' });
    }
  }

  previousStep(): void {
    if (this.currentStep > 1) {
      this.currentStep--;
      window.scrollTo({ top: 0, behavior: 'smooth' });
    }
  }

  // Gestion des fichiers
  onFileSelect(event: Event): void {
    const input = event.target as HTMLInputElement;
    if (input.files) {
      this.handleFiles(Array.from(input.files));
    }
  }

  onDragOver(event: DragEvent): void {
    event.preventDefault();
    event.stopPropagation();
    this.isDragging = true;
  }

  onDragLeave(event: DragEvent): void {
    event.preventDefault();
    event.stopPropagation();
    this.isDragging = false;
  }

  onDrop(event: DragEvent): void {
    event.preventDefault();
    event.stopPropagation();
    this.isDragging = false;

    const files = event.dataTransfer?.files;
    if (files) {
      this.handleFiles(Array.from(files));
    }
  }

  private handleFiles(files: File[]): void {
    files.forEach(file => {
      const validation = this.validateFile(file);

      if (!validation.valid) {
        this.files.push({
          file,
          id: this.generateId(),
          progress: 0,
          status: 'error',
          errorMessage: validation.error
        });
      } else {
        const fileUpload: FileUpload = {
          file,
          id: this.generateId(),
          progress: 0,
          status: 'uploading'
        };
        this.files.push(fileUpload);
        this.simulateUpload(fileUpload);
      }
    });
  }

  private validateFile(file: File): { valid: boolean; error?: string } {
    // Vérifier la taille
    if (file.size > this.MAX_FILE_SIZE) {
      return { valid: false, error: `Fichier trop volumineux (max. 5 Mo)` };
    }

    // Vérifier l'extension
    const extension = '.' + file.name.split('.').pop()?.toLowerCase();
    if (!this.ALLOWED_EXTENSIONS.includes(extension)) {
      return { valid: false, error: 'Format non autorisé (PDF, DOCX, PNG uniquement)' };
    }

    return { valid: true };
  }

  private simulateUpload(fileUpload: FileUpload): void {
    const interval = setInterval(() => {
      if (fileUpload.progress < 100) {
        fileUpload.progress += 10;
      } else {
        fileUpload.status = 'completed';
        clearInterval(interval);
      }
    }, 200);
  }

  removeFile(id: string): void {
    this.files = this.files.filter(f => f.id !== id);
  }

  formatFileSize(bytes: number): string {
    if (bytes < 1024) return bytes + ' o';
    if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(1) + ' Ko';
    return (bytes / (1024 * 1024)).toFixed(1) + ' Mo';
  }

  private generateId(): string {
    return Math.random().toString(36).substr(2, 9);
  }

  isFieldInvalid(form: FormGroup, fieldName: string): boolean {
    const field = form.get(fieldName);
    return !!(field && field.invalid && field.touched);
  }

  // ✅ SOUMISSION DE LA CANDIDATURE
  async submitApplication(): Promise<void> {
    // Validation finale
    if (!this.step5Form.valid) {
      this.markFormGroupTouched(this.step5Form);
      alert('Veuillez remplir tous les champs obligatoires avant de soumettre.');
      return;
    }

    // Vérifier l'ID de l'offre
    if (!this.jobOfferId) {
      alert('Erreur: ID de l\'offre d\'emploi non trouvé.');
      return;
    }

    // Récupérer les fichiers complétés
    const completedFiles = this.files.filter(f => f.status === 'completed');

    if (completedFiles.length === 0) {
      alert('Veuillez uploader au moins votre CV.');
      return;
    }

    // Séparer le CV (premier fichier) et les documents additionnels
    const cvFile = completedFiles[0].file;
    const additionalDocuments = completedFiles.slice(1).map(f => f.file);

    // Préparer les paramètres pour l'API
    const requestParams = {
      jobOfferId: this.jobOfferId,

      // Étape 1: Informations personnelles
      firstName: this.step1Form.value.firstName,
      lastName: this.step1Form.value.lastName,
      email: this.step1Form.value.email,
      phoneNumber: this.step1Form.value.phoneNumber,
      address: this.step1Form.value.address,
      nationality: this.step1Form.value.nationality,

      // Étape 2: Formation académique
      highestDegree: this.step2Form.value.highestDegree,
      majorField: this.step2Form.value.majorField,
      educationalInstitution: this.step2Form.value.educationalInstitution,
      yearOfGraduation: this.step2Form.value.yearOfGraduation,

      // Étape 3: Documents
      body: {
        cv: cvFile
      },
      documents: additionalDocuments.length > 0 ? additionalDocuments : undefined,

      // Étape 4: Motivation
      motivationEcole: this.step4Form.value.motivationEcole,
      motivationPosition: this.step4Form.value.motivationPosition,
      availableImmediately: this.step4Form.value.availableImmediately,

      // Étape 5: Confirmation
      certifyAccurate: this.step5Form.value.certifyAccurate,
      consentGDPR: this.step5Form.value.consentGDPR,
      electronicSignature: this.step5Form.value.electronicSignature
    };

    console.log("📤 Envoi de la candidature...", {
      ...requestParams,
      body: { cv: cvFile.name },
      documents: additionalDocuments.map(d => d.name)
    });

    this.isSubmitting = true;

    try {
      // // ✅ Appel à l'API via le service généré

      // const response = await this.api.invoke$Response(submitApplication, requestParams);
      // console.log('🚀 AVANT appel API - Token:', localStorage.getItem('auth_token'));
      // console.log('✅ Candidature soumise avec succès', response);
      //
      // alert('🎉 Votre candidature a été soumise avec succès !');

      const formData = new FormData();

// Étape 1 : infos perso
      formData.append('firstName', this.step1Form.value.firstName);
      formData.append('lastName', this.step1Form.value.lastName);
      formData.append('email', this.step1Form.value.email);
      formData.append('phoneNumber', this.step1Form.value.phoneNumber);
      formData.append('address', this.step1Form.value.address);
      formData.append('nationality', this.step1Form.value.nationality);

// Étape 2 : formation
      formData.append('highestDegree', this.step2Form.value.highestDegree);
      formData.append('majorField', this.step2Form.value.majorField);
      formData.append('educationalInstitution', this.step2Form.value.educationalInstitution);
      formData.append('yearOfGraduation', this.step2Form.value.yearOfGraduation);

// Étape 3 : fichiers
      const cvFile = this.files[0].file;
      formData.append('cv', cvFile);
      this.files.slice(1).forEach((file, index) => {
        formData.append('documents', file.file);
      });

// Étape 4 : motivation
      formData.append('motivationEcole', this.step4Form.value.motivationEcole);
      formData.append('motivationPosition', this.step4Form.value.motivationPosition);
      formData.append('availableImmediately', String(this.step4Form.value.availableImmediately));

// Étape 5 : confirmation
      formData.append('certifyAccurate', String(this.step5Form.value.certifyAccurate));
      formData.append('consentGDPR', String(this.step5Form.value.consentGDPR));
      formData.append('electronicSignature', this.step5Form.value.electronicSignature);

// Envoi via HttpClient
      const token = localStorage.getItem('auth_token');
      console.log('🔑 Token récupéré:', token ? `token exist` : 'AUCUN TOKEN');

      this.http.post<ApplicationSubmissionResponseDto>(
        `${this.api.rootUrl}/candidate/job-offers/${this.jobOfferId}/apply`,
        formData,
        {
          headers: {
            'Authorization': `Bearer ${token}`
          }
        }
      ).subscribe({
        next: (response) => {
          console.log('✅ Candidature soumise', response.id);

          // ✅ Maintenant TypeScript connaît le type de 'response' et 'response.id' est accessible
          this.router.navigate(['candidate', 'my-applications', response.id]);
        },
        error: err => console.error('❌ Erreur', err)
      });

      // Réinitialiser le formulaire
      // this.resetForm();

      // Rediriger vers la page des candidatures
      //

    } catch (error: any) {
      console.error('❌ Erreur lors de la soumission', error);

      let errorMessage = 'Une erreur est survenue lors de la soumission.';

      // Gestion des erreurs selon le code HTTP
      if (error.status) {
        switch (error.status) {
          case 400:
            errorMessage = 'Données invalides. Veuillez vérifier vos informations.';
            break;
          case 401:
            errorMessage = 'Vous devez être connecté pour postuler.';
            this.router.navigate(['/login']);
            break;
          case 403:
            errorMessage = 'Accès refusé. Vous n\'avez pas les permissions nécessaires.';
            break;
          case 404:
            errorMessage = 'Offre d\'emploi introuvable.';
            break;
          case 409:
            errorMessage = 'Vous avez déjà postulé à cette offre.';
            break;
          case 413:
            errorMessage = 'Un ou plusieurs fichiers sont trop volumineux (max 5 Mo par fichier).';
            break;
          case 500:
            errorMessage = 'Erreur serveur. Veuillez réessayer plus tard.';
            break;
          default:
            errorMessage = `Erreur ${error.status}: ${error.message || 'Erreur inconnue'}`;
        }
      }

      alert(errorMessage);

      // Afficher les détails de l'erreur en console si disponibles
      if (error.error?.errors) {
        console.error("Détails de l'erreur backend:", error.error.errors);
      }

    } finally {
      this.isSubmitting = false;
    }
  }

  private resetForm(): void {
    this.currentStep = 1;
    this.completedSteps.clear();
    this.step1Form.reset();
    this.step2Form.reset();
    this.step4Form.reset();
    this.step5Form.reset();
    this.files = [];
  }
}
