package com.ept.sn.cri.backend.rh.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApplicationStatsDTO {

    private Long jobOfferId;
    private String jobOfferTitle;
    private Long totalApplications;
    private Long submittedCount;
    private Long underReviewCount;
    private Long shortlistedCount;
    private Long rejectedCount;
    private Double averageScore;
    private Integer maxScore;
    private Integer minScore;
}