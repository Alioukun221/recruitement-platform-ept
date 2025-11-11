package com.ept.sn.cri.backend.rh.dto;

import com.ept.sn.cri.backend.enums.ApplicationStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateApplicationStatusDTO {

    private ApplicationStatus status;
    private String comment; // Commentaire optionnel
}