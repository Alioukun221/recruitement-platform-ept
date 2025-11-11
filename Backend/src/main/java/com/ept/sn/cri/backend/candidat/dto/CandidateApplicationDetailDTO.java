package com.ept.sn.cri.backend.candidat.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CandidateApplicationDetailDTO {

    private Long id;
    private Long jobOfferId;
    private String jobTitle;
    private String jobDescription;
    private String firstName;
    private String lastName;
    private String email;
    private String address;
    private String highestDegree;
    private String specialization;
    private String cvUrl;
    private String status;
    private LocalDateTime submitDate;
    private LocalDateTime lastUpdateDate;
}