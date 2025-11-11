package com.ept.sn.cri.backend.commission.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommissionApplicationListDTO {

    private Long id;
    private String candidateName;
    private String candidateEmail;
    private String highestDegree;
    private String specialization;
    private Integer scoreIA;
    private LocalDateTime submitDate;
    private String cvUrl;
    private Integer evaluationCount;
    private Double averageCommissionScore;
    private Boolean alreadyEvaluatedByMe;
}
