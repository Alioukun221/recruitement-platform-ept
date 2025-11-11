package com.ept.sn.cri.backend.rh.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateCommissionDTO {

    @NotBlank(message = "Le nom de la commission est obligatoire.")
    @Size(max = 150, message = "Le nom ne doit pas dépasser 150 caractères.")
    private String name;

    @Size(max = 255, message = "La description ne doit pas dépasser 255 caractères.")
    private String description;

    @NotNull(message = "L'ID de l'offre d'emploi est obligatoire.")
    private Long jobOfferId;
}
