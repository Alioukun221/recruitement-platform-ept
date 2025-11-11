package com.ept.sn.cri.backend.rh.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommissionMemberResponseDTO {

    private Long id;
    private Long userId;
    private String userName;
    private String userEmail;
    private String role;
    private String expertiseArea;
}
