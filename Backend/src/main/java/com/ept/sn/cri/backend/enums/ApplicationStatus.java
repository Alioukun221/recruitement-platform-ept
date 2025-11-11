package com.ept.sn.cri.backend.enums;

public enum ApplicationStatus {
    DRAFT,              // Brouillon
    SUBMITTED,          // Soumise
    UNDER_REVIEW,       // En cours d'examen
    AI_SCORED,          // Score par IA
    SHORTLISTED,        // Présélectionnée
    INTERVIEW_SCHEDULED,// Entretien programmé
    INTERVIEW_COMPLETED,// Entretien terminé
    ACCEPTED,           // Acceptée
    REJECTED,           // Rejetée
    WITHDRAWN           // Retirée
}
