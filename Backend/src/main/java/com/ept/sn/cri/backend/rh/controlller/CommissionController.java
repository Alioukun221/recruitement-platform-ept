package com.ept.sn.cri.backend.rh.controlller;


import com.ept.sn.cri.backend.entity.RH;
import com.ept.sn.cri.backend.enums.CommissionStatus;
import com.ept.sn.cri.backend.rh.dto.*;
import com.ept.sn.cri.backend.rh.service.CommissionService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/rh/commissions")
@RequiredArgsConstructor
@Tag(name="Gestion des commissions de recrutement")
public class CommissionController {

    private final CommissionService commissionService;

    /**
     * Créer une nouvelle commission de recrutement
     */
    @PreAuthorize("hasAuthority('RH')")
    @PostMapping()
    public ResponseEntity<CommissionResponseDTO> createCommission(
            @Valid @RequestBody CreateCommissionDTO dto,
            @AuthenticationPrincipal RH rh) {

        CommissionResponseDTO response = commissionService.createCommission(dto, rh.getId());
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    /**
     * Obtenir tous les users qui ont un role commission_member
     */

    @GetMapping("/get_all_commissions_members")
    public List<CommissionMemberUserDTO> getAllCommissionMemberUsers() {
        return commissionService.getAllCommissionMembersUsers();
    }

    /**
     * Ajouter un membre à une commission
     */
    @PreAuthorize("hasAuthority('RH')")
    @PostMapping("/{commissionId}/members")
    public ResponseEntity<CommissionMemberResponseDTO> addMember(
            @PathVariable Long commissionId,
            @Valid @RequestBody AddCommissionMemberDTO dto,
            @AuthenticationPrincipal RH rh) {

        CommissionMemberResponseDTO response = commissionService.addMemberToCommission(commissionId, dto, rh.getId());
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    /**
     * Changer le président de la commission
     */
    @PreAuthorize("hasAuthority('RH')")
    @PutMapping("/{commissionId}/president/{memberId}")
    public ResponseEntity<CommissionMemberResponseDTO> changePresident(
            @PathVariable Long commissionId,
            @PathVariable Long memberId,
            @AuthenticationPrincipal RH rh) {

        CommissionMemberResponseDTO response = commissionService.changePresident(commissionId, memberId, rh.getId());
        return ResponseEntity.ok(response);
    }


    /**
     * Retirer un membre d'une commission
     */
    @PreAuthorize("hasAuthority('RH')")
    @DeleteMapping("/{commissionId}/members/{memberId}")
    public ResponseEntity<Void> removeMember(
            @PathVariable Long commissionId,
            @PathVariable Long memberId,
            @AuthenticationPrincipal RH rh) {

        commissionService.removeMemberFromCommission(commissionId, memberId, rh.getId());
        return ResponseEntity.noContent().build();
    }

    /**
     * Modifier une commission
     */
    @PreAuthorize("hasAuthority('RH')")
    @PutMapping("/{commissionId}")
    public ResponseEntity<CommissionResponseDTO> updateCommission(
            @PathVariable Long commissionId,
            @Valid @RequestBody UpdateCommissionDTO dto,
            @AuthenticationPrincipal RH rh) {

        CommissionResponseDTO response = commissionService.updateCommission(commissionId, dto, rh.getId());
        return ResponseEntity.ok(response);
    }

    /**
     * Supprimer une commission
     */
    @PreAuthorize("hasAuthority('RH')")
    @DeleteMapping("/{commissionId}")
    public ResponseEntity<Void> deleteCommission(
            @PathVariable Long commissionId,
            @AuthenticationPrincipal RH rh) {

        commissionService.deleteCommission(commissionId, rh.getId());
        return ResponseEntity.noContent().build();
    }

    /**
     * Obtenir toutes les commissions du RH connecté
     */
    @PreAuthorize("hasAuthority('RH')")
    @GetMapping
    public ResponseEntity<List<CommissionListDTO>> getAllCommissions(
            @RequestParam(required = false) CommissionStatus status,
            @AuthenticationPrincipal RH rh) {

        List<CommissionListDTO> commissions = commissionService.getAllCommissions(rh.getId(), status);
        return ResponseEntity.ok(commissions);
    }

    /**
     * Obtenir les détails d'une commission spécifique
     */
    @PreAuthorize("hasAuthority('RH')")
    @GetMapping("/{commissionId}")
    public ResponseEntity<CommissionResponseDTO> getCommissionById(
            @PathVariable Long commissionId,
            @AuthenticationPrincipal RH rh) {

        CommissionResponseDTO response = commissionService.getCommissionById(commissionId, rh.getId());
        return ResponseEntity.ok(response);
    }

    /**
     * Obtenir tous les membres d'une commission
     */
    @PreAuthorize("hasAuthority('RH')")
    @GetMapping("/{commissionId}/members")
    public ResponseEntity<List<CommissionMemberResponseDTO>> getCommissionMembers(
            @PathVariable Long commissionId,
            @AuthenticationPrincipal RH rh) {

        List<CommissionMemberResponseDTO> members = commissionService.getCommissionMembers(commissionId, rh.getId());
        return ResponseEntity.ok(members);
    }




}
