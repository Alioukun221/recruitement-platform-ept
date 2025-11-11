package com.ept.sn.cri.backend.rh.dto;

import com.ept.sn.cri.backend.enums.ContratType;
import com.ept.sn.cri.backend.enums.JobStatus;
import com.ept.sn.cri.backend.enums.JobType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JobOfferListDTO {

    private Long id;
    private String jobTitle;
    private JobType jobType;
    private ContratType typeContrat;
    private JobStatus jobStatus;
    private Date datePublication;
    private Date dateLimite;
    private Integer applicationCount;
}