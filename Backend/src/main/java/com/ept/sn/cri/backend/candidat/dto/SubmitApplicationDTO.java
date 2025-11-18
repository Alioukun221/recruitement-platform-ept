package com.ept.sn.cri.backend.candidat.dto;

import jakarta.persistence.Lob;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SubmitApplicationDTO {

    // ---------- STEP 1 : INFORMATIONS PERSONNELLES ----------
    @NotBlank(message = "Le prénom est obligatoire")
    private String firstName;

    @NotBlank(message = "Le nom est obligatoire")
    private String lastName;

    @NotBlank(message = "L'email est obligatoire")
    @Email(message = "Email invalide")
    private String email;

    private String phoneNumber;

    private String address;

    private String nationality;


    // ---------- STEP 2 : PARCOURS ACADEMIQUE ----------
    @NotBlank(message = "Le niveau d'étude est obligatoire")
    private String highestDegree;

    @NotBlank(message = "Le domaine d'étude est obligatoire")
    private String majorField;

    private String educationalInstitution;

    private String yearOfGraduation;


    // ---------- STEP 4 : MOTIVATION ----------
    @NotBlank(message = "La motivation pour l'école est obligatoire")
    @Size(max = 2000, message = "La motivation ne doit pas dépasser 2000 caractères")
    private String motivationEcole;

    @NotBlank(message = "La motivation pour le poste est obligatoire")
    @Size(max = 2000, message = "La motivation ne doit pas dépasser 2000 caractères")
    private String motivationPosition;

    private boolean availableImmediately;


    // ---------- STEP 5 : CERTIFICATION ----------
    private boolean certifyAccurate;
    private boolean consentGDPR;
    private String electronicSignature;
}
