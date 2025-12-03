import {ChangeDetectorRef, Component, OnInit} from '@angular/core';
import {CommonModule} from '@angular/common';
import {ContentHeader} from '../../shared/content-header/content-header';
import {ApiWrapperService} from '../../services/api-wrapper';
import { ActivatedRoute, Router } from '@angular/router';
import {PublicJobOfferListDto} from '../../api/models/public-job-offer-list-dto';
import {getAvailableJobOffers} from '../../api/functions';
import {Api} from '../../api/api';

interface JobOffer {
  id: number;
  jobType: string;
  jobTitle: string;
  description: string;
  skills: string[];
  dateLimite: string;
  datePublication: string;
  rating: number;
  experienceMin: string;
  niveauEtudeRequis: string;
  reviews: number;
  projects: number;
  totalSpent: string;
}

@Component({
  selector: 'app-offres-emploi',
  standalone: true,
  imports: [CommonModule, ContentHeader],
  templateUrl: './offres-emploi.html',
  styleUrls: ['./offres-emploi.css']
})
export class OffresEmploi implements OnInit {
  jobOffers: JobOffer[] = [];
  isLoading = false;
  error: string | null = null;

  constructor(private api: Api, private cdr: ChangeDetectorRef,private route: ActivatedRoute, private router: Router) {}

  ngOnInit() {
    this.loadJobOffers();
  }

  loadJobOffers() {
    this.isLoading = true;
    this.error = null;

    this.api.invoke(getAvailableJobOffers)
      .then(response => {
        console.log('Offres reçues :', response);

        this.jobOffers = this.mapToJobOffers(response);
        this.cdr.detectChanges();
        console.log('Offres mappee :', this.jobOffers);
      })
      .catch(error => {
        console.error('Erreur:', error);
        this.error = "Impossible de charger les offres d'emploi";
      })
      .finally(() => {
        this.isLoading = false;
      });

  }

  private mapToJobOffers(apiOffers: PublicJobOfferListDto[]): JobOffer[] {
    return apiOffers.map(offer => ({
      id: offer.id || 0,
      jobType: this.getCompanyFromJobType(offer.jobType),
      jobTitle: offer.jobTitle || 'Poste non spécifié',
      description: offer.description || '',
      skills: this.extractSkills(offer),
      niveauEtudeRequis: this.formatNiveauEtude(offer.niveauEtudeRequis),
      experienceMin: this.formatExperience(offer.experienceMin),
      rating: 0,
      dateLimite: this.formatDate(offer.dateLimite),
      datePublication: this.formatDate(offer.datePublication),
      reviews: offer.applicationCount || 0,
      projects: 0,
      totalSpent: this.formatContractType(offer.typeContrat)
    }));
  }

  private formatNiveauEtude(niveau: string | undefined): string {
    if (!niveau) {
      return 'Non spécifié';
    }
    return niveau.trim() || 'Non spécifié';
  }

  private formatExperience(experience: number | undefined): string {
    if (experience === undefined || experience === null) {
      return 'Non spécifiée';
    }

    if (experience === 0) {
      return 'Débutant accepté';
    }

    if (experience === 1) {
      return '1 an d\'expérience';
    }

    return `${experience} ans d\'expérience`;
  }

  private formatDate(dateString: string | undefined): string {
    if (!dateString) {
      return 'Non spécifiée';
    }

    try {
      const date = new Date(dateString);
      if (isNaN(date.getTime())) {
        return 'Date invalide';
      }

      return date.toLocaleDateString('fr-FR');
    } catch (error) {
      console.error('Erreur de formatage de date:', error);
      return 'Date invalide';
    }
  }

  private getCompanyFromJobType(jobType?: 'PER' | 'PATS' | 'CONTRACTUEL'): string {
    const mapping = {
      'PER': 'Personnel Enseignant-Recherche',
      'PATS': 'Personnel Administratif',
      'CONTRACTUEL': 'Contractuel'
    };
    return jobType ? mapping[jobType] : 'Non spécifié';
  }

  private extractSkills(offer: PublicJobOfferListDto): string[] {
    const skills: string[] = [];

    if (offer.niveauEtudeRequis) {
      skills.push(offer.niveauEtudeRequis);
    }

    if (offer.experienceMin !== undefined && offer.experienceMin > 0) {
      skills.push(`${offer.experienceMin} ans d'exp`);
    }

    if (offer.typeContrat) {
      skills.push(offer.typeContrat);
    }

    return skills.length > 0 ? skills : ['Aucune compétence spécifiée'];
  }

  private formatContractType(typeContrat?: 'CDD' | 'CDI' | 'STAGE'): string {
    const mapping = {
      'CDD': 'Contrat à Durée Déterminée',
      'CDI': 'Contrat à Durée Indéterminée',
      'STAGE': 'Stage'
    };
    return typeContrat ? mapping[typeContrat] : 'Non spécifié';
  }

  goToCandidater(id: number) {
    // const id = this.route.snapshot.params['id'];
    this.router.navigate([`/offres-emploi/${id}/apply`]);
  }
}
