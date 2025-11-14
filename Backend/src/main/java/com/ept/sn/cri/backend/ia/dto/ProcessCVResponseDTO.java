package com.ept.sn.cri.backend.ia.dto;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProcessCVResponseDTO {

    private Boolean success;

    @JsonProperty("application_id")
    private Long applicationId;


    @JsonProperty("scoring_result")
    private IAScoringResultDTO scoringResult;

    @JsonProperty("error_message")
    private String errorMessage;

    @JsonProperty("total_processing_time")
    private Double totalProcessingTime;
}
