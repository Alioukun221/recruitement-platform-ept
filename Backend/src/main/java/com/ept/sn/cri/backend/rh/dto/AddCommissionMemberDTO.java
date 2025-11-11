package com.ept.sn.cri.backend.rh.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AddCommissionMemberDTO {

    @NotNull(message = "L'ID de l'utilisateur est obligatoire.")
    private Long userId;

    @NotNull(message = "Le rôle est obligatoire.")
    private String role; // "PRESIDENT" ou "MEMBER"

    @Size(max = 255, message = "Le domaine d'expertise ne doit pas dépasser 255 caractères.")
    private String expertiseArea;
}