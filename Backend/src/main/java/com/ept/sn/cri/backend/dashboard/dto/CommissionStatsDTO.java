package com.ept.sn.cri.backend.dashboard.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommissionStatsDTO {

    private Integer totalCommissions;
    private Integer activeCommissions;
    private Integer totalCommissionMembers;
    private Integer totalEvaluations;

    // Taux de complétion des évaluations
    private Double evaluationCompletionRate;

    // Commission la plus active
    private CommissionActivityDTO mostActiveCommission;

    // Nombre moyen de membres par commission
    private Double averageMembersPerCommission;
}
