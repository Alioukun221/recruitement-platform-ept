import { Routes } from '@angular/router';
import { MainLayout } from './layouts/main-layout/main-layout';
import { OffresEmploi } from './features/offres-emploi/offres-emploi';
import {MultiStepFormComponent} from './features/multi-step-form-component/multi-step-form-component';
import {SignupComponent} from './auth/signup-component/signup-component';
import {LoginComponent} from './auth/login-component/login-component';
import {AuthGuard} from './auth/auth-guard';
import {CandidaturesComponent} from './candidat/candidatures-component/candidatures-component';
import {CandidatureDetailsComponent} from './candidat/candidature-details-component/candidature-details-component';
import {MesCommissionsComponent} from './commission/mes-commissions-components/mes-commissions-components';
import {CandidatesListComponent} from './commission/commission-candidats-components/commission-candidats-components';
import {
  CommissionCandidatDetailComponents
} from './commission/commission-candidat-detail-components/commission-candidat-detail-components';
import {JobOffersListComponent} from './rh/job-offers-list-component/job-offers-list-component';
import {JobOfferDetailComponent} from './rh/job-offer-detail-component/job-offer-detail-component';
import {CreateJobOfferComponent} from './rh/create-job-offer-component/create-job-offer-component';
import {DashboardComponent} from './rh/dashboard-component/dashboard-component';
import {ListCandidatByOfferComponent} from './rh/list-candidat-by-offer-component/list-candidat-by-offer-component';
import {ApplicationDetailComponent} from './rh/application-detail-component/application-detail-component';
import {CommissionsListComponent} from './rh/commissions-list-component/commissions-list-component';
import {CreateCommissionComponent} from './rh/create-commission-component/create-commission-component';
import {CommissionDetailComponent} from './rh/commission-detail-component/commission-detail-component';
import {EvaluateApplicationComponent} from './commission/evaluate-application-component/evaluate-application-component';

export const routes: Routes = [
  {
    path: '',
    component: MainLayout,
    children: [
      { path: 'home', component: OffresEmploi },

      // Détail d'une offre
      // { path: 'offres-emploi/:id', component: OffreDetailComponent },

      { path: 'candidate/my-applications', component: CandidaturesComponent, data: { roles: ['CANDIDATE'] } ,
        canActivate: [AuthGuard] },
      { path: 'commission/my-commissions', component: MesCommissionsComponent, data: { roles: ['COMMISSION_MEMBER'] } ,
        canActivate: [AuthGuard] },
      { path: 'commissions/:commissionId/list-candidatures', component: CandidatesListComponent, data: { roles: ['COMMISSION_MEMBER'] } ,
        canActivate: [AuthGuard] },
      { path: 'rh/job-offers/create', component: CreateJobOfferComponent, data: { roles: ['RH'] } ,
        canActivate: [AuthGuard] },
      { path: 'dashboard', component: DashboardComponent, data: { roles: ['RH'] } ,
        canActivate: [AuthGuard] },
      { path: 'rh/job-offers', component: JobOffersListComponent, data: { roles: ['RH'] } ,
        canActivate: [AuthGuard] },
      { path: 'rh/commissions', component: CommissionsListComponent, data: { roles: ['RH'] } ,
        canActivate: [AuthGuard] },
      { path: 'rh/commissions/create', component: CreateCommissionComponent, data: { roles: ['RH'] } ,
        canActivate: [AuthGuard] },
      { path: 'rh/commissions/:commissionId', component: CommissionDetailComponent, data: { roles: ['RH'] } ,
        canActivate: [AuthGuard] },
      { path: 'rh/job-offers/:offerId/applications', component: ListCandidatByOfferComponent, data: { roles: ['RH'] } ,
        canActivate: [AuthGuard] },
      { path: 'rh/job-offers/:offerId/applications/:applicationId', component: ApplicationDetailComponent, data: { roles: ['RH'] } ,
        canActivate: [AuthGuard] },
      { path: 'rh/job-offers/:offerId', component: JobOfferDetailComponent, data: { roles: ['RH'] } ,
        canActivate: [AuthGuard] },
      { path: 'commissions/:commissionId/list-candidatures/:applicationId', component: CommissionCandidatDetailComponents, data: { roles: ['COMMISSION_MEMBER'] } ,
        canActivate: [AuthGuard] },
      { path: 'commissions/:commissionId/applications/:applicationId/evaluer', component: EvaluateApplicationComponent, data: { roles: ['COMMISSION_MEMBER'] } ,
        canActivate: [AuthGuard] },
      { path: 'commissions/:commissionId/applications/:applicationId/evaluation', component: EvaluateApplicationComponent, data: { roles: ['COMMISSION_MEMBER'] } ,
        canActivate: [AuthGuard] },
      { path: 'candidate/my-applications/:applicationId', component: CandidatureDetailsComponent, data: { roles: ['CANDIDATE'] } ,
        canActivate: [AuthGuard] },
      { path: 'offres-emploi/:id/apply', component: MultiStepFormComponent, data: { roles: ['CANDIDATE'] } ,
        canActivate: [AuthGuard] },
      // redirection par défaut
      { path: '', redirectTo: 'home', pathMatch: 'full' },
    ],
  },
  {
    path: 'auth/signup',
    component: SignupComponent
  },
  {
    path: 'auth/login',
    component: LoginComponent
  },
];
