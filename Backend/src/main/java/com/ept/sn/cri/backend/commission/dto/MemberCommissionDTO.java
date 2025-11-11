package com.ept.sn.cri.backend.commission.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MemberCommissionDTO {

    private Long commissionId;
    private String commissionName;
    private String jobOfferTitle;
    private String myRole; // PRESIDENT ou MEMBER
    private Integer shortlistedCandidatesCount;
    private Integer myEvaluationsCount;
}
