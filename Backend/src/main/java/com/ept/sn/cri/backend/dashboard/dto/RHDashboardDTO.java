package com.ept.sn.cri.backend.dashboard.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RHDashboardDTO {

    // Vue d'ensemble générale
    private DashboardOverviewDTO overview;

    // Statistiques des offres d'emploi
    private JobOfferStatsDTO jobOfferStats;

    // Statistiques des candidatures
    private ApplicationStatsDTO applicationStats;

    // Statistiques des commissions
    private CommissionStatsDTO commissionStats;

    // Offres les plus performantes
    private List<TopJobOfferDTO> topJobOffers;

    // Activité récente
    private List<RecentActivityDTO> recentActivities;

    // Alertes et notifications
    private List<DashboardAlertDTO> alerts;
}
