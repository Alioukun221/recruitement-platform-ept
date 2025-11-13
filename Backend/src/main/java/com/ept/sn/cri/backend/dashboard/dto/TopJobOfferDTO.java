package com.ept.sn.cri.backend.dashboard.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TopJobOfferDTO {

    private Long jobOfferId;
    private String jobTitle;
    private Integer totalApplications;
    private Integer shortlistedCount;
    private Double averageIAScore;
    private Integer daysActive;
    private String status;
}