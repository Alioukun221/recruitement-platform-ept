import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { HttpClient } from '@angular/common/http';
import {FormBuilder, FormGroup, ReactiveFormsModule, Validators} from '@angular/forms';
import {CandidateApplicationDetailDto} from '../../api/models/candidate-application-detail-dto';
import {Api} from '../../api/api';
import {getMyApplicationDetail} from '../../api/functions';
import {CommonModule, NgClass} from '@angular/common';
import {MatButtonModule} from '@angular/material/button';
import {MatIconModule} from '@angular/material/icon';

@Component({
  selector: 'app-candidature-details',
  templateUrl: './candidature-details-component.html',
  imports: [
    CommonModule,
    NgClass,
    ReactiveFormsModule,
    MatIconModule,
    MatButtonModule
  ],
  styleUrls: ['./candidature-details-component.css']
})
export class CandidatureDetailsComponent implements OnInit {
  applicationId!: number;
  application: CandidateApplicationDetailDto | undefined;
  isLoading = true;
  isEditMode = false;
  isSaving = false;

  editForm!: FormGroup;

  // Pour afficher les statuts avec style
  statusConfig: { [key: string]: { label: string; class: string; icon: string } } = {
    'SUBMITTED': { label: 'Soumise', class: 'status-submitted', icon: 'send' },
    'UNDER_REVIEW': { label: 'En cours d\'examen', class: 'status-review', icon: 'hourglass_empty' },
    'SHORTLISTED': { label: 'Présélectionné', class: 'status-shortlisted', icon: 'star' },
    'INTERVIEW_SCHEDULED': { label: 'Entretien planifié', class: 'status-interview', icon: 'event' },
    'ACCEPTED': { label: 'Acceptée', class: 'status-accepted', icon: 'check_circle' },
    'REJECTED': { label: 'Rejetée', class: 'status-rejected', icon: 'cancel' },
    'WITHDRAWN': { label: 'Retirée', class: 'status-withdrawn', icon: 'remove_circle' }
  };

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private http: HttpClient,
    private fb: FormBuilder,
    private apiService: Api
  ) {
    this.initForm();
  }

  ngOnInit(): void {
    // Récupérer l'ID depuis l'URL
    this.applicationId = Number(this.route.snapshot.paramMap.get('applicationId'));

    if (this.applicationId) {
      this.loadApplicationDetails();
    } else {
      console.error('❌ ID de candidature non trouvé dans l\'URL');
      this.router.navigate(['/candidate/my-applications']);
    }
  }

  initForm(): void {
    this.editForm = this.fb.group({
      firstName: ['', Validators.required],
      lastName: ['', Validators.required],
      email: ['', [Validators.required, Validators.email]],
      address: [''],
      highestDegree: ['', Validators.required],
      specialization: ['']
    });
  }

  loadApplicationDetails(): void {
    this.isLoading = true;

    getMyApplicationDetail(this.apiService.http, this.apiService.rootUrl, {
      applicationId: this.applicationId
    }).subscribe({
      next: (response) => {
        this.application = response.body;
        this.populateForm();
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

  populateForm(): void {
    if (this.application) {
      this.editForm.patchValue({
        firstName: this.application.firstName,
        lastName: this.application.lastName,
        email: this.application.email,
        address: this.application.address,
        highestDegree: this.application.highestDegree,
        specialization: this.application.specialization
      });
    }
  }

  toggleEditMode(): void {
    this.isEditMode = !this.isEditMode;

    if (!this.isEditMode) {
      // Si on annule, on recharge les données originales
      this.populateForm();
    }
  }

  saveChanges(): void {
    if (!this.editForm.valid) {
      alert('Veuillez remplir tous les champs obligatoires');
      return;
    }

    this.isSaving = true;
    const token = localStorage.getItem('auth_token');

    // Appel API pour mettre à jour la candidature
    this.http.put(
      `${this.apiService.rootUrl}/candidate/my-applications/${this.applicationId}`,
      this.editForm.value,
      {
        headers: {
          'Authorization': `Bearer ${token}`
        }
      }
    ).subscribe({
      next: () => {
        console.log('✅ Candidature mise à jour avec succès');
        alert('✅ Vos informations ont été mises à jour avec succès');
        this.isEditMode = false;
        this.isSaving = false;
        this.loadApplicationDetails(); // Recharger les données
      },
      error: (err) => {
        console.error('❌ Erreur lors de la mise à jour', err);
        alert('❌ Erreur lors de la mise à jour. Veuillez réessayer.');
        this.isSaving = false;
      }
    });
  }

  downloadCV(): void {
    if (this.application?.cvUrl) {
      window.open(this.application.cvUrl, '_blank');
    } else {
      alert('Aucun CV disponible');
    }
  }

  goBack(): void {
    this.router.navigate(['/candidate/my-applications']);
  }

  getStatusConfig(status: string | undefined) {
    return status ? this.statusConfig[status] || {
      label: status,
      class: 'status-default',
      icon: 'info'
    } : { label: 'Inconnu', class: 'status-default', icon: 'help' };
  }

  formatDate(dateString: string | undefined): string {
    if (!dateString) return 'Non spécifié';

    const date = new Date(dateString);
    return new Intl.DateTimeFormat('fr-FR', {
      day: '2-digit',
      month: 'long',
      year: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    }).format(date);
  }
}
