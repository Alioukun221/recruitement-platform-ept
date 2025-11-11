package com.ept.sn.cri.backend.commission.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateEvaluationDTO {

    @NotNull(message = "La note de compétence est obligatoire")
    @Min(value = 0, message = "La note doit être entre 0 et 5")
    @Max(value = 5, message = "La note doit être entre 0 et 5")
    private Integer competenceScore;

    @NotNull(message = "La note d'expérience est obligatoire")
    @Min(value = 0, message = "La note doit être entre 0 et 5")
    @Max(value = 5, message = "La note doit être entre 0 et 5")
    private Integer experienceScore;

    @NotNull(message = "La note de diplôme est obligatoire")
    @Min(value = 0, message = "La note doit être entre 0 et 5")
    @Max(value = 5, message = "La note doit être entre 0 et 5")
    private Integer diplomaScore;

    @NotNull(message = "La note de motivation est obligatoire")
    @Min(value = 0, message = "La note doit être entre 0 et 5")
    @Max(value = 5, message = "La note doit être entre 0 et 5")
    private Integer motivationScore;

    @NotNull(message = "La note de soft skills est obligatoire")
    @Min(value = 0, message = "La note doit être entre 0 et 5")
    @Max(value = 5, message = "La note doit être entre 0 et 5")
    private Integer softSkillsScore;

    @Size(max = 2000, message = "Le commentaire ne doit pas dépasser 2000 caractères")
    private String comment;
}