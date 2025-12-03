import {ChangeDetectorRef, Component, OnInit} from '@angular/core';
import {CreateJobOfferDto} from '../../api/models/create-job-offer-dto';
import {Router} from '@angular/router';
import {Api} from '../../api/api';
import {createJobOffer} from '../../api/functions';
import {FormsModule} from '@angular/forms';
import {CommonModule, NgIf} from '@angular/common';

@Component({
  selector: 'app-create-job-offer-component',
  imports: [
    FormsModule,
    NgIf,
    CommonModule,
  ],
  templateUrl: './create-job-offer-component.html',
  styleUrl: './create-job-offer-component.css',
})

export class CreateJobOfferComponent implements OnInit {
  jobOffer: CreateJobOfferDto = {
    jobTitle: '',
    jobType: 'PER',
    typeContrat: 'CDI',
    description: '',
    requiredSkills: '',
    niveauEtudeRequis: '',
    experienceMin: 0,
    dateLimite: ''
  };

  skillInput: string = '';
  isSubmitting: boolean = false;
  errorMessage: string | null = null;
  formErrors: { [key: string]: string } = {};

  jobTypeOptions = [
    { value: 'PER', label: 'Personnel Enseignant et de Recherche (PER)' },
    { value: 'PATS', label: 'Personnel Administratif, Technique et de Service (PATS)' },
    { value: 'CONTRACTUEL', label: 'Contractuel' }
  ];

  contractTypeOptions = [
    { value: 'CDI', label: 'Contrat à Durée Indéterminée (CDI)' },
    { value: 'CDD', label: 'Contrat à Durée Déterminée (CDD)' },
    { value: 'STAGE', label: 'Stage' }
  ];

  educationLevels = [
    'Baccalauréat',
    'Bac +2 (BTS, DUT, DEUG)',
    'Licence (Bac +3)',
    'Master 1 (Bac +4)',
    'Master 2 (Bac +5)',
    'Doctorat (Bac +8)',
    'Ingénieur',
    'Autre'
  ];

  constructor(
    private api: Api,
    private cdr: ChangeDetectorRef,
    private router: Router
  ) {}

  ngOnInit(): void {
    // Initialisation
  }

  addSkill(): void {
    const trimmedSkill = this.skillInput.trim();

    if (!trimmedSkill) return;

    // Convertir la chaîne en tableau, ajouter la nouvelle compétence, puis reconvertir
    const skillsArray = this.jobOffer.requiredSkills
      ? this.jobOffer.requiredSkills.split(';').filter(s => s.trim())
      : [];

    // Vérifier si la compétence existe déjà
    if (!skillsArray.includes(trimmedSkill)) {
      skillsArray.push(trimmedSkill);
      this.jobOffer.requiredSkills = skillsArray.join(';');
      this.skillInput = '';
      this.clearFieldError('requiredSkills');
    }
  }

  removeSkill(skill: string): void {
    // Convertir la chaîne en tableau, filtrer, puis reconvertir
    const skillsArray = this.jobOffer.requiredSkills
      ? this.jobOffer.requiredSkills.split(';').filter(s => s.trim())
      : [];

    const updatedSkills = skillsArray.filter(s => s !== skill);
    this.jobOffer.requiredSkills = updatedSkills.join(';');
  }
  getSkillsArray(): string[] {
    return this.jobOffer.requiredSkills
      ? this.jobOffer.requiredSkills.split(';').filter(s => s.trim())
      : [];
  }
  onSkillKeyPress(event: KeyboardEvent): void {
    if (event.key === 'Enter') {
      event.preventDefault();
      this.addSkill();
    }
  }

  validateForm(): boolean {
    this.formErrors = {};
    let isValid = true;

    if (!this.jobOffer.jobTitle || this.jobOffer.jobTitle.trim().length < 3) {
      this.formErrors['jobTitle'] = 'Le titre du poste doit contenir au moins 3 caractères';
      isValid = false;
    }

    if (!this.jobOffer.description || this.jobOffer.description.trim().length < 50) {
      this.formErrors['description'] = 'La description doit contenir au moins 50 caractères';
      isValid = false;
    }

    if (!this.jobOffer.requiredSkills || this.getSkillsArray().length === 0) {
      this.formErrors['requiredSkills'] = 'Veuillez ajouter au moins une compétence requise';
      isValid = false;
    }

    if (!this.jobOffer.niveauEtudeRequis) {
      this.formErrors['niveauEtudeRequis'] = 'Le niveau d\'étude est obligatoire';
      isValid = false;
    }

    if (this.jobOffer.experienceMin < 0) {
      this.formErrors['experienceMin'] = 'L\'expérience minimale ne peut pas être négative';
      isValid = false;
    }

    if (!this.jobOffer.dateLimite) {
      this.formErrors['dateLimite'] = 'La date limite est obligatoire';
      isValid = false;
    } else {
      const selectedDate = new Date(this.jobOffer.dateLimite);
      const today = new Date();
      today.setHours(0, 0, 0, 0);

      if (selectedDate < today) {
        this.formErrors['dateLimite'] = 'La date limite ne peut pas être dans le passé';
        isValid = false;
      }
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
      body: this.jobOffer
    };

    this.api.invoke(createJobOffer, params)
      .then((response: any) => {
        console.log('✅ Offre créée avec succès', response);
        // Redirection vers la page de détails de l'offre créée
        if (response.id) {
          this.router.navigate(['/rh/job-offers', response.id]);
        } else {
          this.router.navigate(['/rh/job-offers']);
        }
      })
      .catch((error: any) => {
        console.error('❌ Erreur création offre', error);
        this.errorMessage = 'Une erreur est survenue lors de la création de l\'offre. Veuillez réessayer.';
        this.isSubmitting = false;
        window.scrollTo({ top: 0, behavior: 'smooth' });
      });
  }

  cancel(): void {
    const confirmCancel = confirm('Êtes-vous sûr de vouloir annuler ? Les données non enregistrées seront perdues.');
    if (confirmCancel) {
      this.router.navigate(['/rh/job-offers']);
    }
  }

  getTodayDate(): string {
    const today = new Date();
    const year = today.getFullYear();
    const month = String(today.getMonth() + 1).padStart(2, '0');
    const day = String(today.getDate()).padStart(2, '0');
    return `${year}-${month}-${day}`;
  }
}
