package com.ept.sn.cri.backend.candidat.dto;

import com.ept.sn.cri.backend.enums.ContratType;
import com.ept.sn.cri.backend.enums.JobType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PublicJobOfferDetailDTO {

    private Long id;
    private String jobTitle;
    private JobType jobType;
    private ContratType typeContrat;
    private String description;
    private String requiredSkills;
    private String niveauEtudeRequis;
    private Integer experienceMin;
    private Date datePublication;
    private LocalDateTime dateLimite;
    private Integer applicationCount;
    private String companyName; // a changer apres
}