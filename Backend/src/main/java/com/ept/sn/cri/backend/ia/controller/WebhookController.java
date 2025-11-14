package com.ept.sn.cri.backend.ia.controller;

import com.ept.sn.cri.backend.entity.Application;
import com.ept.sn.cri.backend.enums.ApplicationStatus;
import com.ept.sn.cri.backend.exception.ResourceNotFoundException;
import com.ept.sn.cri.backend.ia.dto.ProcessCVResponseDTO;
import com.ept.sn.cri.backend.rh.repository.ApplicationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/webhook")
@RequiredArgsConstructor
@Slf4j
public class WebhookController {

    private final ApplicationRepository applicationRepository;

    /**
     * Endpoint de callback pour recevoir les résultats du service IA
     * POST /api/v1/webhook/ia-result
     */

    @PostMapping("/ia-result")
    public ResponseEntity<String> receiveIAResult(@RequestBody ProcessCVResponseDTO response) {
        log.info(" Callback IA reçu pour candidature {}", response.getApplicationId());

        try {
            if (response.getSuccess()) {
                // Sauvegarder les résultats dans la base de données
                Application application = applicationRepository.findById(response.getApplicationId())
                        .orElseThrow(() -> new ResourceNotFoundException(
                                "Candidature non trouvée: " + response.getApplicationId()
                        ));

                // Mettre à jour les scores
                if (response.getScoringResult() != null) {
                    var scoring = response.getScoringResult();

                    application.setScoreIA(scoring.getScoreGlobal().intValue());
                    application.setMatchingCompetences(scoring.getMatchingCompetences().intValue());
                    application.setMatchingExperience(scoring.getMatchingExperience().intValue());
                    application.setMatchingDiploma(scoring.getMatchingDiploma().intValue());
                    application.setJustificationIA(scoring.getJustification());
                    application.setApplicationStatus(ApplicationStatus.AI_SCORED);

                    applicationRepository.save(application);

                    log.info(" Résultats IA sauvegardés pour candidature {} - Score: {}",
                            response.getApplicationId(), scoring.getScoreGlobal());
                } else {
                    log.warn(" Pas de résultat de scoring dans le callback pour candidature {}",
                            response.getApplicationId());
                }

                return ResponseEntity.ok("Callback traité avec succès");

            } else {
                log.error("Callback IA en échec pour candidature {}: {}",
                        response.getApplicationId(), response.getErrorMessage());

                return ResponseEntity.status(HttpStatus.ACCEPTED)
                        .body("Callback reçu mais traitement IA en échec");
            }

        } catch (Exception e) {
            log.error(" Erreur lors du traitement du callback IA pour candidature {}",
                    response.getApplicationId(), e);

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erreur lors du traitement du callback: " + e.getMessage());
        }
    }

}
