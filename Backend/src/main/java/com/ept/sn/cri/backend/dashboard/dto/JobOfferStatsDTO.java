package com.ept.sn.cri.backend.dashboard.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JobOfferStatsDTO {

    private Integer totalJobOffers;
    private Integer publishedJobOffers;
    private Integer draftJobOffers;
    private Integer closedJobOffers;
    private Integer archivedJobOffers;

    // Répartition par type
    private Map<String, Integer> jobOffersByType; // FULL_TIME, PART_TIME, etc.

    // Répartition par type de contrat
    private Map<String, Integer> jobOffersByContract; // CDI, CDD, etc.

    // Offres expirant bientôt (dans les 7 jours)
    private Integer jobOffersExpiringSoon;

    // Taux de remplissage moyen (candidatures / offre)
    private Double averageCandidatesPerOffer;
}