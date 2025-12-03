import { Component, OnInit, inject, PLATFORM_ID, ChangeDetectorRef, ViewChild } from '@angular/core';
import { CommonModule, isPlatformBrowser, NgClass, NgIf } from '@angular/common';
import { NgApexchartsModule, ChartComponent, ApexAxisChartSeries, ApexChart, ApexXAxis, ApexStroke, ApexDataLabels, ApexLegend, ApexGrid, ApexTooltip, ApexNonAxisChartSeries, ApexPlotOptions } from 'ng-apexcharts';
import { RhDashboardDto } from '../../api/models/rh-dashboard-dto';
import { DashboardOverviewDto } from '../../api/models/dashboard-overview-dto';
import { ApplicationStatsDto } from '../../api/models/application-stats-dto';
import { JobOfferStatsDto } from '../../api/models/job-offer-stats-dto';
import { CommissionStatsDto } from '../../api/models/commission-stats-dto';
import { DashboardAlertDto } from '../../api/models/dashboard-alert-dto';
import { RecentActivityDto } from '../../api/models/recent-activity-dto';
import { TopJobOfferDto } from '../../api/models/top-job-offer-dto';
import { Api } from '../../api/api';
import { getDashboard } from '../../api/functions';

export type LineChartOptions = {
  series: ApexAxisChartSeries;
  chart: ApexChart;
  xaxis: ApexXAxis;
  stroke: ApexStroke;
  colors: string[];
  dataLabels: ApexDataLabels;
  legend: ApexLegend;
  grid: ApexGrid;
  tooltip: ApexTooltip;
};

export type DonutChartOptions = {
  series: ApexNonAxisChartSeries;
  chart: ApexChart;
  labels: string[];
  colors: string[];
  legend: ApexLegend;
  dataLabels: ApexDataLabels;
  plotOptions: ApexPlotOptions;
  tooltip: ApexTooltip;
};

@Component({
  selector: 'app-dashboard-component',
  standalone: true,
  imports: [NgClass, NgIf, CommonModule, NgApexchartsModule],
  templateUrl: './dashboard-component.html',
  styleUrl: './dashboard-component.css',
})
export class DashboardComponent implements OnInit {
  @ViewChild('lineChart') lineChart!: ChartComponent;
  @ViewChild('donutChart') donutChart!: ChartComponent;

  private platformId = inject(PLATFORM_ID);

  dashboardData: RhDashboardDto = {};
  overview: DashboardOverviewDto = {};
  applicationStats: ApplicationStatsDto = {};
  jobOfferStats: JobOfferStatsDto = {};
  commissionStats: CommissionStatsDto = {};
  alerts: DashboardAlertDto[] = [];
  recentActivities: RecentActivityDto[] = [];
  topJobOffers: TopJobOfferDto[] = [];

  isLoading: boolean = true;
  errorMessage: string | null = null;

  // Données pour les graphiques
  applicationTrendData: any[] = [];
  educationLevelData: any[] = [];
  contractTypeData: any[] = [];
  jobTypeData: any[] = [];

  // Chart Options
  public lineChartOptions: Partial<LineChartOptions>;
  public donutChartOptions: Partial<DonutChartOptions>;

  constructor(
    private api: Api,
    private cdr: ChangeDetectorRef
  ) {
    // Initialize Line Chart
    this.lineChartOptions = {
      series: [
        { name: 'Soumises', data: [12, 18, 25, 31, 42] },
        { name: 'En révision', data: [8, 12, 16, 22, 28] },
        { name: 'Présélectionnées', data: [5, 8, 12, 15, 20] },
        { name: 'Rejetées', data: [3, 6, 8, 11, 15] }
      ],
      chart: {
        height: 350,
        type: 'line',
        toolbar: {
          show: false
        },
        zoom: {
          enabled: false
        },
        fontFamily: '-apple-system, BlinkMacSystemFont, "Segoe UI", Inter, Roboto, sans-serif',
      },
      colors: ['#3B82F6', '#F59E0B', '#10B981', '#EF4444'],
      stroke: {
        curve: 'smooth',
        width: 2
      },
      dataLabels: {
        enabled: false
      },
      legend: {
        position: 'top',
        horizontalAlign: 'left',
        fontSize: '13px',
        fontWeight: 500,
        markers: {
          width: 10,
          height: 10,
          radius: 10
        } as any
      },
      grid: {
        borderColor: '#E2E8F0',
        strokeDashArray: 4,
        xaxis: {
          lines: {
            show: false
          }
        }
      },
      xaxis: {
        categories: ['Sem 1', 'Sem 2', 'Sem 3', 'Sem 4', 'Aujourd\'hui'],
        labels: {
          style: {
            colors: '#64748B',
            fontSize: '12px'
          }
        },
        axisBorder: {
          show: false
        },
        axisTicks: {
          show: false
        }
      },
      tooltip: {
        theme: 'light',
        y: {
          formatter: function (val: number) {
            return val + ' candidatures';
          }
        }
      }
    };

    // Initialize Donut Chart
    this.donutChartOptions = {
      series: [0],
      chart: {
        height: 320,
        type: 'donut',
        fontFamily: '-apple-system, BlinkMacSystemFont, "Segoe UI", Inter, Roboto, sans-serif',
      },
      labels: ['Aucune donnée'],
      colors: ['#3B82F6', '#8B5CF6', '#10B981', '#F59E0B', '#EF4444', '#06B6D4'],
      legend: {
        position: 'bottom',
        fontSize: '13px',
        fontWeight: 500,
        markers: {
          width: 10,
          height: 10,
          radius: 10
        } as any
      },
      plotOptions: {
        pie: {
          donut: {
            size: '70%',
            labels: {
              show: true,
              total: {
                show: true,
                label: 'Total',
                fontSize: '14px',
                fontWeight: 500,
                color: '#64748B',
                formatter: function (w: any) {
                  return w.globals.seriesTotals.reduce((a: number, b: number) => {
                    return a + b;
                  }, 0);
                }
              },
              value: {
                show: true,
                fontSize: '24px',
                fontWeight: 700,
                color: '#0F172A'
              }
            }
          }
        }
      },
      dataLabels: {
        enabled: false
      },
      tooltip: {
        theme: 'light',
        y: {
          formatter: function (val: number) {
            return val + ' candidats';
          }
        }
      }
    };
  }

  ngOnInit(): void {
    if (isPlatformBrowser(this.platformId)) {
      setTimeout(() => {
        this.loadDashboard();
      }, 100);
    }
  }

  async loadDashboard(): Promise<void> {
    this.isLoading = true;
    this.errorMessage = null;

    try {
      this.dashboardData = await this.api.invoke(getDashboard, {});

      console.log('✅ Dashboard chargé', this.dashboardData);

      // Extraction des données
      this.overview = this.dashboardData.overview || {};
      this.applicationStats = this.dashboardData.applicationStats || {};
      this.jobOfferStats = this.dashboardData.jobOfferStats || {};
      this.commissionStats = this.dashboardData.commissionStats || {};
      this.alerts = this.dashboardData.alerts || [];
      this.recentActivities = this.dashboardData.recentActivities || [];
      this.topJobOffers = this.dashboardData.topJobOffers || [];

      // Préparation des données pour les graphiques
      this.prepareChartData();
      this.updateCharts();

      this.isLoading = false;
      this.cdr.detectChanges();

    } catch (error) {
      console.error('❌ Erreur chargement dashboard', error);
      this.errorMessage = 'Impossible de charger le dashboard pour le moment.';
      this.isLoading = false;
      this.cdr.detectChanges();
    }
  }

  prepareChartData(): void {
    // Données pour le graphique d'évolution des candidatures
    this.applicationTrendData = [
      { label: 'Soumises', value: this.applicationStats.submittedApplications || 0, color: '#3B82F6' },
      { label: 'En révision', value: this.applicationStats.underReviewApplications || 0, color: '#F59E0B' },
      { label: 'Présélectionnées', value: this.applicationStats.shortlistedApplications || 0, color: '#10B981' },
      { label: 'Rejetées', value: this.applicationStats.rejectedApplications || 0, color: '#EF4444' }
    ];

    // Données niveau d'éducation
    if (this.applicationStats.applicationsByEducationLevel) {
      this.educationLevelData = Object.entries(this.applicationStats.applicationsByEducationLevel).map(([key, value]) => ({
        label: key,
        value: value
      }));
    }

    // Données type de contrat
    if (this.jobOfferStats.jobOffersByContract) {
      this.contractTypeData = Object.entries(this.jobOfferStats.jobOffersByContract).map(([key, value]) => ({
        label: key,
        value: value
      }));
    }

    // Données type d'offre
    if (this.jobOfferStats.jobOffersByType) {
      this.jobTypeData = Object.entries(this.jobOfferStats.jobOffersByType).map(([key, value]) => ({
        label: key,
        value: value
      }));
    }
  }

  updateCharts(): void {
    // Update Line Chart with trend data (simulating weekly data)
    const weeks = 5;

// Soumises
    const submittedData = Array(weeks).fill(0).map((_, i) => {
      const value = Math.floor((this.applicationStats.submittedApplications || 0) * (0.6 + (i * 0.1)));
      console.log(`Week ${i + 1} - Soumises: ${value}`);
      return value;
    });

// En révision
    const reviewData = Array(weeks).fill(0).map((_, i) => {
      const value = Math.floor((this.applicationStats.underReviewApplications || 0) * (0.5 + (i * 0.12)));
      console.log(`Week ${i + 1} - En révision: ${value}`);
      return value;
    });

// Présélectionnées
    const shortlistedData = Array(weeks).fill(0).map((_, i) => {
      const value = Math.floor((this.applicationStats.shortlistedApplications || 0) * (0.4 + (i * 0.15)));
      console.log(`Week ${i + 1} - Présélectionnées: ${value}`);
      return value;
    });

// Rejetées
    const rejectedData = Array(weeks).fill(0).map((_, i) => {
      const value = Math.floor((this.applicationStats.rejectedApplications || 0) * (0.3 + (i * 0.18)));
      console.log(`Week ${i + 1} - Rejetées: ${value}`);
      return value;
    });

    console.log('Submitted Data:', submittedData);
    console.log('Review Data:', reviewData);
    console.log('Shortlisted Data:', shortlistedData);
    console.log('Rejected Data:', rejectedData);

    this.lineChartOptions.series = [
      { name: 'Soumises', data: submittedData },
      { name: 'En révision', data: reviewData },
      { name: 'Présélectionnées', data: shortlistedData },
      { name: 'Rejetées', data: rejectedData }
    ];


    // Update Donut Chart with education level data
    if (this.educationLevelData.length > 0) {
      this.donutChartOptions.series = this.educationLevelData.map(d => d.value);
      this.donutChartOptions.labels = this.educationLevelData.map(d => d.label);
    }
  }

  getAlertClass(alertType?: string): string {
    const typeMap: { [key: string]: string } = {
      'ERROR': 'alert-error',
      'WARNING': 'alert-warning',
      'INFO': 'alert-info',
      'SUCCESS': 'alert-success'
    };
    return typeMap[alertType || 'INFO'] || 'alert-info';
  }

  getStatusBadgeClass(status?: string): string {
    const statusMap: { [key: string]: string } = {
      'PUBLISHED': 'published',
      'DRAFT': 'draft',
      'CLOSED': 'closed',
      'ARCHIVED': 'archived'
    };
    return statusMap[status || 'DRAFT'] || 'draft';
  }

  formatDate(dateString?: string): string {
    if (!dateString) return '-';
    const date = new Date(dateString);
    const now = new Date();
    const diffMs = now.getTime() - date.getTime();
    const diffMins = Math.floor(diffMs / 60000);
    const diffHours = Math.floor(diffMs / 3600000);
    const diffDays = Math.floor(diffMs / 86400000);

    if (diffMins < 1) return 'À l\'instant';
    if (diffMins < 60) return `Il y a ${diffMins} min`;
    if (diffHours < 24) return `Il y a ${diffHours}h`;
    if (diffDays < 7) return `Il y a ${diffDays}j`;

    return date.toLocaleDateString('fr-FR', {
      day: '2-digit',
      month: 'short'
    });
  }

  calculatePercentage(value: number, total: number): number {
    return total > 0 ? Math.round((value / total) * 100) : 0;
  }
}
