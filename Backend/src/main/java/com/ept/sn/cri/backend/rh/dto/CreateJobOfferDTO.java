package com.ept.sn.cri.backend.rh.dto;

import com.ept.sn.cri.backend.enums.ContratType;
import com.ept.sn.cri.backend.enums.JobType;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateJobOfferDTO {

    @NotBlank(message = "Le titre du poste est obligatoire.")
    private String jobTitle;

    @NotNull(message = "Le type de poste est obligatoire.")
    private JobType jobType;

    @NotNull(message = "Le type de contrat est obligatoire.")
    private ContratType typeContrat;

    @NotBlank(message = "La description est obligatoire.")
    private String description;

    @NotBlank(message = "Les compétences requises sont obligatoires.")
    private String requiredSkills;

    @NotBlank(message = "Le niveau d'étude requis est obligatoire.")
    private String niveauEtudeRequis;

    @NotNull(message = "L'expérience minimale est obligatoire.")
    @Min(value = 0, message = "L'expérience minimale ne peut pas être négative.")
    private Integer experienceMin;

    @Future(message = "La date limite doit être dans le futur.")
    private Date dateLimite;
}