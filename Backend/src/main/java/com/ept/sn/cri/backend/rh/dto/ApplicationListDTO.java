package com.ept.sn.cri.backend.rh.dto;

import com.ept.sn.cri.backend.enums.ApplicationStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApplicationListDTO {

    private Long id;
    private String candidateName;
    private String candidateEmail;
    private String highestDegree;
    private String specialization;
    private ApplicationStatus status;
    private Integer scoreIA;
    private LocalDateTime submitDate;
    private String cvUrl;
}