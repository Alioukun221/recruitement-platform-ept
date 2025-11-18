package com.ept.sn.cri.backend.rh.dto;

import com.ept.sn.cri.backend.enums.ApplicationStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApplicationDetailDTO {

    private Long id;

    // Informations du candidat
    private Long candidateId;
    private String firstName;
    private String lastName;
    private String email;
    private String address;

    // Informations académiques et professionnelles
    private String highestDegree;
    private String majorField;
    private String cvUrl;

    // Statut
    private ApplicationStatus status;

    // Scores IA
    private Integer scoreIA;
    private Integer matchingCompetences;
    private Integer matchingExperience;
    private Integer matchingDiploma;
    private String justificationIA;

    // Dates
    private LocalDateTime submitDate;
    private LocalDateTime updateDate;

    // Informations sur l'offre
    private Long jobOfferId;
    private String jobOfferTitle;

    // Statistiques
    private Integer evaluationCount;
}