import {ChangeDetectorRef, Component, inject, OnInit, PLATFORM_ID} from '@angular/core';
import {MemberCommissionDto} from '../../api/models/member-commission-dto';
import {Api} from '../../api/api';
import {Router} from '@angular/router';
import {getMyCommissions, getMyEvaluation} from '../../api/functions';
import {CommonModule, isPlatformBrowser} from '@angular/common';
import {FormsModule} from '@angular/forms';

@Component({
  selector: 'app-mes-commissions-components',
  imports: [CommonModule, FormsModule],
  templateUrl: './mes-commissions-components.html',
  styleUrl: './mes-commissions-components.css',
})
export class MesCommissionsComponent implements OnInit {
  private platformId = inject(PLATFORM_ID);
  commissions: MemberCommissionDto[] = [];
  filteredCommissions: MemberCommissionDto[] = [];
  isLoading = false;
  error: string | null = null;

  searchTerm = '';
  selectedRole = 'all';

  constructor(
    private api: Api,
    private router: Router,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit() {

    if (isPlatformBrowser(this.platformId)) {
      this.loadCommissions();
    }
  }

  async loadCommissions(): Promise<void> {
    this.isLoading = true;
    this.error = null;

    try {
      const response = await this.api.invoke(getMyCommissions);
      console.log('Commissions reçues :', response);
      this.commissions = response;
      this.filteredCommissions = response;
      this.isLoading = false;
      this.cdr.detectChanges();

    } catch (error: any) {
      console.error('Erreur:', error);
      this.error = "Impossible de charger les commissions";
      this.isLoading = false;
      this.cdr.detectChanges();
    }
  }

  onSearchChange(term: string) {
    this.searchTerm = term;
    this.applyFilters();
  }

  onRoleChange(role: string) {
    this.selectedRole = role;
    this.applyFilters();
  }

  applyFilters() {
    this.filteredCommissions = this.commissions.filter(commission => {
      const matchesSearch =
        commission.jobOfferTitle?.toLowerCase().includes(this.searchTerm.toLowerCase()) ||
        commission.commissionName?.toLowerCase().includes(this.searchTerm.toLowerCase());

      const matchesRole = this.selectedRole === 'all' || commission.myRole === this.selectedRole;

      return matchesSearch && matchesRole;
    });
  }

  getProgressPercentage(commission: MemberCommissionDto): number {
    if (!commission.shortlistedCandidatesCount || commission.shortlistedCandidatesCount === 0) {
      return 0;
    }
    return Math.min(
      Math.round(((commission.myEvaluationsCount || 0) / commission.shortlistedCandidatesCount) * 100),
      100
    );
  }

  navigateToCommission(commissionId?: number) {
    if (commissionId) {
      this.router.navigate(['/commissions', commissionId, 'list-candidatures']);
    }
  }
  getRoleBadgeClass(role?: string): string {
    const classes: Record<string, string> = {
      'Président': 'president',
      'Rapporteur': 'rapporteur',
      'Membre': 'membre'
    };
    return classes[role || ''] || '';
  }
}
