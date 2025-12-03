import { ChangeDetectorRef, Component, inject, OnInit, PLATFORM_ID } from '@angular/core';
import { Router } from '@angular/router';
import { Api } from '../../api/api';
import { getAllCommissions, deleteCommission } from '../../api/functions';
import { CommissionListDto } from '../../api/models/commission-list-dto';
import { isPlatformBrowser, NgClass, NgForOf, NgIf } from '@angular/common';

@Component({
  selector: 'app-commissions-list',
  imports: [
    NgClass,
    NgIf,
    NgForOf
  ],
  templateUrl: './commissions-list-component.html',
  styleUrl: './commissions-list-component.css',
})
export class CommissionsListComponent implements OnInit {
  private platformId = inject(PLATFORM_ID);
  commissions: CommissionListDto[] = [];
  isLoading: boolean = true;
  errorMessage: string | null = null;

  constructor(
    private api: Api,
    private cdr: ChangeDetectorRef,
    private router: Router
  ) {}

  ngOnInit(): void {
    if (isPlatformBrowser(this.platformId)) {
      setTimeout(() => {
        this.loadCommissions();
      }, 100);
    }
  }

  async loadCommissions(status?: 'ACTIVE' | 'CLOSED' | 'ARCHIVED'): Promise<void> {
    this.isLoading = true;
    this.errorMessage = null;

    try {
      const params = status ? { status } : {};
      const response = await this.api.invoke(getAllCommissions, params);

      this.commissions = response || [];
      this.isLoading = false;
      console.log('✅ Commissions chargées', this.commissions);

      this.cdr.detectChanges();

    } catch (error) {
      console.error('❌ Erreur chargement commissions', error);
      this.errorMessage = 'Impossible de charger les commissions pour le moment.';
      this.isLoading = false;
      this.cdr.detectChanges();
    }
  }

  getStatusLabel(status?: string): string {
    const statusMap: { [key: string]: string } = {
      'ACTIVE': 'Active',
      'CLOSED': 'Fermée',
      'ARCHIVED': 'Archivée'
    };
    return status ? statusMap[status] || status : 'En attente';
  }

  formatDate(date?: string): string {
    if (!date) return '—';
    const d = new Date(date);
    return d.toLocaleDateString('fr-FR', {
      day: '2-digit',
      month: '2-digit',
      year: 'numeric'
    });
  }

  goToCommissionDetail(commissionId?: number): void {
    if (commissionId) {
      this.router.navigate(['/rh/commissions', commissionId]);
    }
  }

  editCommission(commission: CommissionListDto, event: Event): void {
    event.stopPropagation();
    if (commission.id) {
      this.router.navigate(['/rh/commissions', commission.id, 'edit']);
    }
  }

  async deleteCommission(commission: CommissionListDto, event: Event): Promise<void> {
    event.stopPropagation();

    if (!commission.id) return;

    const confirmDelete = confirm(
      `Êtes-vous sûr de vouloir supprimer la commission "${commission.name}" ?\n\nCette action est irréversible.`
    );

    if (confirmDelete) {
      try {
        await this.api.invoke(deleteCommission, { commissionId: commission.id });
        console.log('✅ Commission supprimée', commission.id);

        // Recharger la liste
        await this.loadCommissions();

        alert('Commission supprimée avec succès !');
      } catch (error) {
        console.error('❌ Erreur lors de la suppression', error);
        alert('Erreur lors de la suppression de la commission.');
      }
    }
  }

  createNewCommission(): void {
    this.router.navigate(['/rh/commissions/create']);
  }

  getActiveCommissionsCount(): number {
    return this.commissions.filter(c => c.status === 'ACTIVE').length;
  }
}
