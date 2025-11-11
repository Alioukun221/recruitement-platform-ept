package com.ept.sn.cri.backend.rh.dto;

import com.ept.sn.cri.backend.enums.CommissionStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommissionListDTO {

    private Long id;
    private String name;
    private CommissionStatus status;
    private LocalDateTime createdAt;
    private Long jobOfferId;
    private String jobOfferTitle;
    private Integer memberCount;
    private String presidentName;
}
