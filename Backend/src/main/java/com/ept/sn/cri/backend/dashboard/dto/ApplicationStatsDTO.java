package com.ept.sn.cri.backend.dashboard.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApplicationStatsDTO {

    private Integer totalApplications;
    private Integer submittedApplications;
    private Integer underReviewApplications;
    private Integer shortlistedApplications;
    private Integer rejectedApplications;
    private Integer withdrawnApplications;

    // Évolution sur les 30 derniers jours
    private Integer applicationsLast30Days;
    private Double growthRate; // Pourcentage de croissance

    // Score IA moyen
    private Double averageIAScore;

    // Répartition par niveau d'étude
    private Map<String, Integer> applicationsByEducationLevel;

    // Candidatures par offre (top 5)
    private List<JobOfferApplicationCountDTO> topOffersByApplications;
}