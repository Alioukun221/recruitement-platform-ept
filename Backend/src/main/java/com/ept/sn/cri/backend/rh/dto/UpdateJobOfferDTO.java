package com.ept.sn.cri.backend.rh.dto;

import com.ept.sn.cri.backend.enums.ContratType;
import com.ept.sn.cri.backend.enums.JobStatus;
import com.ept.sn.cri.backend.enums.JobType;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateJobOfferDTO {

    private String jobTitle;
    private JobType jobType;
    private ContratType typeContrat;
    private JobStatus jobStatus;
    private String description;
    private String requiredSkills;
    private String niveauEtudeRequis;

    @Min(value = 0, message = "L'expérience minimale ne peut pas être négative.")
    private Integer experienceMin;

    @Future(message = "La date limite doit être dans le futur.")
    private Date dateLimite;
}
