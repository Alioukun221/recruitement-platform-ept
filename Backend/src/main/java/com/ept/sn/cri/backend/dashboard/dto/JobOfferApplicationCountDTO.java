package com.ept.sn.cri.backend.dashboard.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JobOfferApplicationCountDTO {

    private Long jobOfferId;
    private String jobTitle;
    private Integer applicationCount;
    private Integer shortlistedCount;
    private String status;
}
