package com.ept.sn.cri.backend.candidat.dto;

import com.ept.sn.cri.backend.enums.ContratType;
import com.ept.sn.cri.backend.enums.JobType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CandidateApplicationHistoryDTO {

    private Long id;
    private Long jobOfferId;
    private String jobTitle;
    private JobType jobType;
    private ContratType typeContrat;
    private String status;
    private LocalDateTime submitDate;
    private LocalDateTime lastUpdateDate;
    private String cvUrl;
    private Boolean canWithdraw;
}