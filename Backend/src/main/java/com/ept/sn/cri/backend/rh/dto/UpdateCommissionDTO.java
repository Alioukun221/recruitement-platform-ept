package com.ept.sn.cri.backend.rh.dto;

import com.ept.sn.cri.backend.enums.CommissionStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateCommissionDTO {

    private String name;
    private String description;
    private CommissionStatus status;
}
