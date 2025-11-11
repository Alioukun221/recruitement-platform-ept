package com.ept.sn.cri.backend.rh.controlller;


import com.ept.sn.cri.backend.entity.RH;
import com.ept.sn.cri.backend.enums.JobStatus;
import com.ept.sn.cri.backend.rh.dto.CreateJobOfferDTO;
import com.ept.sn.cri.backend.rh.dto.JobOfferListDTO;
import com.ept.sn.cri.backend.rh.dto.JobOfferResponseDTO;
import com.ept.sn.cri.backend.rh.dto.UpdateJobOfferDTO;
import com.ept.sn.cri.backend.rh.service.JobOfferService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/rh")
@RequiredArgsConstructor
public class JobOfferController {

    private final JobOfferService jobOfferService;

    /**
     * Créer une nouvelle offre d'emploi
     */
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
    @GetMapping("job-offers/{id}")
    public ResponseEntity<JobOfferResponseDTO> getJobOfferById(
            @PathVariable Long id,
            @AuthenticationPrincipal RH rh) {

        JobOfferResponseDTO response = jobOfferService.getJobOfferById(id, rh.getId());
        return ResponseEntity.ok(response);
    }

}
