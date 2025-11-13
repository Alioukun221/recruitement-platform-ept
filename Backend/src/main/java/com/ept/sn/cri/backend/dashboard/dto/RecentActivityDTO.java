package com.ept.sn.cri.backend.dashboard.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RecentActivityDTO {

    private String activityType; // NEW_APPLICATION, SHORTLISTED, EVALUATED, etc.
    private String description;
    private String timestamp;
    private String relatedEntity; // Job offer title, candidate name, etc.
    private Long entityId;
}