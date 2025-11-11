package com.ept.sn.cri.backend.candidat.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApplicationSubmissionResponseDTO {

    private Long id;
    private String message;
    private Long jobOfferId;
    private String jobOfferTitle;
    private String candidateName;
    private LocalDateTime submitDate;
    private String status;
}