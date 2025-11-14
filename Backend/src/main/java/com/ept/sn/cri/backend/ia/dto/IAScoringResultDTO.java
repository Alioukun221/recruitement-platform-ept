package com.ept.sn.cri.backend.ia.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IAScoringResultDTO {

    @JsonProperty("score_global")
    private Double scoreGlobal;

    @JsonProperty("matching_competences")
    private Double matchingCompetences;

    @JsonProperty("matching_experience")
    private Double matchingExperience;

    @JsonProperty("matching_diploma")
    private Double matchingDiploma;

    @JsonProperty("matching_motivation")
    private Double matchingMotivation;

    private String justification;

    private String recommendation;

    private List<String> strengths;

    private List<String> weaknesses;

    @JsonProperty("missing_skills")
    private List<String> missingSkills;
}