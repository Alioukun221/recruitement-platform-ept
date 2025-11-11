package com.ept.sn.cri.backend.rh.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ShortlistApplicationsDTO {

    private Long[] applicationIds; // Liste des IDs à présélectionner
}