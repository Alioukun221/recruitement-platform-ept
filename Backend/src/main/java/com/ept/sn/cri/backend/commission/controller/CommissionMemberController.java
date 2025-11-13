package com.ept.sn.cri.backend.commission.controller;

import com.ept.sn.cri.backend.commission.dto.*;
import com.ept.sn.cri.backend.commission.service.CommissionMemberService;
import com.ept.sn.cri.backend.entity.User;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@RestController
@RequestMapping("/commission-member")
@RequiredArgsConstructor
@Tag(name="Membre de commission (notations && commentaires)")
public class CommissionMemberController {

    private final CommissionMemberService commissionMemberService;

    /**
     * Obtenir mes commissions
     * GET /api/v1/commission-member/my-commissions
     */
    @PreAuthorize("hasAuthority('COMMISSION_MEMBER')")
    @GetMapping("/my-commissions")
    public ResponseEntity<List<MemberCommissionDTO>> getMyCommissions(
            @AuthenticationPrincipal User user) {

        List<MemberCommissionDTO> commissions = commissionMemberService.getMyCommissions(user.getId());
        return ResponseEntity.ok(commissions);
    }

    /**
     * Obtenir les candidats présélectionnés d'une commission
     * GET /api/v1/commission member/commissions/{commissionId}/applications
     */
    @PreAuthorize("hasAuthority('COMMISSION_MEMBER')")
    @GetMapping("/commissions/{commissionId}/applications")
    public ResponseEntity<List<CommissionApplicationListDTO>> getShortlistedApplications(
            @PathVariable Long commissionId,
            @AuthenticationPrincipal User user) {

        List<CommissionApplicationListDTO> applications =
                commissionMemberService.getShortlistedApplications(commissionId, user.getId());

        return ResponseEntity.ok(applications);
    }

    /**
     * Obtenir les détails complets d'un candidat
     * GET /api/v1/commission member/commissions/{commissionId}/applications/{applicationId}
     */
    @PreAuthorize("hasAuthority('COMMISSION_MEMBER')")
    @GetMapping("/commissions/{commissionId}/applications/{applicationId}")
    public ResponseEntity<CommissionApplicationDetailDTO> getApplicationDetails(
            @PathVariable Long commissionId,
            @PathVariable Long applicationId,
            @AuthenticationPrincipal User user) {

        CommissionApplicationDetailDTO application =
                commissionMemberService.getApplicationDetails(applicationId, commissionId, user.getId());

        return ResponseEntity.ok(application);
    }

    /**
     * Visualiser le CV d'un candidat
     * GET /api/v1/commission-member/commissions/{commissionId}/applications/{applicationId}/cv
     */
    @PreAuthorize("hasAuthority('COMMISSION_MEMBER')")
    @GetMapping("/commissions/{commissionId}/applications/{applicationId}/cv")
    public ResponseEntity<Resource> viewCV(
            @PathVariable Long commissionId,
            @PathVariable Long applicationId,
            @AuthenticationPrincipal User user) {

        try {
            // Récupérer les détails de la candidature pour obtenir l'URL du CV
            CommissionApplicationDetailDTO application =
                    commissionMemberService.getApplicationDetails(applicationId, commissionId, user.getId());

            if (application.getCvUrl() == null) {
                throw new RuntimeException("Aucun CV disponible pour ce candidat");
            }

            // Construire le chemin du fichier
            Path filePath = Paths.get("uploads/cvs")
                    .resolve(
                            application.getCvUrl()
                                    .substring(application.getCvUrl().lastIndexOf("/") + 1)
                    )
                    .normalize();

            Resource resource = new UrlResource(filePath.toUri());

            if (!resource.exists()) {
                throw new RuntimeException("CV non trouvé");
            }

            // Déterminer le type de contenu
            String contentType = "application/pdf";

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + resource.getFilename() + "\"")
                    .body(resource);

        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de la récupération du CV: " + e.getMessage());
        }
    }

    /**
     * Évaluer un candidat
     * POST /api/v1/commission-member/commissions/{commissionId}/applications/{applicationId}/evaluate
     */
    @PreAuthorize("hasAuthority('COMMISSION_MEMBER')")
    @PostMapping("/commissions/{commissionId}/applications/{applicationId}/evaluate")
    public ResponseEntity<EvaluationResponseDTO> evaluateApplication(
            @PathVariable Long commissionId,
            @PathVariable Long applicationId,
            @Valid @RequestBody CreateEvaluationDTO dto,
            @AuthenticationPrincipal User user) {

        EvaluationResponseDTO evaluation = commissionMemberService.evaluateApplication(
                applicationId, commissionId, dto, user.getId()
        );

        return new ResponseEntity<>(evaluation, HttpStatus.CREATED);
    }

    /**
     * Modifier mon évaluation
     * PUT /api/v1/commission-member/commissions/{commissionId}/applications/{applicationId}/evaluate
     */
    @PreAuthorize("hasAuthority('COMMISSION_MEMBER')")
    @PutMapping("/commissions/{commissionId}/applications/{applicationId}/evaluate")
    public ResponseEntity<EvaluationResponseDTO> updateMyEvaluation(
            @PathVariable Long commissionId,
            @PathVariable Long applicationId,
            @Valid @RequestBody CreateEvaluationDTO dto,
            @AuthenticationPrincipal User user) {

        // La méthode evaluateApplication gère à la fois création et modification
        EvaluationResponseDTO evaluation = commissionMemberService.evaluateApplication(
                applicationId, commissionId, dto, user.getId()
        );

        return ResponseEntity.ok(evaluation);
    }

    /**
     * Obtenir mon évaluation pour un candidat
     * GET /api/v1/commission-member/commissions/{commissionId}/applications/{applicationId}/my-evaluation
     */
    @PreAuthorize("hasAuthority('COMMISSION_MEMBER')")
    @GetMapping("/commissions/{commissionId}/applications/{applicationId}/my-evaluation")
    public ResponseEntity<EvaluationResponseDTO> getMyEvaluation(
            @PathVariable Long commissionId,
            @PathVariable Long applicationId,
            @AuthenticationPrincipal User user) {

        EvaluationResponseDTO evaluation = commissionMemberService.getMyEvaluation(
                applicationId, commissionId, user.getId()
        );

        return ResponseEntity.ok(evaluation);
    }

    /**
     * Supprimer mon évaluation
     * DELETE /api/v1/commission-member/commissions/{commissionId}/applications/{applicationId}/my-evaluation
     */
    @PreAuthorize("hasAuthority('COMMISSION_MEMBER')")
    @DeleteMapping("/commissions/{commissionId}/applications/{applicationId}/my-evaluation")
    public ResponseEntity<Void> deleteMyEvaluation(
            @PathVariable Long commissionId,
            @PathVariable Long applicationId,
            @AuthenticationPrincipal User user) {

        commissionMemberService.deleteMyEvaluation(applicationId, commissionId, user.getId());
        return ResponseEntity.noContent().build();
    }






















}
