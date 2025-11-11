package com.ept.sn.cri.backend.commission.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EvaluationAverageDTO {

    private Double averageCompetenceScore;
    private Double averageExperienceScore;
    private Double averageDiplomaScore;
    private Double averageMotivationScore;
    private Double averageSoftSkillsScore;
    private Double overallAverage;
}