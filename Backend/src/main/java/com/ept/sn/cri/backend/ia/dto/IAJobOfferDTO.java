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
public class IAJobOfferDTO {

    @JsonProperty("job_id")
    private Long jobId;

    @JsonProperty("job_title")
    private String jobTitle;

    @JsonProperty("job_type")
    private String jobType;

    @JsonProperty("contract_type")
    private String contractType;

    private String description;

    @JsonProperty("required_skills")
    private List<String> requiredSkills;

    @JsonProperty("education_level")
    private String educationLevel;

    @JsonProperty("min_experience")
    private Integer minExperience;
}
