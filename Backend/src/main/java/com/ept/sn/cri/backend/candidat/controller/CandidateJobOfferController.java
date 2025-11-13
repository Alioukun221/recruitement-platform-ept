package com.ept.sn.cri.backend.candidat.controller;

import com.ept.sn.cri.backend.candidat.dto.*;
import com.ept.sn.cri.backend.candidat.service.CandidateJobOfferService;
import com.ept.sn.cri.backend.entity.User;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/candidate")
@RequiredArgsConstructor
@Tag(name="Candidats")
public class CandidateJobOfferController {

    private final CandidateJobOfferService candidateJobOfferService;

    /**
     * Obtenir toutes les offres d'emploi disponibles (PUBLIC)
     * Accessible sans authentification
     */
    @GetMapping("/job-offers")
    public ResponseEntity<List<PublicJobOfferListDTO>> getAvailableJobOffers() {
        List<PublicJobOfferListDTO> jobOffers = candidateJobOfferService.getAvailableJobOffers();
        return ResponseEntity.ok(jobOffers);
    }


    /**
     * Obtenir les détails d'une offre spécifique (PUBLIC)
     * Accessible sans authentification
     */
    @GetMapping("/job-offers/{jobOfferId}")
    public ResponseEntity<PublicJobOfferDetailDTO> getJobOfferDetails(@PathVariable Long jobOfferId) {
        PublicJobOfferDetailDTO jobOffer = candidateJobOfferService.getJobOfferDetails(jobOfferId);
        return ResponseEntity.ok(jobOffer);
    }

    /**
     * Rechercher des offres par mot-clé (PUBLIC)
     * GET /api/candidate/job-offers/search?keyword=java
     */
    @GetMapping("/job-offers/search")
    public ResponseEntity<List<PublicJobOfferListDTO>> searchJobOffers(@RequestParam String keyword) {
        List<PublicJobOfferListDTO> jobOffers = candidateJobOfferService.searchJobOffers(keyword);
        return ResponseEntity.ok(jobOffers);
    }

    /**
     * Soumettre une candidature (PROTÉGÉ)
     * Nécessite une authentification avec rôle CANDIDATE
     */
    @PreAuthorize("hasAuthority('CANDIDATE')")
    @PostMapping(value = "/job-offers/{jobOfferId}/apply", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApplicationSubmissionResponseDTO> submitApplication(
            @PathVariable Long jobOfferId,
            @RequestParam String firstName,
            @RequestParam String lastName,
            @RequestParam String email,
            @RequestParam(required = false) String address,
            @RequestParam String highestDegree,
            @RequestParam String specialization,
            @RequestParam(required = false) MultipartFile cv,
            @AuthenticationPrincipal User user) {

        // Construire le DTO avec les données du formulaire
        SubmitApplicationDTO dto = new SubmitApplicationDTO(
                firstName, lastName, email, address, highestDegree, specialization
        );

        // Soumettre la candidature
        ApplicationSubmissionResponseDTO response = candidateJobOfferService.submitApplication(
                jobOfferId, dto, cv, user.getId()
        );

        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    /**
     * Obtenir l'historique des candidatures du candidat connecté (PROTÉGÉ)
     * Nécessite une authentification avec rôle CANDIDATE
     */
    @PreAuthorize("hasAuthority('CANDIDATE')")
    @GetMapping("/my-applications")
    public ResponseEntity<List<CandidateApplicationHistoryDTO>> getMyApplications(
            @AuthenticationPrincipal User user) {

        List<CandidateApplicationHistoryDTO> applications =
                candidateJobOfferService.getCandidateApplicationHistory(user.getId());

        return ResponseEntity.ok(applications);
    }

    /**
     * Obtenir les détails d'une candidature spécifique (PROTÉGÉ)
     * Nécessite une authentification avec rôle CANDIDATE
     */
    @PreAuthorize("hasAuthority('CANDIDATE')")
    @GetMapping("/my-applications/{applicationId}")
    public ResponseEntity<CandidateApplicationDetailDTO> getMyApplicationDetail(
            @PathVariable Long applicationId,
            @AuthenticationPrincipal User user) {

        CandidateApplicationDetailDTO application =
                candidateJobOfferService.getCandidateApplicationDetail(applicationId, user.getId());

        return ResponseEntity.ok(application);
    }

    /**
     * Retirer une candidature (PROTÉGÉ)
     * Nécessite une authentification avec rôle CANDIDATE
     * je ne pense pas que c'est une methode necessaire mais bon laissons
     */
    @PreAuthorize("hasAuthority('CANDIDATE')")
    @DeleteMapping("/my-applications/{applicationId}")
    public ResponseEntity<Void> withdrawApplication(
            @PathVariable Long applicationId,
            @AuthenticationPrincipal User user) {

        candidateJobOfferService.withdrawApplication(applicationId, user.getId());
        return ResponseEntity.noContent().build();
    }
}


