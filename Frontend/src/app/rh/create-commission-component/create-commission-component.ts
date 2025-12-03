import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { Api } from '../../api/api';
import {createCommission, getAllJobOffers} from '../../api/functions';
import { CreateCommissionDto } from '../../api/models/create-commission-dto';
import { FormsModule } from '@angular/forms';
import { CommonModule, NgIf } from '@angular/common';
import {JobOfferListDto} from '../../api/models/job-offer-list-dto';

@Component({
  selector: 'app-create-commission',
  imports: [
    FormsModule,
    CommonModule,
  ],
  templateUrl: './create-commission-component.html',
  styleUrl: './create-commission-component.css',
})
export class CreateCommissionComponent implements OnInit {
  commission: CreateCommissionDto = {
    name: '',
    jobOfferId: 0,
    description: ''
  };

  jobOffers: JobOfferListDto[] = []; // À remplacer par le type approprié
  isSubmitting: boolean = false;
  errorMessage: string | null = null;
  formErrors: { [key: string]: string } = {};

  constructor(
    private api: Api,
    private cdr: ChangeDetectorRef,
    private router: Router
  ) {}

  ngOnInit(): void {
    // Charger la liste des offres d'emploi disponibles
    this.loadJobOffers();
  }

  loadJobOffers(): void {
    // TODO: Implémenter le chargement des offres d'emploi
    // Exemple:
    this.api.invoke(getAllJobOffers, {})
      .then((response: any) => {
        this.jobOffers = response;
      })
      .catch((error: any) => {
        console.error('Erreur lors du chargement des offres', error);
      });

  }

  validateForm(): boolean {
    this.formErrors = {};
    let isValid = true;

    if (!this.commission.name || this.commission.name.trim().length < 3) {
      this.formErrors['name'] = 'Le nom de la commission doit contenir au moins 3 caractères';
      isValid = false;
    }

    if (!this.commission.jobOfferId || this.commission.jobOfferId === 0) {
      this.formErrors['jobOfferId'] = 'Veuillez sélectionner une offre d\'emploi';
      isValid = false;
    }

    return isValid;
  }

  clearFieldError(fieldName: string): void {
    delete this.formErrors[fieldName];
  }

  onSubmit(): void {
    if (!this.validateForm()) {
      this.errorMessage = 'Veuillez corriger les erreurs dans le formulaire';
      window.scrollTo({ top: 0, behavior: 'smooth' });
      return;
    }

    this.isSubmitting = true;
    this.errorMessage = null;

    const params = {
      body: this.commission
    };

    this.api.invoke(createCommission, params)
      .then((response: any) => {
        console.log('✅ Commission créée avec succès', response);
        // Redirection vers la page de détails de la commission créée
        if (response.id) {
          this.router.navigate(['/rh/commissions', response.id]);
        } else {
          this.router.navigate(['/rh/commissions']);
        }
      })
      .catch((error: any) => {
        console.error('❌ Erreur création commission', error);
        this.errorMessage = 'Une erreur est survenue lors de la création de la commission. Veuillez réessayer.';
        this.isSubmitting = false;
        window.scrollTo({ top: 0, behavior: 'smooth' });
      });
  }

  cancel(): void {
    const confirmCancel = confirm('Êtes-vous sûr de vouloir annuler ? Les données non enregistrées seront perdues.');
    if (confirmCancel) {
      this.router.navigate(['/rh/commissions']);
    }
  }
}
