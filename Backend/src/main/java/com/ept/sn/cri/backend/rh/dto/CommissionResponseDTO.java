package com.ept.sn.cri.backend.rh.dto;

import com.ept.sn.cri.backend.enums.CommissionStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommissionResponseDTO {

    private Long id;
    private String name;
    private String description;
    private CommissionStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Long createdById;
    private String createdByName;
    private Long jobOfferId;
    private String jobOfferTitle;
    private List<CommissionMemberResponseDTO> members;
    private Integer memberCount;
    private CommissionMemberResponseDTO president;
}
