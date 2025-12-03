import {ChangeDetectorRef, Component, inject, OnInit, PLATFORM_ID} from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { Api } from '../../api/api';
import {
  getCommissionById,
  addMember,
  updateCommission,
  getApplicationDetails,
  getAllCommissionMemberUsers
} from '../../api/functions';
import { CommissionResponseDto } from '../../api/models/commission-response-dto';
import { AddCommissionMemberDto } from '../../api/models/add-commission-member-dto';
import { UpdateCommissionDto } from '../../api/models/update-commission-dto';
import { FormsModule } from '@angular/forms';
import {CommonModule, NgIf, NgFor, NgClass, isPlatformBrowser} from '@angular/common';
import {CommissionMemberUserDto} from '../../api/models/commission-member-user-dto';

@Component({
  selector: 'app-commission-detail',
  imports: [
    FormsModule,
    CommonModule,
  ],
  templateUrl: './commission-detail-component.html',
  styleUrl: './commission-detail-component.css',
})
export class CommissionDetailComponent implements OnInit {
  private platformId = inject(PLATFORM_ID);
  commission: CommissionResponseDto | null = null;
  isLoading: boolean = true;
  errorMessage: string | null = null;
  successMessage: string | null = null;

  // Add Member Form
  showAddMemberForm: boolean = false;
  isAddingMember: boolean = false;
  newMember: AddCommissionMemberDto = {
    userId: 0,
    role: '',
    expertiseArea: ''
  };
  availableUsers: CommissionMemberUserDto[] = []; // À remplacer par le type approprié
  memberFormErrors: { [key: string]: string } = {};

  // Edit Mode
  isEditMode: boolean = false;
  editData: UpdateCommissionDto = {
    name: '',
    description: '',
    status: 'ACTIVE'
  };
  roleOptions = [
    { value: 'PRESIDENT', label: 'Président' },
    { value: 'MEMBER', label: 'Membre' }
  ];
  commissionId: number = 0;

  constructor(
    private api: Api,
    private route: ActivatedRoute,
    private cdr: ChangeDetectorRef,
    private router: Router
  ) {}

  ngOnInit(): void {
    if (isPlatformBrowser(this.platformId)) {
    this.route.params.subscribe(params => {
      this.commissionId = +params['commissionId'];
      if (this.commissionId) {
        setTimeout(() => {
          this.loadCommissionDetails();
          this.loadAvailableUsers();
        }, 100);
      }
    });}
  }
  async loadCommissionDetails(): Promise<void> {
    if (!this.commissionId) return;

    this.isLoading = true;
    this.errorMessage = null;

    try {
      const response = await this.api.invoke(getCommissionById, {
        commissionId: this.commissionId
      });

      this.commission = response || null;
      this.isLoading = false;
      console.log('✅ Détails de la candidature chargés', this.commission);

      this.cdr.detectChanges();

    } catch (error) {
      console.error('❌ Erreur chargement détails candidature', error);
      this.errorMessage = 'Impossible de charger les détails de la candidature.';
      this.isLoading = false;
      this.cdr.detectChanges();
    }
  }

  loadAvailableUsers(): void {
    // TODO: Implémenter le chargement de la liste des utilisateurs disponibles
    // Exemple:
    this.api.invoke(getAllCommissionMemberUsers, {})
      .then((response: any) => {
        this.availableUsers = response;
        console.log(response);
      })
      .catch((error: any) => {
        console.error('Erreur lors du chargement des utilisateurs', error);
      });

    // Données de test (à remplacer)
    // this.availableUsers = [
    //   { id: 1, name: 'Jean Dupont', email: 'jean.dupont@example.com' },
    //   { id: 2, name: 'Marie Martin', email: 'marie.martin@example.com' },
    //   { id: 3, name: 'Pierre Durand', email: 'pierre.durand@example.com' }
    // ];
  }

  toggleAddMemberForm(): void {
    this.showAddMemberForm = !this.showAddMemberForm;
    if (!this.showAddMemberForm) {
      this.resetMemberForm();
    }
  }

  validateMemberForm(): boolean {
    this.memberFormErrors = {};
    let isValid = true;

    if (!this.newMember.userId || this.newMember.userId === 0) {
      this.memberFormErrors['userId'] = 'Veuillez sélectionner un utilisateur';
      isValid = false;
    }

    if (!this.newMember.role || this.newMember.role.trim().length < 2) {
      this.memberFormErrors['role'] = 'Le rôle doit contenir au moins 2 caractères';
      isValid = false;
    }

    return isValid;
  }

  addMemberToCommission(): void {
    if (!this.validateMemberForm()) {
      return;
    }

    this.isAddingMember = true;
    this.errorMessage = null;
    this.newMember.userId = Number(this.newMember.userId);
    const params = {
      commissionId: this.commissionId,
      body: this.newMember
    };
    console.log('📤 Données envoyées:', params);
    console.log('📤 userId type:', typeof this.newMember.userId);
    console.log('📤 userId value:', this.newMember.userId);

    this.api.invoke(addMember, params)
      .then((response: any) => {
        console.log('✅ Membre ajouté avec succès', response);
        this.successMessage = 'Membre ajouté avec succès à la commission.';
        this.resetMemberForm();
        this.showAddMemberForm = false;
        this.loadCommissionDetails(); // Recharger les détails
        this.clearSuccessMessage();
      })
      .catch((error: any) => {
        console.error('❌ Erreur ajout membre', error);
        this.errorMessage = 'Une erreur est survenue lors de l\'ajout du membre.';
        this.isAddingMember = false;
      });
  }

  resetMemberForm(): void {
    this.newMember = {
      userId: 0,
      role: '',
      expertiseArea: ''
    };
    this.memberFormErrors = {};
    this.isAddingMember = false;
  }

  enableEditMode(): void {
    if (this.commission) {
      this.editData = {
        name: this.commission.name || '',
        description: this.commission.description || '',
        status: this.commission.status || 'ACTIVE'
      };
      this.isEditMode = true;
    }
  }

  cancelEdit(): void {
    this.isEditMode = false;
    this.editData = {
      name: '',
      description: '',
      status: 'ACTIVE'
    };
  }

  saveEdit(): void {
    if (!this.editData.name || this.editData.name.trim().length < 3) {
      this.errorMessage = 'Le nom de la commission doit contenir au moins 3 caractères';
      return;
    }

    const params = {
      commissionId: this.commissionId,
      body: this.editData
    };

    this.api.invoke(updateCommission, params)
      .then((response: CommissionResponseDto) => {
        console.log('✅ Commission mise à jour', response);
        this.commission = response;
        this.successMessage = 'Commission mise à jour avec succès.';
        this.isEditMode = false;
        this.clearSuccessMessage();
      })
      .catch((error: any) => {
        console.error('❌ Erreur mise à jour commission', error);
        this.errorMessage = 'Une erreur est survenue lors de la mise à jour.';
      });
  }

  getStatusLabel(status?: string): string {
    const labels: { [key: string]: string } = {
      'ACTIVE': 'Active',
      'CLOSED': 'Fermée',
      'ARCHIVED': 'Archivée'
    };
    return status ? labels[status] || status : 'N/A';
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

  clearSuccessMessage(): void {
    setTimeout(() => {
      this.successMessage = null;
    }, 5000);
  }

  goBack(): void {
    this.router.navigate(['/rh/commissions']);
  }
}
