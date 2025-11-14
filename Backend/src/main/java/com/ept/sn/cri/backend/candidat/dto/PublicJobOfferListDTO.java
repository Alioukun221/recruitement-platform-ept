package com.ept.sn.cri.backend.candidat.dto;

import com.ept.sn.cri.backend.enums.ContratType;
import com.ept.sn.cri.backend.enums.JobType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PublicJobOfferListDTO {

    private Long id;
    private String jobTitle;
    private JobType jobType;
    private ContratType typeContrat;
    private String description;
    private String niveauEtudeRequis;
    private Integer experienceMin;
    private Date datePublication;
    private Date dateLimite;
    private Integer applicationCount;
    private List<String> requiredSkills;
}