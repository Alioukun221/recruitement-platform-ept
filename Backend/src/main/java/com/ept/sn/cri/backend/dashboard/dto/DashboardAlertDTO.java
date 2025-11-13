package com.ept.sn.cri.backend.dashboard.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DashboardAlertDTO {

    private String alertType; // WARNING, INFO, URGENT
    private String title;
    private String message;
    private String actionUrl;
    private Integer count;
}