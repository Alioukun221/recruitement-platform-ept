package com.ept.sn.cri.backend.rh.controlller;


import com.ept.sn.cri.backend.entity.RH;
import com.ept.sn.cri.backend.enums.JobStatus;
import com.ept.sn.cri.backend.rh.dto.CreateJobOfferDTO;
import com.ept.sn.cri.backend.rh.dto.JobOfferListDTO;
import com.ept.sn.cri.backend.rh.dto.JobOfferResponseDTO;
import com.ept.sn.cri.backend.rh.dto.UpdateJobOfferDTO;
import com.ept.sn.cri.backend.rh.service.JobOfferService;
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
@RequestMapping("/rh")
@RequiredArgsConstructor
@Tag(name="Gestion des offres")
public class JobOfferController {

    private final JobOfferService jobOfferService;

    /**
     * Créer une nouvelle offre d'emploi
     */
    @PreAuthorize("hasAuthority('RH')")
    @PostMapping("/create-job-offers")
    public ResponseEntity<JobOfferResponseDTO> createJobOffer(
            @Valid @RequestBody CreateJobOfferDTO dto,
            @AuthenticationPrincipal RH rh) {

        JobOfferResponseDTO response = jobOfferService.createJobOffer(dto, rh.getId());
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    /**
     * Modifier une offre d'emploi existante
     */
    @PreAuthorize("hasAuthority('RH')")
    @PutMapping("/update-job-offers/{id}")
    public ResponseEntity<JobOfferResponseDTO> updateJobOffer(
            @PathVariable Long id,
            @Valid @RequestBody UpdateJobOfferDTO dto,
            @AuthenticationPrincipal RH rh) {

        JobOfferResponseDTO response = jobOfferService.updateJobOffer(id, dto, rh.getId());
        return ResponseEntity.ok(response);
    }

    /**
     * Supprimer une offre d'emploi
     */
    @PreAuthorize("hasAuthority('RH')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteJobOffer(
            @PathVariable Long id,
            @AuthenticationPrincipal RH rh) {

        jobOfferService.deleteJobOffer(id, rh.getId());
        return ResponseEntity.noContent().build();
    }

    /**
     * Obtenir toutes les offres d'emploi du RH connecté
     * Avec filtre optionnel par statut et tri par date de création (DESC)
     * GET /api/v1/rh/job-offers?status=PUBLISHED
     */
    @PreAuthorize("hasAuthority('RH')")
    @GetMapping("/job-offers")
    public ResponseEntity<List<JobOfferListDTO>> getAllJobOffers(
            @RequestParam(required = false) JobStatus status,
            @AuthenticationPrincipal RH rh) {

        List<JobOfferListDTO> offers = jobOfferService.getAllJobOffers(rh.getId(), status);
        return ResponseEntity.ok(offers);
    }

    /**
     * Obtenir les détails d'une offre spécifique
     */
    @PreAuthorize("hasAuthority('RH')")
    @GetMapping("job-offers/{id}")
    public ResponseEntity<JobOfferResponseDTO> getJobOfferById(
            @PathVariable Long id,
            @AuthenticationPrincipal RH rh) {

        JobOfferResponseDTO response = jobOfferService.getJobOfferById(id, rh.getId());
        return ResponseEntity.ok(response);
    }

}
