package com.ept.sn.cri.backend.dashboard.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DashboardOverviewDTO {

    private Integer totalActiveJobOffers;
    private Integer totalCandidates;
    private Integer totalApplications;
    private Integer totalCommissions;
    private Integer candidatesShortlisted;
    private Integer candidatesInterviewed;
    private Integer candidatesAccepted;
    private Integer pendingEvaluations;
}
