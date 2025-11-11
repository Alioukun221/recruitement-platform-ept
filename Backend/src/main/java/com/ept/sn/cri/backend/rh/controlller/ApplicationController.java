package com.ept.sn.cri.backend.rh.controlller;

import com.ept.sn.cri.backend.entity.RH;
import com.ept.sn.cri.backend.enums.ApplicationStatus;
import com.ept.sn.cri.backend.rh.dto.*;
import com.ept.sn.cri.backend.rh.service.ApplicationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/rh/applications")
@RequiredArgsConstructor
public class ApplicationController {

    private final ApplicationService applicationService;

    /**
     * Obtenir toutes les candidatures pour une offre d'emploi
     */
    @GetMapping("/job-offer/{jobOfferId}")
    public ResponseEntity<List<ApplicationListDTO>> getApplicationsByJobOffer(
            @PathVariable Long jobOfferId,
            @RequestParam(required = false) ApplicationStatus status,
            @AuthenticationPrincipal RH rh) {

        List<ApplicationListDTO> applications = applicationService.getApplicationsByJobOffer(jobOfferId, rh.getId(), status);
        return ResponseEntity.ok(applications);
    }


    /**
     * Obtenir les détails complets d'une candidature
     */
    @GetMapping("/{applicationId}")
    public ResponseEntity<ApplicationDetailDTO> getApplicationDetails(
            @PathVariable Long applicationId,
            @AuthenticationPrincipal RH rh) {

        ApplicationDetailDTO application = applicationService.getApplicationDetails(applicationId, rh.getId());
        return ResponseEntity.ok(application);
    }

    /**
     * Présélectionner des candidats pour l'entretien
     */
    @PostMapping("/job-offer/{jobOfferId}/shortlist")
    public ResponseEntity<List<ApplicationDetailDTO>> shortlistApplications(
            @PathVariable Long jobOfferId,
            @Valid @RequestBody ShortlistApplicationsDTO dto,
            @AuthenticationPrincipal RH rh) {

        List<ApplicationDetailDTO> shortlisted = applicationService.shortlistApplications(jobOfferId, dto, rh.getId());
        return ResponseEntity.ok(shortlisted);
    }

    /**
     * Obtenir les candidats présélectionnés pour une offre
     */
    @GetMapping("/job-offer/{jobOfferId}/shortlisted")
    public ResponseEntity<List<ApplicationDetailDTO>> getShortlistedApplications(
            @PathVariable Long jobOfferId,
            @AuthenticationPrincipal RH rh) {

        List<ApplicationDetailDTO> shortlisted = applicationService.getShortlistedApplications(jobOfferId, rh.getId());
        return ResponseEntity.ok(shortlisted);
    }


    /**
     * Changer le statut d'une candidature
     */
    @PutMapping("/{applicationId}/status")
    public ResponseEntity<ApplicationDetailDTO> updateApplicationStatus(
            @PathVariable Long applicationId,
            @Valid @RequestBody UpdateApplicationStatusDTO dto,
            @AuthenticationPrincipal RH rh) {

        ApplicationDetailDTO updated = applicationService.updateApplicationStatus(applicationId, dto, rh.getId());
        return ResponseEntity.ok(updated);
    }

    /**
     * Rejeter une candidature
     */
    @PutMapping("/{applicationId}/reject")
    public ResponseEntity<ApplicationDetailDTO> rejectApplication(
            @PathVariable Long applicationId,
            @AuthenticationPrincipal RH rh) {

        ApplicationDetailDTO rejected = applicationService.rejectApplication(applicationId, rh.getId());
        return ResponseEntity.ok(rejected);
    }

    /**
     * Rejeter plusieurs candidatures en masse
     */
    @PostMapping("/job-offer/{jobOfferId}/reject-multiple")
    public ResponseEntity<List<ApplicationDetailDTO>> rejectMultipleApplications(
            @PathVariable Long jobOfferId,
            @Valid @RequestBody ShortlistApplicationsDTO dto,
            @AuthenticationPrincipal RH rh) {

        List<ApplicationDetailDTO> rejected = applicationService.rejectMultipleApplications(jobOfferId, dto, rh.getId());
        return ResponseEntity.ok(rejected);
    }

    /**
     * Obtenir les statistiques des candidatures pour une offre
     */
    @GetMapping("/job-offer/{jobOfferId}/stats")
    public ResponseEntity<ApplicationStatsDTO> getApplicationStats(
            @PathVariable Long jobOfferId,
            @AuthenticationPrincipal RH rh) {

        ApplicationStatsDTO stats = applicationService.getApplicationStats(jobOfferId, rh.getId());
        return ResponseEntity.ok(stats);
    }

    /**
     * Obtenir les candidatures avec un score minimum
     */
    @GetMapping("/job-offer/{jobOfferId}/by-score")
    public ResponseEntity<List<ApplicationListDTO>> getApplicationsByMinScore(
            @PathVariable Long jobOfferId,
            @RequestParam Integer minScore,
            @AuthenticationPrincipal RH rh) {

        List<ApplicationListDTO> applications = applicationService.getApplicationsByMinScore(jobOfferId, Long.valueOf(minScore), rh.getId());
        return ResponseEntity.ok(applications);
    }




}
