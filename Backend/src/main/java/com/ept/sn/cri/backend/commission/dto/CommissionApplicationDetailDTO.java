package com.ept.sn.cri.backend.commission.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommissionApplicationDetailDTO {

    private Long id;

    // Informations du candidat
    private String firstName;
    private String lastName;
    private String email;
    private String address;
    private String highestDegree;
    private String specialization;
    private String cvUrl;

    // Scores IA (visibles pour les membres de commission)
    private Integer scoreIA;
    private Integer matchingCompetences;
    private Integer matchingExperience;
    private Integer matchingDiploma;
    private String justificationIA;

    // Dates
    private LocalDateTime submitDate;

    // Informations sur l'offre
    private Long jobOfferId;
    private String jobOfferTitle;

    // Évaluations
    private List<EvaluationResponseDTO> evaluations;
    private Integer evaluationCount;
    private EvaluationAverageDTO averageScores;
    private Boolean alreadyEvaluatedByMe;
}