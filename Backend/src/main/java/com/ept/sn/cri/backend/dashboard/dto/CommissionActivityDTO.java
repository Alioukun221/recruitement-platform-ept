package com.ept.sn.cri.backend.dashboard.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommissionActivityDTO {

    private Long commissionId;
    private String commissionName;
    private String jobOfferTitle;
    private Integer membersCount;
    private Integer candidatesCount;
    private Integer evaluationsCount;
    private Double completionRate;
}