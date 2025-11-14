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
public class ProcessCVRequestDTO {

    @JsonProperty("application_id")
    private Long applicationId;

    @JsonProperty("cv_base64")
    private String cvBase64;

    private String filename;

    @JsonProperty("job_offer")
    private IAJobOfferDTO jobOffer;

    @JsonProperty("callback_url")
    private String callbackUrl;
}
